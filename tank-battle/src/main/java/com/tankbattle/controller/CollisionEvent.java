package com.tankbattle.controller;

import com.tankbattle.controller.Collidable;

public class CollisionEvent {
    private Collidable source;
    private Collidable target;

    public CollisionEvent(Collidable source, Collidable target) {
        this.source = source;
        this.target = target;
    }

    public Collidable getSource() { return source; }
    public Collidable getTarget() { return target; }
}