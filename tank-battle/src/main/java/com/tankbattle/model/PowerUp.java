package com.tankbattle.model;

import com.tankbattle.controller.Collidable;
import com.tankbattle.controller.CollisionEvent;
import com.tankbattle.controller.CollisionGroup;
import com.tankbattle.config.GameConfig;

import java.awt.Rectangle;

public class PowerUp implements Updatable, Collidable {
    private int x;
    private int y;
    private PowerUpType type;
    private long creationTime;
    private long duration;

    public PowerUp(int x, int y, PowerUpType type, long creationTime) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.creationTime = creationTime;
        this.duration = GameConfig.POWERUP_DURATION;
    }

    public boolean isExpired(long currentTime) {
        return currentTime - creationTime >= duration;
    }

    @Override
    public void update(long deltaTime) {}

    @Override
    public boolean isActive() { return true; }

    @Override
    public Rectangle getBoundingBox() { return new Rectangle(x, y, GameConfig.TILE_SIZE * 2, GameConfig.TILE_SIZE * 2); }

    @Override
    public void onCollision(CollisionEvent event) {}

    @Override
    public CollisionGroup getCollisionGroup() { return CollisionGroup.POWERUP; }

    public int getX() { return x; }
    public int getY() { return y; }
    public PowerUpType getType() { return type; }
    public long getCreationTime() { return creationTime; }
}