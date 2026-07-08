package com.tankbattle.model;

import com.tankbattle.controller.Collidable;
import com.tankbattle.controller.CollisionEvent;
import com.tankbattle.controller.CollisionGroup;

import java.awt.Rectangle;

public class Base implements Collidable {
    private int x;
    private int y;
    private boolean destroyed;
    private static final int BASE_SIZE = 32;

    public Base(int x, int y) {
        this.x = x;
        this.y = y;
        this.destroyed = false;
    }

    public void destroy() { destroyed = true; }

    @Override
    public Rectangle getBoundingBox() { return new Rectangle(x, y, BASE_SIZE, BASE_SIZE); }

    @Override
    public void onCollision(CollisionEvent event) { destroy(); }

    @Override
    public CollisionGroup getCollisionGroup() { return CollisionGroup.BASE; }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isDestroyed() { return destroyed; }
    public int getSize() { return BASE_SIZE; }
}