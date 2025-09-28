package com.snailycfx.managers;

import com.snailycfx.Rankup;
import com.snailycfx.data.PlayerData;

import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.sql.*;

import com.snailycfx.utils.RankupUtils;

public class DatabaseManager {
    private final Map<UUID, PlayerData> cache = new WeakHashMap<>();
    private final Map<UUID, Long> pendingIncrements = new ConcurrentHashMap<>();
    private Connection connection;

    private final Rankup plugin;
    private final RankupUtils rankupUtils;
    private final ConfigManager configManager;

    public DatabaseManager(Rankup plugin, RankupUtils rankupUtils, ConfigManager configManager) {
        this.plugin = plugin;
        this.rankupUtils = rankupUtils;
        this.configManager = configManager;

        try {
            initDatabase();
            loadAllPlayers();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database: {0}", e.getMessage());
        }
    }

    private void initDatabase() throws IOException {
        try {
            File dataFile = new File(plugin.getDataFolder(), "playerdata.db");
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile.getPath());
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode = WAL;");
                stmt.execute("PRAGMA synchronous = NORMAL;");
                stmt.execute("PRAGMA cache_size = 10000;");
                stmt.execute("PRAGMA temp_store = MEMORY;");
                stmt.execute("PRAGMA wal_autocheckpoint = 100;");
            }

            String query = "CREATE TABLE IF NOT EXISTS players (uuid TEXT PRIMARY KEY, rank INTEGER, blocks INTEGER)";

            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database: {0}", e.getMessage());
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData pd = cache.get(uuid);
        if (pd != null) {
            Long pending = pendingIncrements.getOrDefault(uuid, 0L);
            if (pending > 0) {
                pd.setBlocksMined(pd.getBlocksMined() + pending);
            }

            return pd;
        }

        String query = "SELECT rank, blocks FROM players WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int rank = rs.getInt("rank");
                    long blocks = rs.getLong("blocks");

                    pd = new PlayerData(rank, blocks);
                } else {
                    pd = new PlayerData(1, 0L);
                    savePlayerDataSync(uuid, pd);
                }
            } 
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player data for " + uuid.toString() + ". {0}", e.getMessage());
            pd = new PlayerData(1, 0L);
        }

        cache.put(uuid, pd);
        return pd;
    }

    public void savePlayerDataSync(UUID uuid, PlayerData pd) {
        if (pd == null) return;

        String query = "INSERT OR REPLACE INTO players (uuid, rank, blocks) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setInt(2, pd.getRank());
            pstmt.setLong(3, pd.getBlocksMined());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player data for " + uuid.toString() + ". {0}", e.getMessage());
        }

        cache.put(uuid, pd);
    }

    public final void loadAllPlayers() {
        String query = "SELECT uuid, rank, blocks FROM players";

        try (PreparedStatement pstmt = connection.prepareStatement(query); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                int rank = rs.getInt("rank");
                long blocks = rs.getLong("blocks");

                cache.put(uuid, new PlayerData(rank, blocks));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load all players data: {0}", e.getMessage());
        }
    }

    public boolean incrementBlocksMined(UUID uuid, Player player) {
        PlayerData pd = getPlayerData(uuid);
        int maxRank = configManager.getMaxRank();

        if (pd.getRank() >= maxRank) {
            return false;
        }

        pd.incrementBlocksMined();
        pendingIncrements.merge(uuid, 1L, Long::sum);

        boolean ranked = rankupUtils.rankupPlayer(pd);
        if (ranked) {
            pendingIncrements.remove(uuid);
            savePlayerDataSync(uuid, pd);
        }

        long pending = pendingIncrements.getOrDefault(uuid, 0L);
        int batchSize = configManager.getBatchSize();

        if (pending >= batchSize) {
            flushPendingAsync(uuid);
        }

        return ranked;
    }

    public void flushPendingAsync(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            flushPendingSync(uuid);
        });
    }

    public void flushAllPendingAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            pendingIncrements.forEach((uuid, pending) -> {
                if (pending > 0) {
                    flushPendingSync(uuid);
                }
            });

            pendingIncrements.clear();
        });
    }

    private void flushPendingSync(UUID uuid) {
        Long pending = pendingIncrements.remove(uuid);
        if (pending == null || pending == 0) return;

        PlayerData pd = cache.get(uuid);
        if (pd == null) return;

        String query = "UPDATE players SET blocks = blocks + ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setLong(1, pending);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to flush pending for {0}: {1}", new Object[]{uuid, e.getMessage()});
        }
    }

    // Sync flush all (only for onDisable)
    public void flushAllPendingSync() {
        pendingIncrements.forEach((uuid, pending) -> {
            if (pending > 0) {
                flushPendingSync(uuid);
            }
        });

        pendingIncrements.clear();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA wal_checkpoint(FULL);");
                }

                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close SQLite database: {0}", e.getMessage());
        }

        cache.clear();
        pendingIncrements.clear();
    }
}
