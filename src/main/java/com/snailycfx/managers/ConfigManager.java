package com.snailycfx.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public int getMaxRank() {
        return config.getInt("max-rank", 100);
    }

    public double getBaseBlocks() {
        return config.getDouble("base-blocks", 100.0);
    }

    public double getMultiplier() {
        return config.getDouble("multiplier", 1.5);
    }

    public boolean isBroadcastRankup() {
        return config.getBoolean("broadcast-rankup", true);
    }

    public String getMessage(String key) {
        return config.getString("messages." + key);
    }

    public int getBatchSize() {
        return config.getInt("optimizations.batch-size", 10);
    }

    public int getFlushIntervalTicks() {
        return config.getInt("optimizations.flush-interval-ticks", 100);
    }

    public long getActionBarThrottleMs() {
        return config.getLong("optimizations.actionbar-throttle-ms", 1000L);
    }

    public boolean isPlaceholderAPIEnabled() {
        return config.getBoolean("placeholderapi.enabled", true);
    }
}
