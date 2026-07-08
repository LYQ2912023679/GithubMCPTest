package com.tankbattle.controller;

import com.tankbattle.model.*;
import com.tankbattle.config.GameConfig;

import java.awt.Rectangle;
import java.util.List;
import java.util.logging.Logger;

public class CollisionDetector {
    private static final Logger logger = Logger.getLogger(CollisionDetector.class.getName());

    public void detectBulletMapCollision(Bullet bullet, GameMap map, List<Explosion> explosions, long currentTime) {
        if (!bullet.isActive()) return;
        int ts = GameConfig.TILE_SIZE;
        int step = GameConfig.BULLET_STEP_SIZE;
        int steps = Math.max(1, bullet.getSpeed() / step);
        int dx = bullet.getDirection().dx();
        int dy = bullet.getDirection().dy();
        for (int s = 0; s < steps; s++) {
            int checkX = bullet.getX() + dx * s * step;
            int checkY = bullet.getY() + dy * s * step;
            int col = checkX / ts;
            int row = checkY / ts;
            if (col < 0 || col >= GameConfig.MAP_SIZE || row < 0 || row >= GameConfig.MAP_SIZE) {
                bullet.deactivate();
                return;
            }
            TileType tile = map.getTile(col, row);
            if (tile == TileType.BRICK) {
                map.removeTile(col, row);
                bullet.deactivate();
                explosions.add(new Explosion(col * ts + ts / 2, row * ts + ts / 2, currentTime, 300, false));
                return;
            } else if (tile == TileType.STEEL) {
                if (bullet.isEnhanced()) {
                    map.removeTile(col, row);
                    explosions.add(new Explosion(col * ts + ts / 2, row * ts + ts / 2, currentTime, 300, false));
                }
                bullet.deactivate();
                return;
            } else if (tile == TileType.BASE) {
                bullet.deactivate();
                return;
            }
        }
    }

    public EnemyTank detectBulletEnemyCollision(Bullet bullet, List<EnemyTank> enemies) {
        if (!bullet.isActive() || !bullet.isFromPlayer()) return null;
        Rectangle bulletBox = bullet.getBoundingBox();
        for (EnemyTank enemy : enemies) {
            if (!enemy.isAlive()) continue;
            if (bulletBox.intersects(enemy.getBoundingBox())) {
                return enemy;
            }
        }
        return null;
    }

    public boolean detectBulletPlayerCollision(Bullet bullet, PlayerTank player) {
        if (!bullet.isActive() || bullet.isFromPlayer() || !player.isAlive()) return false;
        if (player.isShielded()) return false;
        return bullet.getBoundingBox().intersects(player.getBoundingBox());
    }

    public boolean detectBulletBaseCollision(Bullet bullet, Base base) {
        if (!bullet.isActive() || base.isDestroyed()) return false;
        return bullet.getBoundingBox().intersects(base.getBoundingBox());
    }

    public boolean detectBulletBulletCollision(Bullet b1, Bullet b2) {
        if (!b1.isActive() || !b2.isActive()) return false;
        if (b1.isFromPlayer() == b2.isFromPlayer()) return false;
        return b1.getBoundingBox().intersects(b2.getBoundingBox());
    }

    public boolean detectTankCollision(Tank moving, int newX, int newY, Tank other) {
        if (other == moving || !other.isAlive()) return false;
        Rectangle newBox = new Rectangle(newX, newY, GameConfig.TANK_SIZE, GameConfig.TANK_SIZE);
        return newBox.intersects(other.getBoundingBox());
    }

    public PowerUp detectPlayerPowerUpCollision(PlayerTank player, List<PowerUp> powerUps) {
        if (!player.isAlive()) return null;
        Rectangle playerBox = player.getBoundingBox();
        for (PowerUp pu : powerUps) {
            if (playerBox.intersects(pu.getBoundingBox())) {
                return pu;
            }
        }
        return null;
    }
}