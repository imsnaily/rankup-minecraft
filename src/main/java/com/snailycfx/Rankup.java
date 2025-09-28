package com.snailycfx;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.snailycfx.events.BlockBreakListener;
import com.snailycfx.expansions.RankupExpansion;
import com.snailycfx.managers.ConfigManager;
import com.snailycfx.managers.DatabaseManager;
import com.snailycfx.utils.MessageUtils;
import com.snailycfx.utils.RankupUtils;

public class Rankup extends JavaPlugin {
    private ConfigManager cfgManager;
    private DatabaseManager dbManager;
    private MessageUtils messageUtils;
    private RankupUtils rankupUtils;
    
    @Override
    public void onEnable() {
        cfgManager = new ConfigManager(this);
        rankupUtils = new RankupUtils(cfgManager);
        messageUtils = new MessageUtils(cfgManager);
        dbManager = new DatabaseManager(this, rankupUtils, cfgManager);

        getServer().getPluginManager().registerEvents(new BlockBreakListener(dbManager, cfgManager, rankupUtils, messageUtils), this);
        
        int flushInterval = cfgManager.getFlushIntervalTicks();
        if (flushInterval > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    dbManager.flushAllPendingAsync();
                }
            }.runTaskTimerAsynchronously(this, flushInterval, flushInterval);
        }

        if (cfgManager.isPlaceholderAPIEnabled()) {
            if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new RankupExpansion(dbManager, rankupUtils).register();
                getLogger().info("PlaceholderAPI integration enabled!");
            } else {
                getLogger().info("PlaceholderAPI not found. Placeholders disabled.");
            }
        }
        
        getLogger().info("Rankup Plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (dbManager != null) {
            dbManager.flushAllPendingSync(); // Sync flush on disable to avoid data loss
            dbManager.close();
        }

        getLogger().info("Rankup Plugin disabled!");
    }
}