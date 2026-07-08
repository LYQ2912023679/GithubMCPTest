package com.tankbattle.model;

import com.tankbattle.controller.CollisionGroup;
import com.tankbattle.config.GameConfig;

public class EnemyTank extends Tank {
    private EnemyType type;
    private int hp;
    private boolean frozen;
    private long frozenStartTime;
    private long frozenDuration;
    private long directionChangeTimer;
    private long shootingTimer;
    private long directionChangeInterval;
    private long shootingInterval;

    public EnemyTank(int x, int y, EnemyType type) {
        super(x, y, Direction.DOWN, getSpeedForType(type));
        this.type = type;
        this.hp = getHpForType(type);
        this.frozen = false;
        this.frozenStartTime = 0;
        this.frozenDuration = 0;
        this.directionChangeTimer = 0;
        this.shootingTimer = 0;
        this.directionChangeInterval = randomInterval(1000, 4000);
        this.shootingInterval = randomInterval(1000, 3000);
    }

    private static int getSpeedForType(EnemyType type) {
        switch (type) {
            case FAST: return GameConfig.ENEMY_FAST_SPEED;
            default: return GameConfig.ENEMY_NORMAL_SPEED;
        }
    }

    private static int getHpForType(EnemyType type) {
        switch (type) {
            case ARMORED: return 3;
            default: return 1;
        }
    }

    private long randomInterval(int min, int max) {
        return min + (long) (Math.random() * (max - min));
    }

    @Override
    public Bullet fire() {
        if (!alive || frozen || bullet != null) return null;
        int bx, by;
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
        bullet = new Bullet(bx, by, direction, GameConfig.BULLET_SPEED, false, false);
        return bullet;
    }

    public void freeze(long currentTime, long duration) {
        frozen = true;
        frozenStartTime = currentTime;
        frozenDuration = duration;
    }

    public void updateFreeze(long currentTime) {
        if (frozen && currentTime - frozenStartTime >= frozenDuration) {
            frozen = false;
        }
    }

    public void hit() {
        hp--;
        if (hp <= 0) destroy();
    }

    @Override
    public CollisionGroup getCollisionGroup() { return CollisionGroup.ENEMY; }

    public EnemyType getType() { return type; }
    public int getHp() { return hp; }
    public boolean isFrozen() { return frozen; }
    public long getDirectionChangeTimer() { return directionChangeTimer; }
    public void setDirectionChangeTimer(long timer) { this.directionChangeTimer = timer; }
    public long getShootingTimer() { return shootingTimer; }
    public void setShootingTimer(long timer) { this.shootingTimer = timer; }
    public long getDirectionChangeInterval() { return directionChangeInterval; }
    public void setDirectionChangeInterval(long interval) { this.directionChangeInterval = interval; }
    public long getShootingInterval() { return shootingInterval; }
    public void setShootingInterval(long interval) { this.shootingInterval = interval; }
    public void resetDirectionChangeInterval() { this.directionChangeInterval = randomInterval(1000, 4000); }
    public void resetShootingInterval() { this.shootingInterval = randomInterval(1000, 3000); }
}