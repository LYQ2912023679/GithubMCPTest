package com.tankbattle.view;

import com.tankbattle.model.*;
import com.tankbattle.config.GameConfig;

import java.awt.*;
import java.util.List;

public class GameRenderer {
    public void renderMap(Graphics2D g, GameMap map) {
        int ts = GameConfig.TILE_SIZE;
        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                TileType tile = map.getTile(col, row);
                if (tile == TileType.EMPTY) continue;
                int x = col * ts;
                int y = row * ts;
                switch (tile) {
                    case BRICK:
                        g.setColor(new Color(139, 69, 19));
                        g.fillRect(x, y, ts, ts);
                        g.setColor(new Color(160, 82, 45));
                        g.fillRect(x + 1, y + 1, ts / 2 - 1, ts / 2 - 1);
                        g.fillRect(x + ts / 2 + 1, y + ts / 2 + 1, ts / 2 - 2, ts / 2 - 2);
                        break;
                    case STEEL:
                        g.setColor(new Color(192, 192, 192));
                        g.fillRect(x, y, ts, ts);
                        g.setColor(Color.GRAY);
                        g.drawRect(x, y, ts - 1, ts - 1);
                        break;
                    case WATER:
                        g.setColor(new Color(0, 0, 180));
                        g.fillRect(x, y, ts, ts);
                        g.setColor(new Color(30, 30, 220));
                        g.drawLine(x, y + ts / 2, x + ts, y + ts / 2);
                        break;
                    case FOREST:
                        g.setColor(new Color(0, 100, 0));
                        g.fillRect(x, y, ts, ts);
                        break;
                    case BASE:
                        g.setColor(Color.RED);
                        g.fillRect(x, y, ts, ts);
                        g.setColor(Color.WHITE);
                        g.drawLine(x + 2, y + 2, x + ts - 3, y + ts - 3);
                        g.drawLine(x + ts - 3, y + 2, x + 2, y + ts - 3);
                        break;
                }
            }
        }
    }

    public void renderForestLayer(Graphics2D g, GameMap map) {
        int ts = GameConfig.TILE_SIZE;
        for (int row = 0; row < map.getHeight(); row++) {
            for (int col = 0; col < map.getWidth(); col++) {
                if (map.getTile(col, row) == TileType.FOREST) {
                    g.setColor(new Color(0, 100, 0));
                    g.fillRect(col * ts, row * ts, ts, ts);
                    g.setColor(new Color(0, 70, 0));
                    g.fillOval(col * ts + 2, row * ts + 2, ts - 4, ts - 4);
                }
            }
        }
    }

    public void renderPlayerTank(Graphics2D g, PlayerTank tank) {
        if (!tank.isAlive()) return;
        int x = tank.getX();
        int y = tank.getY();
        int size = GameConfig.TANK_SIZE;
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, size, size);
        g.setColor(new Color(200, 200, 0));
        g.fillRect(x + 4, y + 4, size - 8, size - 8);
        g.setColor(Color.YELLOW);
        drawTurret(g, x, y, size, tank.getDirection());
        if (tank.isShielded()) {
            g.setColor(new Color(0, 200, 255, 100));
            g.drawOval(x - 4, y - 4, size + 8, size + 8);
            g.drawOval(x - 2, y - 2, size + 4, size + 4);
        }
    }

    public void renderEnemyTank(Graphics2D g, EnemyTank tank) {
        if (!tank.isAlive()) return;
        int x = tank.getX();
        int y = tank.getY();
        int size = GameConfig.TANK_SIZE;
        Color bodyColor;
        switch (tank.getType()) {
            case NORMAL: bodyColor = Color.GRAY; break;
            case FAST: bodyColor = new Color(180, 180, 180); break;
            case ARMORED: bodyColor = new Color(100, 100, 200); break;
            default: bodyColor = Color.GRAY;
        }
        if (tank.isFrozen()) {
            long flash = System.currentTimeMillis() % 400;
            if (flash < 200) bodyColor = Color.WHITE;
        }
        g.setColor(bodyColor);
        g.fillRect(x, y, size, size);
        g.setColor(bodyColor.darker());
        g.fillRect(x + 4, y + 4, size - 8, size - 8);
        g.setColor(bodyColor);
        drawTurret(g, x, y, size, tank.getDirection());
    }

    private void drawTurret(Graphics2D g, int x, int y, int size, Direction dir) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        g.setColor(Color.DARK_GRAY);
        switch (dir) {
            case UP: g.fillRect(cx - 3, y - 4, 6, size / 2 + 4); break;
            case DOWN: g.fillRect(cx - 3, cy, 6, size / 2 + 4); break;
            case LEFT: g.fillRect(x - 4, cy - 3, size / 2 + 4, 6); break;
            case RIGHT: g.fillRect(cx, cy - 3, size / 2 + 4, 6); break;
        }
    }

    public void renderBullet(Graphics2D g, Bullet bullet) {
        if (!bullet.isActive()) return;
        g.setColor(bullet.isFromPlayer() ? Color.WHITE : Color.RED);
        g.fillRect(bullet.getX(), bullet.getY(), 4, 4);
    }

    public void renderBase(Graphics2D g, Base base) {
        int x = base.getX();
        int y = base.getY();
        int size = base.getSize();
        if (base.isDestroyed()) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, size, size);
        } else {
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, size, size);
            g.setColor(Color.RED);
            int cx = x + size / 2;
            int cy = y + size / 2;
            int r = size / 2 - 4;
            g.fillOval(cx - r, cy - r, r * 2, r * 2);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("★", cx - 5, cy + 5);
        }
    }

    public void renderPowerUp(Graphics2D g, PowerUp powerUp) {
        int x = powerUp.getX();
        int y = powerUp.getY();
        int size = GameConfig.TILE_SIZE * 2;
        g.setColor(Color.RED);
        g.fillRect(x, y, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String label;
        switch (powerUp.getType()) {
            case STAR: label = "★"; break;
            case SHIELD: label = "S"; break;
            case BOMB: label = "B"; break;
            case CLOCK: label = "C"; break;
            default: label = "?";
        }
        g.drawString(label, x + size / 2 - 5, y + size / 2 + 5);
    }

    public void renderExplosions(Graphics2D g, List<Explosion> explosions) {
        for (Explosion exp : explosions) {
            exp.render(g);
        }
    }
}