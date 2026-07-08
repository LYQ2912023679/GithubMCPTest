package com.tankbattle.model;

import com.tankbattle.controller.Collidable;
import com.tankbattle.controller.CollisionEvent;
import com.tankbattle.controller.CollisionGroup;
import com.tankbattle.config.GameConfig;

import java.awt.Rectangle;

public class Bullet implements Updatable, Collidable {
    private int x;
    private int y;
    private Direction direction;
    private int speed;
    private boolean fromPlayer;
    private boolean enhanced;
    private boolean active;

    public Bullet(int x, int y, Direction direction, int speed, boolean fromPlayer, boolean enhanced) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
        this.fromPlayer = fromPlayer;
        this.enhanced = enhanced;
        this.active = true;
    }

    @Override
    public void update(long deltaTime) {
        if (!active) return;
        x += direction.dx() * speed;
        y += direction.dy() * speed;
        if (isOutOfBounds()) active = false;
    }

    public boolean isOutOfBounds() {
        return x < 0 || y < 0 || x >= GameConfig.GAME_AREA_SIZE || y >= GameConfig.GAME_AREA_SIZE;
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public Rectangle getBoundingBox() { return new Rectangle(x, y, 4, 4); }

    @Override
    public void onCollision(CollisionEvent event) { active = false; }

    @Override
    public CollisionGroup getCollisionGroup() {
        return fromPlayer ? CollisionGroup.PLAYER : CollisionGroup.ENEMY;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }
    public int getSpeed() { return speed; }
    public boolean isFromPlayer() { return fromPlayer; }
    public boolean isEnhanced() { return enhanced; }
    public void deactivate() { active = false; }
}