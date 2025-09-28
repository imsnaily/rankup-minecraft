package com.snailycfx.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.snailycfx.managers.ConfigManager;

public class MessageUtils {
    private final ConfigManager configManager;

    public MessageUtils(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void sendMessage(Player player, String key, Object... placeholders) {
        String message = configManager.getMessage(key);
        if (message == null) {
            message = "&cDefault message for " + key;
        }

        message = applyPlaceholders(message, placeholders);
        player.sendMessage(color(message));
    }

    @SuppressWarnings("deprecation")
    public void sendProgressActionBar(Player player, int percent) {
        int length = 30;
        int filled = (percent * length) / 100;
        StringBuilder barBuilder = new StringBuilder();

        for (int i = 0; i < filled; i++) {
            barBuilder.append(ChatColor.GREEN).append(":");
        }

        for (int i = filled; i < length; i++) {
            barBuilder.append(ChatColor.RED).append(":");
        }

        String bar = barBuilder.toString();
        String baseMsg = configManager.getMessage("progress-actionbar");
        if (baseMsg == null) {
            baseMsg = "Progress: &8[&a%bar%&8] (&a%percent%%&8)";
        }

        String msg = applyPlaceholders(baseMsg, "bar", bar, "percent", percent);
        player.sendActionBar(color(msg));
    }

    @SuppressWarnings("deprecation")
    public void sendMaxRankActionBar(Player player, int rank) {
        String msg = ChatColor.GOLD + "MAX RANK ACHIEVED: " + ChatColor.GREEN + rank;
        player.sendActionBar(msg);
    }

    @SuppressWarnings("deprecation")
    public void broadcastMessage(String key, Object... placeholders) {
        String message = configManager.getMessage(key);
        if (message == null) { return; }

        message = applyPlaceholders(message, placeholders);
        Bukkit.broadcastMessage(color(message));
    }

    private String applyPlaceholders(String message, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String placeholder = "%" + placeholders[i] + "%";
                message = message.replace(placeholder, String.valueOf(placeholders[i + 1]));
            }
        }
        return message;
    }

    @SuppressWarnings("deprecation")
    public String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
