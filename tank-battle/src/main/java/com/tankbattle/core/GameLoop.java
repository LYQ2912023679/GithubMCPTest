package com.tankbattle.core;

import com.tankbattle.ai.EnemyAI;
import com.tankbattle.audio.AudioManager;
import com.tankbattle.audio.SoundEffect;
import com.tankbattle.config.GameConfig;
import com.tankbattle.config.LevelData;
import com.tankbattle.controller.CollisionDetector;
import com.tankbattle.controller.InputController;
import com.tankbattle.controller.KeyCode;
import com.tankbattle.model.*;
import com.tankbattle.view.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class GameLoop {
    private static final Logger logger = Logger.getLogger(GameLoop.class.getName());

    private GameStateMachine stateMachine;
    private InputController inputController;
    private CollisionDetector collisionDetector;
    private GameRenderer gameRenderer;
    private HUDRenderer hudRenderer;
    private MenuRenderer menuRenderer;
    private AudioManager audioManager;
    private EnemyAI enemyAI;

    private GameMap map;
    private PlayerTank playerTank;
    private Base base;
    private List<EnemyTank> enemies;
    private List<Explosion> explosions;
    private List<PowerUp> powerUps;
    private LevelData currentLevelData;

    private int spawnedEnemies;
    private int killedInLevel;
    private int nextEnemyTypeIndex;
    private EnemyType[] enemyTypeSequence;
    private long lastTime;
    private long levelTransitionStart;
    private Random random = new Random();
    private boolean running;

    public GameLoop(GameStateMachine stateMachine, InputController inputController) {
        this.stateMachine = stateMachine;
        this.inputController = inputController;
        this.collisionDetector = new CollisionDetector();
        this.gameRenderer = new GameRenderer();
        this.hudRenderer = new HUDRenderer();
        this.menuRenderer = new MenuRenderer();
        this.audioManager = new AudioManager();
        this.enemyAI = new EnemyAI();
        this.enemies = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.running = true;
    }

    public void initLevel() {
        try {
            currentLevelData = stateMachine.getLevelLoader().loadLevel(stateMachine.getCurrentLevel());
            map = new GameMap(currentLevelData.getMapLayout());
        } catch (Exception e) {
            logger.warning("Failed to load level data, using empty map: " + e.getMessage());
            map = new GameMap();
        }
        playerTank = new PlayerTank(8 * GameConfig.TILE_SIZE, 24 * GameConfig.TILE_SIZE);
        playerTank.activateShield(System.currentTimeMillis(), GameConfig.SHIELD_DURATION);
        base = new Base(12 * GameConfig.TILE_SIZE, 24 * GameConfig.TILE_SIZE);
        enemies.clear();
        explosions.clear();
        powerUps.clear();
        spawnedEnemies = 0;
        killedInLevel = 0;
        nextEnemyTypeIndex = 0;
        buildEnemySequence();
    }

    private void buildEnemySequence() {
        if (currentLevelData == null) {
            enemyTypeSequence = new EnemyType[10];
            for (int i = 0; i < enemyTypeSequence.length; i++) enemyTypeSequence[i] = EnemyType.NORMAL;
            return;
        }
        int total = currentLevelData.getTotalEnemies();
        enemyTypeSequence = new EnemyType[total];
        int idx = 0;
        for (int i = 0; i < currentLevelData.getNormalCount(); i++) enemyTypeSequence[idx++] = EnemyType.NORMAL;
        for (int i = 0; i < currentLevelData.getFastCount(); i++) enemyTypeSequence[idx++] = EnemyType.FAST;
        for (int i = 0; i < currentLevelData.getArmoredCount(); i++) enemyTypeSequence[idx++] = EnemyType.ARMORED;
    }

    public void update() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastTime;
        lastTime = currentTime;
        GameStatus status = stateMachine.getCurrentStatus();

        if (status == GameStatus.MENU) {
            updateMenu();
            return;
        }

        if (status == GameStatus.PAUSED) {
            if (inputController.consumeEsc()) {
                stateMachine.resume();
            }
            return;
        }

        if (status == GameStatus.GAME_OVER || status == GameStatus.VICTORY) {
            if (inputController.consumeEsc() || inputController.isPressed(KeyCode.FIRE)) {
                stateMachine.transitionTo(GameStatus.MENU);
                menuRenderer.reset();
            }
            return;
        }

        if (status == GameStatus.LEVEL_TRANSITION) {
            if (currentTime - levelTransitionStart >= 3000) {
                stateMachine.loadLevelData();
                initLevel();
                stateMachine.transitionTo(GameStatus.PLAYING);
            }
            return;
        }

        if (status == GameStatus.PLAYING) {
            if (inputController.consumeEsc()) {
                stateMachine.pause();
                return;
            }
            updatePlaying(deltaTime, currentTime);
        }
    }

    private void updateMenu() {
        if (inputController.isPressed(KeyCode.UP)) {
            menuRenderer.moveUp();
            inputController.onKeyUp(KeyCode.UP);
        }
        if (inputController.isPressed(KeyCode.DOWN)) {
            menuRenderer.moveDown();
            inputController.onKeyUp(KeyCode.DOWN);
        }
        if (inputController.isPressed(KeyCode.FIRE)) {
            inputController.onKeyUp(KeyCode.FIRE);
            if (menuRenderer.getSelectedOption() == 0) {
                stateMachine.loadLevelData();
                initLevel();
                stateMachine.start();
            } else if (menuRenderer.getSelectedOption() == 1) {
                System.exit(0);
            }
        }
    }

    private void updatePlaying(long deltaTime, long currentTime) {
        if (playerTank.isAlive()) {
            playerTank.setMoving(false);
            if (inputController.isPressed(KeyCode.UP)) playerTank.move(Direction.UP, map);
            else if (inputController.isPressed(KeyCode.DOWN)) playerTank.move(Direction.DOWN, map);
            else if (inputController.isPressed(KeyCode.LEFT)) playerTank.move(Direction.LEFT, map);
            else if (inputController.isPressed(KeyCode.RIGHT)) playerTank.move(Direction.RIGHT, map);
            if (inputController.isPressed(KeyCode.FIRE)) {
                Bullet b = playerTank.fire();
                if (b != null) audioManager.play(SoundEffect.FIRE);
            }
            playerTank.updateShield(currentTime);
            checkTankTankCollision(playerTank);
        }

        for (EnemyTank enemy : enemies) {
            if (!enemy.isAlive() || enemy.isFrozen()) continue;
            enemy.setDirectionChangeTimer(enemy.getDirectionChangeTimer() + deltaTime);
            if (enemy.getDirectionChangeTimer() >= enemy.getDirectionChangeInterval()) {
                Direction newDir = enemyAI.decideMovement(enemy, map, base);
                enemy.setDirection(newDir);
                enemy.setDirectionChangeTimer(0);
                enemy.resetDirectionChangeInterval();
            }
            enemy.move(enemy.getDirection(), map);
            if (checkEnemyTankCollision(enemy)) {
                enemy.setDirection(enemyAI.decideMovement(enemy, map, base));
            }
            enemy.setShootingTimer(enemy.getShootingTimer() + deltaTime);
            if (enemy.getShootingTimer() >= enemy.getShootingInterval()) {
                Bullet b = enemy.fire();
                if (b != null) audioManager.play(SoundEffect.FIRE);
                enemy.setShootingTimer(0);
                enemy.resetShootingInterval();
            }
            enemy.updateFreeze(currentTime);
        }

        if (playerTank.isAlive() && playerTank.getBullet() != null) {
            playerTank.getBullet().update(deltaTime);
        }
        for (EnemyTank enemy : enemies) {
            if (enemy.getBullet() != null && enemy.getBullet().isActive()) {
                enemy.getBullet().update(deltaTime);
            }
        }

        processCollisions(currentTime);

        spawnEnemies(currentTime);
        updatePowerUps(currentTime);
        cleanupExplosions(currentTime);

        checkWinLoseConditions();
    }

    private void checkTankTankCollision(PlayerTank player) {
        for (EnemyTank enemy : enemies) {
            if (!enemy.isAlive()) continue;
            if (player.getBoundingBox().intersects(enemy.getBoundingBox())) {
                switch (player.getDirection()) {
                    case UP: player.setY(enemy.getY() + GameConfig.TANK_SIZE); break;
                    case DOWN: player.setY(enemy.getY() - GameConfig.TANK_SIZE); break;
                    case LEFT: player.setX(enemy.getX() + GameConfig.TANK_SIZE); break;
                    case RIGHT: player.setX(enemy.getX() - GameConfig.TANK_SIZE); break;
                }
            }
        }
    }

    private boolean checkEnemyTankCollision(EnemyTank enemy) {
        Rectangle eBox = enemy.getBoundingBox();
        if (playerTank.isAlive() && eBox.intersects(playerTank.getBoundingBox())) return true;
        for (EnemyTank other : enemies) {
            if (other == enemy || !other.isAlive()) continue;
            if (eBox.intersects(other.getBoundingBox())) return true;
        }
        return false;
    }

    private void processCollisions(long currentTime) {
        if (playerTank.isAlive() && playerTank.getBullet() != null && playerTank.getBullet().isActive()) {
            Bullet pBullet = playerTank.getBullet();
            collisionDetector.detectBulletMapCollision(pBullet, map, explosions, currentTime);
            if (pBullet.isActive()) {
                EnemyTank hitEnemy = collisionDetector.detectBulletEnemyCollision(pBullet, enemies);
                if (hitEnemy != null) {
                    hitEnemy.hit();
                    pBullet.deactivate();
                    explosions.add(new Explosion(hitEnemy.getX() + GameConfig.TANK_SIZE / 2,
                            hitEnemy.getY() + GameConfig.TANK_SIZE / 2, currentTime, 500, true));
                    audioManager.play(SoundEffect.EXPLOSION);
                    stateMachine.addScore(100);
                    stateMachine.enemyKilled();
                    killedInLevel++;
                }
            }
            if (pBullet.isActive() && collisionDetector.detectBulletBaseCollision(pBullet, base)) {
                base.destroy();
                pBullet.deactivate();
                explosions.add(new Explosion(base.getX() + base.getSize() / 2,
                        base.getY() + base.getSize() / 2, currentTime, 800, true));
                audioManager.play(SoundEffect.EXPLOSION);
            }
        }

        for (EnemyTank enemy : enemies) {
            if (!enemy.isAlive() || enemy.getBullet() == null || !enemy.getBullet().isActive()) continue;
            Bullet eBullet = enemy.getBullet();
            collisionDetector.detectBulletMapCollision(eBullet, map, explosions, currentTime);
            if (eBullet.isActive() && collisionDetector.detectBulletPlayerCollision(eBullet, playerTank)) {
                playerTank.destroy();
                eBullet.deactivate();
                explosions.add(new Explosion(playerTank.getX() + GameConfig.TANK_SIZE / 2,
                        playerTank.getY() + GameConfig.TANK_SIZE / 2, currentTime, 500, true));
                audioManager.play(SoundEffect.EXPLOSION);
                stateMachine.playerDied();
                if (stateMachine.getPlayerLives() > 0) {
                    playerTank.respawn();
                }
            }
            if (eBullet.isActive() && collisionDetector.detectBulletBaseCollision(eBullet, base)) {
                base.destroy();
                eBullet.deactivate();
                explosions.add(new Explosion(base.getX() + base.getSize() / 2,
                        base.getY() + base.getSize() / 2, currentTime, 800, true));
                audioManager.play(SoundEffect.EXPLOSION);
            }
            if (eBullet.isActive() && playerTank.isAlive() && playerTank.getBullet() != null
                    && playerTank.getBullet().isActive()) {
                if (collisionDetector.detectBulletBulletCollision(eBullet, playerTank.getBullet())) {
                    eBullet.deactivate();
                    playerTank.getBullet().deactivate();
                }
            }
        }

        if (playerTank.isAlive()) {
            PowerUp picked = collisionDetector.detectPlayerPowerUpCollision(playerTank, powerUps);
            if (picked != null) {
                applyPowerUp(picked, currentTime);
                powerUps.remove(picked);
                audioManager.play(SoundEffect.POWERUP);
            }
        }
    }

    private void applyPowerUp(PowerUp powerUp, long currentTime) {
        switch (powerUp.getType()) {
            case STAR:
                playerTank.upgrade();
                break;
            case SHIELD:
                playerTank.activateShield(currentTime, GameConfig.SHIELD_DURATION);
                break;
            case BOMB:
                for (EnemyTank enemy : enemies) {
                    if (enemy.isAlive()) {
                        enemy.destroy();
                        explosions.add(new Explosion(enemy.getX() + GameConfig.TANK_SIZE / 2,
                                enemy.getY() + GameConfig.TANK_SIZE / 2, currentTime, 500, true));
                        stateMachine.addScore(100);
                        stateMachine.enemyKilled();
                        killedInLevel++;
                    }
                }
                break;
            case CLOCK:
                for (EnemyTank enemy : enemies) {
                    if (enemy.isAlive()) enemy.freeze(currentTime, GameConfig.FREEZE_DURATION);
                }
                break;
        }
    }

    private void spawnEnemies(long currentTime) {
        int total = (currentLevelData != null) ? currentLevelData.getTotalEnemies() : 10;
        int activeCount = 0;
        for (EnemyTank e : enemies) if (e.isAlive()) activeCount++;

        if (spawnedEnemies < total && activeCount < GameConfig.MAX_ENEMIES_ON_SCREEN) {
            int spawnCol = GameConfig.ENEMY_SPAWN_POINTS[random.nextInt(GameConfig.ENEMY_SPAWN_POINTS.length)];
            int spawnX = spawnCol * GameConfig.TILE_SIZE;
            int spawnY = 0;
            Rectangle spawnBox = new Rectangle(spawnX, spawnY, GameConfig.TANK_SIZE, GameConfig.TANK_SIZE);
            boolean occupied = false;
            if (playerTank.isAlive() && spawnBox.intersects(playerTank.getBoundingBox())) occupied = true;
            for (EnemyTank e : enemies) {
                if (e.isAlive() && spawnBox.intersects(e.getBoundingBox())) { occupied = true; break; }
            }
            if (!occupied) {
                EnemyType type = (nextEnemyTypeIndex < enemyTypeSequence.length)
                        ? enemyTypeSequence[nextEnemyTypeIndex++] : EnemyType.NORMAL;
                EnemyTank enemy = new EnemyTank(spawnX, spawnY, type);
                enemies.add(enemy);
                spawnedEnemies++;
            }
        }
    }

    private void updatePowerUps(long currentTime) {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            if (it.next().isExpired(currentTime)) it.remove();
        }

        if (killedInLevel > 0 && killedInLevel % GameConfig.POWERUP_SPAWN_INTERVAL == 0 && powerUps.isEmpty()) {
            for (int attempt = 0; attempt < 10; attempt++) {
                int col = random.nextInt(GameConfig.MAP_SIZE - 2) + 1;
                int row = random.nextInt(GameConfig.MAP_SIZE - 2) + 1;
                if (map.getTile(col, row) == TileType.EMPTY) {
                    PowerUpType[] types = PowerUpType.values();
                    powerUps.add(new PowerUp(col * GameConfig.TILE_SIZE, row * GameConfig.TILE_SIZE,
                            types[random.nextInt(types.length)], currentTime));
                    break;
                }
            }
        }
    }

    private void cleanupExplosions(long currentTime) {
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            if (it.next().isFinished(currentTime)) it.remove();
        }
    }

    private void checkWinLoseConditions() {
        if (base.isDestroyed()) {
            stateMachine.onGameOver();
            audioManager.play(SoundEffect.GAME_OVER);
            return;
        }
        if (!playerTank.isAlive() && stateMachine.getPlayerLives() <= 0) {
            stateMachine.onGameOver();
            audioManager.play(SoundEffect.GAME_OVER);
            return;
        }
        boolean allEnemiesDead = true;
        for (EnemyTank e : enemies) {
            if (e.isAlive()) { allEnemiesDead = false; break; }
        }
        int total = (currentLevelData != null) ? currentLevelData.getTotalEnemies() : 10;
        if (spawnedEnemies >= total && allEnemiesDead) {
            audioManager.play(SoundEffect.LEVEL_COMPLETE);
            stateMachine.onLevelComplete();
            levelTransitionStart = System.currentTimeMillis();
        }
    }

    public void render(Graphics2D g) {
        GameStatus status = stateMachine.getCurrentStatus();

        if (status == GameStatus.MENU) {
            menuRenderer.render(g);
            return;
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        if (map != null) gameRenderer.renderMap(g, map);
        if (base != null) gameRenderer.renderBase(g, base);

        if (playerTank != null && playerTank.isAlive()) gameRenderer.renderPlayerTank(g, playerTank);
        for (EnemyTank enemy : enemies) {
            if (enemy.isAlive()) gameRenderer.renderEnemyTank(g, enemy);
        }
        if (playerTank != null && playerTank.getBullet() != null) {
            gameRenderer.renderBullet(g, playerTank.getBullet());
        }
        for (EnemyTank enemy : enemies) {
            if (enemy.getBullet() != null && enemy.getBullet().isActive()) {
                gameRenderer.renderBullet(g, enemy.getBullet());
            }
        }
        for (PowerUp pu : powerUps) {
            gameRenderer.renderPowerUp(g, pu);
        }
        gameRenderer.renderExplosions(g, explosions);
        if (map != null) gameRenderer.renderForestLayer(g, map);

        int activeEnemies = 0;
        for (EnemyTank e : enemies) if (e.isAlive()) activeEnemies++;
        hudRenderer.render(g, stateMachine, activeEnemies);

        if (status == GameStatus.PAUSED) {
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRect(0, 0, GameConfig.GAME_AREA_SIZE, GameConfig.GAME_AREA_SIZE);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g.getFontMetrics();
            String text = "暂停";
            g.drawString(text, (GameConfig.GAME_AREA_SIZE - fm.stringWidth(text)) / 2,
                    GameConfig.GAME_AREA_SIZE / 2);
        }

        if (status == GameStatus.LEVEL_TRANSITION) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, GameConfig.GAME_AREA_SIZE, GameConfig.GAME_AREA_SIZE);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String text = "关卡 " + stateMachine.getCurrentLevel() + " 完成！";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (GameConfig.GAME_AREA_SIZE - fm.stringWidth(text)) / 2,
                    GameConfig.GAME_AREA_SIZE / 2);
        }

        if (status == GameStatus.GAME_OVER) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, GameConfig.GAME_AREA_SIZE, GameConfig.GAME_AREA_SIZE);
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g.getFontMetrics();
            String text = "游戏结束";
            g.drawString(text, (GameConfig.GAME_AREA_SIZE - fm.stringWidth(text)) / 2,
                    GameConfig.GAME_AREA_SIZE / 2 - 30);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String scoreText = "分数: " + stateMachine.getScore();
            fm = g.getFontMetrics();
            g.drawString(scoreText, (GameConfig.GAME_AREA_SIZE - fm.stringWidth(scoreText)) / 2,
                    GameConfig.GAME_AREA_SIZE / 2 + 30);
        }

        if (status == GameStatus.VICTORY) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, GameConfig.GAME_AREA_SIZE, GameConfig.GAME_AREA_SIZE);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g.getFontMetrics();
            String text = "恭喜通关！";
            g.drawString(text, (GameConfig.GAME_AREA_SIZE - fm.stringWidth(text)) / 2,
                    GameConfig.GAME_AREA_SIZE / 2 - 30);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String scoreText = "总分: " + stateMachine.getScore();
            fm = g.getFontMetrics();
            g.drawString(scoreText, (GameConfig.GAME_AREA_SIZE - fm.stringWidth(scoreText)) / 2,
                    GameConfig.GAME_AREA_SIZE / 2 + 30);
        }
    }

    public void setLastTime(long lastTime) { this.lastTime = lastTime; }
    public GameStateMachine getStateMachine() { return stateMachine; }
    public AudioManager getAudioManager() { return audioManager; }
}