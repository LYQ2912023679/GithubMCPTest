package com.tankbattle.config;

public class GameConfig {
    public static final int PLAYER_SPEED = 2;
    public static final int BULLET_SPEED = 4;
    public static final int ENHANCED_BULLET_SPEED = 6;
    public static final int ENEMY_NORMAL_SPEED = 1;
    public static final int ENEMY_FAST_SPEED = 2;
    public static final long SHIELD_DURATION = 3000;
    public static final long FREEZE_DURATION = 3000;
    public static final long POWERUP_DURATION = 10000;
    public static final int MAX_ENEMIES_ON_SCREEN = 4;
    public static final int POWERUP_SPAWN_INTERVAL = 3;
    public static final int MAP_SIZE = 26;
    public static final int TILE_SIZE = 16;
    public static final int GAME_AREA_SIZE = MAP_SIZE * TILE_SIZE;
    public static final int TANK_SIZE = 32;
    public static final int HUD_WIDTH = 160;
    public static final int WINDOW_WIDTH = GAME_AREA_SIZE + HUD_WIDTH;
    public static final int WINDOW_HEIGHT = GAME_AREA_SIZE;
    public static final int TARGET_FPS = 60;
    public static final int FRAME_INTERVAL = 1000 / TARGET_FPS;
    public static final int PLAYER_INITIAL_LIVES = 3;
    public static final int MAX_PLAYER_LEVEL = 2;
    public static final int BULLET_STEP_SIZE = 8;
    public static final int GRID_SIZE = 16;
    public static final int ENEMY_SPAWN_POINTS[] = {0, 12, 24};
}