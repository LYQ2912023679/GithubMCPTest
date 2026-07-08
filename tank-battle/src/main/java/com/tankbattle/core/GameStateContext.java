package com.tankbattle.core;

import com.tankbattle.model.GameStatus;

public interface GameStateContext {
    GameStatus getCurrentStatus();
    void transitionTo(GameStatus status);
    int getCurrentLevel();
    int getScore();
    int getPlayerLives();
    int getRemainingEnemies();
    void onLevelComplete();
    void onGameOver();
    void onVictory();
}