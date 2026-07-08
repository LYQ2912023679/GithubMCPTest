package com.tankbattle.controller;

import java.awt.Rectangle;

public interface Collidable {
    Rectangle getBoundingBox();
    void onCollision(CollisionEvent event);
    CollisionGroup getCollisionGroup();
}