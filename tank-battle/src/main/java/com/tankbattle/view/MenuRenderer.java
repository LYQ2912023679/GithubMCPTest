package com.tankbattle.view;

import com.tankbattle.config.GameConfig;

import java.awt.*;

public class MenuRenderer {
    private int selectedOption = 0;
    private String[] options = {"开始游戏", "退出游戏"};

    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "坦克大战";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (GameConfig.WINDOW_WIDTH - titleWidth) / 2, 150);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < options.length; i++) {
            if (i == selectedOption) {
                g.setColor(Color.WHITE);
                g.drawString("▶", (GameConfig.WINDOW_WIDTH - 180) / 2 - 30, 300 + i * 50);
            } else {
                g.setColor(Color.GRAY);
            }
            fm = g.getFontMetrics();
            int w = fm.stringWidth(options[i]);
            g.drawString(options[i], (GameConfig.WINDOW_WIDTH - w) / 2, 300 + i * 50);
        }
    }

    public void moveUp() { selectedOption = (selectedOption - 1 + options.length) % options.length; }
    public void moveDown() { selectedOption = (selectedOption + 1) % options.length; }
    public int getSelectedOption() { return selectedOption; }
    public void reset() { selectedOption = 0; }
}