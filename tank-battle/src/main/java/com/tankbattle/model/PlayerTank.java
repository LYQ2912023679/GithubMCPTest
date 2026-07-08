package com.tankbattle.model;

import com.tankbattle.controller.CollisionGroup;
import com.tankbattle.config.GameConfig;

public class PlayerTank extends Tank {
    private int lives;
    private int level;
    private boolean shielded;
    private long shieldStartTime;
    private long shieldDuration;

    public PlayerTank(int x, int y) {
        super(x, y, Direction.UP, GameConfig.PLAYER_SPEED);
        this.lives = GameConfig.PLAYER_INITIAL_LIVES;
        this.level = 0;
        this.shielded = false;
        this.shieldStartTime = 0;
        this.shieldDuration = 0;
    }

    @Override
    public Bullet fire() {
        if (!alive || bullet != null) return null;
        int bx, by;
        int bulletSpeed = (level >= 1) ? GameConfig.ENHANCED_BULLET_SPEED : GameConfig.BULLET_SPEED;
        boolean enhanced = (level >= 2);
        switch (direction) {
            case UP:
                bx = x + GameConfig.TANK_SIZE / 2 - 2;
                by = y - 4;
                break;
            case DOWN:
                bx = x + GameConfig.TANK_SIZE / 2 - 2;
                by = y + GameConfig.TANK_SIZE;
                break;
            case LEFT:
                bx = x - 4;
                by = y + GameConfig.TANK_SIZE / 2 - 2;
                break;
            case RIGHT:
                bx = x + GameConfig.TANK_SIZE;
                by = y + GameConfig.TANK_SIZE / 2 - 2;
                break;
            default:
                return null;
        }
        bullet = new Bullet(bx, by, direction, bulletSpeed, true, enhanced);
        return bullet;
    }

    public void respawn() {
        lives--;
        x = 8 * GameConfig.TILE_SIZE;
        y = 24 * GameConfig.TILE_SIZE;
        direction = Direction.UP;
        alive = true;
        bullet = null;
        activateShield(System.currentTimeMillis(), GameConfig.SHIELD_DURATION);
    }

    public void upgrade() {
        if (level < GameConfig.MAX_PLAYER_LEVEL) level++;
    }

    public void activateShield(long currentTime, long duration) {
        shielded = true;
        shieldStartTime = currentTime;
        shieldDuration = duration;
    }

    public void updateShield(long currentTime) {
        if (shielded && currentTime - shieldStartTime >= shieldDuration) {
            shielded = false;
        }
    }

    public int getLives() { return lives; }
    public int getLevel() { return level; }
    public boolean isShielded() { return shielded; }
    public void setLives(int lives) { this.lives = lives; }
    public void setLevel(int level) { this.level = level; }

    @Override
    public CollisionGroup getCollisionGroup() { return CollisionGroup.PLAYER; }
}