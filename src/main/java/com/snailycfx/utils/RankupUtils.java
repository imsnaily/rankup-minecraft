package com.snailycfx.utils;

import com.snailycfx.data.PlayerData;
import com.snailycfx.managers.ConfigManager;

public class RankupUtils {
    private final ConfigManager cfgManager;

    public RankupUtils(ConfigManager cfgManager) {
        this.cfgManager = cfgManager;
    }

    public boolean rankupPlayer(PlayerData pd) {
        if (!canRankUp(pd)) {
            return false;
        }

        pd.setRank(pd.getRank() + 1);
        pd.setBlocksMined(0L);
        return true;
    }

    public boolean canRankUp(PlayerData pd) {
        int maxRank = cfgManager.getMaxRank();
        long required = calculateRequiredBlocks(pd.getRank());
    
        return pd.canRankUp(maxRank, required);
    }

    public long calculateRequiredBlocks(int currentRank) {
        double base = cfgManager.getBaseBlocks();
        double mult = cfgManager.getMultiplier();

        return (long) (base * Math.pow(mult, currentRank));
    }
}
