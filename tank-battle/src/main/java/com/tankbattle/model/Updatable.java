package com.tankbattle.model;

public interface Updatable {
    void update(long deltaTime);
    boolean isActive();
}