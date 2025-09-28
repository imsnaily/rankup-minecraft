package com.snailycfx.data;

public class PlayerData {
    private int rank;
    private long blocksMined;

    public PlayerData(int rank, long blocksMined) {
        this.rank = rank;
        this.blocksMined = blocksMined;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getBlocksMined() {
        return blocksMined;
    }

    public void setBlocksMined(long blocksMined) {
        this.blocksMined = blocksMined;
    }

    public void incrementBlocksMined() {
        this.blocksMined++;
    }

    public boolean canRankUp(int maxRank, long required) {
        return rank < maxRank && blocksMined >= required;
    }
}
