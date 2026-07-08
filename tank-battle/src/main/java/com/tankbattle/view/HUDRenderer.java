package com.tankbattle.view;

import com.tankbattle.core.GameStateMachine;
import com.tankbattle.config.GameConfig;

import java.awt.*;

public class HUDRenderer {
    public void render(Graphics2D g, GameStateMachine stateMachine, int activeEnemyCount) {
        int hudX = GameConfig.GAME_AREA_SIZE;
        int hudWidth = GameConfig.HUD_WIDTH;
        g.setColor(Color.BLACK);
        g.fillRect(hudX, 0, hudWidth, GameConfig.WINDOW_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));

        int y = 20;
        g.drawString("关卡", hudX + 10, y);
        g.drawString(String.valueOf(stateMachine.getCurrentLevel()), hudX + 60, y);
        y += 30;

        g.drawString("分数", hudX + 10, y);
        g.drawString(String.valueOf(stateMachine.getScore()), hudX + 60, y);
        y += 30;

        g.drawString("生命", hudX + 10, y);
        g.drawString(String.valueOf(stateMachine.getPlayerLives()), hudX + 60, y);
        y += 30;

        g.drawString("敌人", hudX + 10, y);
        g.drawString(String.valueOf(stateMachine.getRemainingEnemies()), hudX + 60, y);
        y += 40;

        int iconSize = 12;
        for (int i = 0; i < stateMachine.getRemainingEnemies(); i++) {
            int ix = hudX + 10 + (i % 5) * (iconSize + 2);
            int iy = y + (i / 5) * (iconSize + 2);
            g.setColor(Color.GRAY);
            g.fillRect(ix, iy, iconSize, iconSize);
        }
    }
}