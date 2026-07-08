package com.tankbattle.config;

public interface LevelLoader {
    LevelData loadLevel(int levelNumber) throws LevelLoadException;
    boolean levelExists(int levelNumber);
}