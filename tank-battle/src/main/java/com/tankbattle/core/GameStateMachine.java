package com.tankbattle.core;

import com.tankbattle.model.GameStatus;
import com.tankbattle.config.LevelConfigLoader;
import com.tankbattle.config.LevelData;
import com.tankbattle.config.LevelLoadException;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class GameStateMachine implements GameStateContext, GameLifecycle {
    private static final Logger logger = Logger.getLogger(GameStateMachine.class.getName());

    private GameStatus currentStatus;
    private int currentLevel;
    private int score;
    private int playerLives;
    private int remainingEnemies;
    private int totalEnemies;
    private int killedEnemies;
    private LevelConfigLoader levelLoader;

    private static final Set<String> VALID_TRANSITIONS = new HashSet<>();
    static {
        VALID_TRANSITIONS.add("MENU->PLAYING");
        VALID_TRANSITIONS.add("PLAYING->PAUSED");
        VALID_TRANSITIONS.add("PAUSED->PLAYING");
        VALID_TRANSITIONS.add("PLAYING->LEVEL_TRANSITION");
        VALID_TRANSITIONS.add("LEVEL_TRANSITION->PLAYING");
        VALID_TRANSITIONS.add("PLAYING->VICTORY");
        VALID_TRANSITIONS.add("PLAYING->GAME_OVER");
        VALID_TRANSITIONS.add("VICTORY->MENU");
        VALID_TRANSITIONS.add("GAME_OVER->MENU");
    }

    public GameStateMachine() {
        this.currentStatus = GameStatus.MENU;
        this.currentLevel = 1;
        this.score = 0;
        this.playerLives = 3;
        this.remainingEnemies = 0;
        this.totalEnemies = 0;
        this.killedEnemies = 0;
        this.levelLoader = new LevelConfigLoader();
    }

    @Override
    public GameStatus getCurrentStatus() { return currentStatus; }

    @Override
    public void transitionTo(GameStatus status) {
        String transition = currentStatus.name() + "->" + status.name();
        if (!VALID_TRANSITIONS.contains(transition)) {
            logger.warning("Invalid state transition: " + transition);
            throw new IllegalStateException("Invalid state transition: " + transition);
        }
        logger.info("State transition: " + transition);
        currentStatus = status;
    }

    @Override
    public int getCurrentLevel() { return currentLevel; }

    @Override
    public int getScore() { return score; }

    @Override
    public int getPlayerLives() { return playerLives; }

    @Override
    public int getRemainingEnemies() { return remainingEnemies; }

    @Override
    public void onLevelComplete() {
        score += 1000;
        if (levelLoader.levelExists(currentLevel + 1)) {
            currentLevel++;
            transitionTo(GameStatus.LEVEL_TRANSITION);
        } else {
            onVictory();
        }
    }

    @Override
    public void onGameOver() {
        transitionTo(GameStatus.GAME_OVER);
    }

    @Override
    public void onVictory() {
        transitionTo(GameStatus.VICTORY);
    }

    @Override
    public void start() {
        currentLevel = 1;
        score = 0;
        playerLives = 3;
        killedEnemies = 0;
        loadLevelData();
        transitionTo(GameStatus.PLAYING);
    }

    @Override
    public void stop() {
        currentStatus = GameStatus.MENU;
    }

    @Override
    public void pause() {
        if (currentStatus == GameStatus.PLAYING) {
            transitionTo(GameStatus.PAUSED);
        }
    }

    @Override
    public void resume() {
        if (currentStatus == GameStatus.PAUSED) {
            transitionTo(GameStatus.PLAYING);
        }
    }

    @Override
    public GameStatus getStatus() { return currentStatus; }

    public void loadLevelData() {
        try {
            LevelData data = levelLoader.loadLevel(currentLevel);
            totalEnemies = data.getTotalEnemies();
            remainingEnemies = totalEnemies;
            killedEnemies = 0;
        } catch (LevelLoadException e) {
            logger.severe("Failed to load level " + currentLevel + ": " + e.getMessage());
            totalEnemies = 10;
            remainingEnemies = 10;
            killedEnemies = 0;
        }
    }

    public void addScore(int points) { score += points; }
    public void playerDied() { playerLives--; }
    public void enemyKilled() { killedEnemies++; remainingEnemies--; }
    public int getTotalEnemies() { return totalEnemies; }
    public int getKilledEnemies() { return killedEnemies; }
    public void setPlayerLives(int lives) { this.playerLives = lives; }
    public LevelConfigLoader getLevelLoader() { return levelLoader; }
}