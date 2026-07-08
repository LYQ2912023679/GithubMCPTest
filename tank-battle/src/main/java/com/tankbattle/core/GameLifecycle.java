package com.tankbattle.core;

import com.tankbattle.model.GameStatus;

public interface GameLifecycle {
    void start();
    void stop();
    void pause();
    void resume();
    GameStatus getStatus();
}