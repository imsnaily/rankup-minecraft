package com.snailycfx.expansions;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.snailycfx.data.PlayerData;
import com.snailycfx.managers.DatabaseManager;
import com.snailycfx.utils.RankupUtils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

// https://github.com/PlaceholderAPI/Example-Expansion/blob/master/src/main/java/com/extendedclip/expansions/example/ExampleExpansion.java
public class RankupExpansion extends PlaceholderExpansion {
    private final DatabaseManager dbManager;
    private final RankupUtils rankupUtils;
    private final String VERSION = getClass().getPackage().getImplementationVersion();

    public RankupExpansion(DatabaseManager dbManager, RankupUtils rankupUtils) {
        this.dbManager = dbManager;
        this.rankupUtils = rankupUtils;
    }

    @Override
    public String getIdentifier() {
        return "mcrankup"; // Prefix: %mcrankup_%
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getAuthor() {
        return "Snaily";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (params.equals("test")) { return "success"; }
        if (offlinePlayer == null || !offlinePlayer.isOnline()) { return "player is not online"; }

        Player player = offlinePlayer.getPlayer();
        if (player == null) { return "player is not online"; }

        UUID uuid = player.getUniqueId();
        PlayerData pd = dbManager.getPlayerData(uuid);

        switch (params) {
            case "rank" -> {
                return String.valueOf(pd.getRank());
            }

            case "blocks" -> {
                return String.valueOf(pd.getBlocksMined());
            }

            case "progress" -> {
                long required = rankupUtils.calculateRequiredBlocks(pd.getRank());
                double progress = required > 0 ? (pd.getBlocksMined() * 100.0) / required : 0;
                int percent = (int) Math.min(100, progress);

                return percent + "%";
            }
            
            default -> {
                return null;
            }
        }
    }
}
