package com.tankbattle.model;

import com.tankbattle.controller.Collidable;
import com.tankbattle.controller.CollisionEvent;
import com.tankbattle.controller.CollisionGroup;
import com.tankbattle.config.GameConfig;

import java.awt.Rectangle;

public abstract class Tank implements Updatable, Collidable {
    protected int x;
    protected int y;
    protected Direction direction;
    protected int speed;
    protected boolean alive;
    protected Bullet bullet;
    protected boolean moving;

    public Tank(int x, int y, Direction direction, int speed) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
        this.alive = true;
        this.bullet = null;
        this.moving = false;
    }

    public void move(Direction dir, GameMap map) {
        if (!alive) return;
        if (dir != direction) {
            direction = dir;
            alignToGrid();
        }
        this.moving = true;
        int newX = x + dir.dx() * speed;
        int newY = y + dir.dy() * speed;
        newX = Math.max(0, Math.min(newX, GameConfig.GAME_AREA_SIZE - GameConfig.TANK_SIZE));
        newY = Math.max(0, Math.min(newY, GameConfig.GAME_AREA_SIZE - GameConfig.TANK_SIZE));
        if (canMoveTo(newX, newY, map)) {
            x = newX;
            y = newY;
        }
    }

    protected void alignToGrid() {
        int gs = GameConfig.GRID_SIZE;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            x = Math.round((float) x / gs) * gs;
        } else {
            y = Math.round((float) y / gs) * gs;
        }
        x = Math.max(0, Math.min(x, GameConfig.GAME_AREA_SIZE - GameConfig.TANK_SIZE));
        y = Math.max(0, Math.min(y, GameConfig.GAME_AREA_SIZE - GameConfig.TANK_SIZE));
    }

    protected boolean canMoveTo(int newX, int newY, GameMap map) {
        int ts = GameConfig.TILE_SIZE;
        int left = newX / ts;
        int top = newY / ts;
        int right = (newX + GameConfig.TANK_SIZE - 1) / ts;
        int bottom = (newY + GameConfig.TANK_SIZE - 1) / ts;
        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                if (!map.isPassable(col, row)) return false;
            }
        }
        return true;
    }

    public abstract Bullet fire();

    public void destroy() {
        alive = false;
        bullet = null;
    }

    @Override
    public void update(long deltaTime) {}

    @Override
    public boolean isActive() { return alive; }

    @Override
    public Rectangle getBoundingBox() {
        return new Rectangle(x, y, GameConfig.TANK_SIZE, GameConfig.TANK_SIZE);
    }

    @Override
    public void onCollision(CollisionEvent event) {}

    public int getX() { return x; }
    public int getY() { return y; }
    public Direction getDirection() { return direction; }
    public int getSpeed() { return speed; }
    public boolean isAlive() { return alive; }
    public Bullet getBullet() { return bullet; }
    public void setBullet(Bullet bullet) { this.bullet = bullet; }
    public boolean isMoving() { return moving; }
    public void setMoving(boolean moving) { this.moving = moving; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setDirection(Direction direction) { this.direction = direction; }
}