package com.snailycfx.events;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.snailycfx.data.PlayerData;
import com.snailycfx.managers.ConfigManager;
import com.snailycfx.managers.DatabaseManager;
import com.snailycfx.utils.MessageUtils;
import com.snailycfx.utils.RankupUtils;

public class BlockBreakListener implements Listener {
    private final Map<UUID, Long> lastActionBarTime = new ConcurrentHashMap<>();
    private final DatabaseManager dbManager;
    private final ConfigManager cfgManager;
    private final MessageUtils messageUtils;
    private final RankupUtils rankupUtils;

    public BlockBreakListener(DatabaseManager dbManager, ConfigManager cfgManager, RankupUtils rankupUtils, MessageUtils messageUtils) {
        this.dbManager = dbManager;
        this.cfgManager = cfgManager;
        this.messageUtils = messageUtils;
        this.rankupUtils = rankupUtils;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        PlayerData pd = dbManager.getPlayerData(uuid);
        int maxRank = cfgManager.getMaxRank();

        if (pd.getRank() >= maxRank) {
            final int rank = pd.getRank();
            sendThrottledActionBar(uuid, () -> messageUtils.sendMaxRankActionBar(player, rank));
            return;
        }

        boolean rankedUp = dbManager.incrementBlocksMined(uuid, player);
        pd = dbManager.getPlayerData(uuid);

        long required = rankupUtils.calculateRequiredBlocks(pd.getRank());
        if (required == 0) required = 1;

        double progress =(pd.getBlocksMined() * 100.0) / required;
        int percent = (int) Math.min(100, progress);
        sendThrottledActionBar(uuid, () -> messageUtils.sendProgressActionBar(player, percent));

        if (rankedUp) {
            messageUtils.sendMessage(player, "rankup-success", "rank", pd.getRank());

            if (cfgManager.isBroadcastRankup()) {
                messageUtils.broadcastMessage("rankup-broadcast", "player", player.getName(), "rank", pd.getRank());
            }
        }
    }

    private void sendThrottledActionBar(UUID uuid, Runnable action) {
        long now = System.currentTimeMillis();
        long lastTime = lastActionBarTime.getOrDefault(uuid, 0L);
        long throttleMs = cfgManager.getActionBarThrottleMs();

        if (now - lastTime > throttleMs) {
            action.run();
            lastActionBarTime.put(uuid, now);
        }
    }
}
