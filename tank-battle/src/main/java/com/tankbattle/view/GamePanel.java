package com.tankbattle.view;

import com.tankbattle.core.GameLoop;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private GameLoop gameLoop;

    public GamePanel() {
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(
                com.tankbattle.config.GameConfig.WINDOW_WIDTH,
                com.tankbattle.config.GameConfig.WINDOW_HEIGHT));
        setFocusable(true);
    }

    public void setGameLoop(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameLoop != null) {
            gameLoop.render((Graphics2D) g);
        }
    }
}