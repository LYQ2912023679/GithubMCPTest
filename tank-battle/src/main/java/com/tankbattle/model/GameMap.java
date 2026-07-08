package com.tankbattle.model;

import com.tankbattle.config.GameConfig;

public class GameMap {
    private TileType[][] tiles;
    private int width;
    private int height;

    public GameMap(TileType[][] tiles) {
        this.tiles = tiles;
        this.height = tiles.length;
        this.width = tiles[0].length;
    }

    public GameMap() {
        this.width = GameConfig.MAP_SIZE;
        this.height = GameConfig.MAP_SIZE;
        this.tiles = new TileType[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                tiles[row][col] = TileType.EMPTY;
            }
        }
    }

    public TileType getTile(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) return TileType.STEEL;
        return tiles[row][col];
    }

    public void removeTile(int col, int row) {
        if (col >= 0 && col < width && row >= 0 && row < height) {
            tiles[row][col] = TileType.EMPTY;
        }
    }

    public boolean isPassable(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) return false;
        TileType t = tiles[row][col];
        return t == TileType.EMPTY || t == TileType.FOREST;
    }

    public boolean isBulletPassable(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) return false;
        TileType t = tiles[row][col];
        return t == TileType.EMPTY || t == TileType.FOREST || t == TileType.WATER;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public TileType[][] getTiles() { return tiles; }
    public void setTile(int col, int row, TileType type) {
        if (col >= 0 && col < width && row >= 0 && row < height) {
            tiles[row][col] = type;
        }
    }
}