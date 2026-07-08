package com.tankbattle.config;

import com.tankbattle.model.TileType;

public class LevelData {
    private int levelNumber;
    private TileType[][] mapLayout;
    private int totalEnemies;
    private int normalCount;
    private int fastCount;
    private int armoredCount;

    public LevelData(int levelNumber, TileType[][] mapLayout, int totalEnemies, int normalCount, int fastCount, int armoredCount) {
        this.levelNumber = levelNumber;
        this.mapLayout = mapLayout;
        this.totalEnemies = totalEnemies;
        this.normalCount = normalCount;
        this.fastCount = fastCount;
        this.armoredCount = armoredCount;
    }

    public int getLevelNumber() { return levelNumber; }
    public TileType[][] getMapLayout() { return mapLayout; }
    public int getTotalEnemies() { return totalEnemies; }
    public int getNormalCount() { return normalCount; }
    public int getFastCount() { return fastCount; }
    public int getArmoredCount() { return armoredCount; }
}