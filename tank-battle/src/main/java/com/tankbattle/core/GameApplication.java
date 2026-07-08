package com.tankbattle.core;

import com.tankbattle.controller.InputController;
import com.tankbattle.view.GamePanel;
import com.tankbattle.config.GameConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameApplication {
    private static final Logger logger = Logger.getLogger(GameApplication.class.getName());

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Logger.getLogger(GameApplication.class.getName()).log(Level.SEVERE,
                    "Uncaught exception in thread " + t.getName(), e);
        });

        SwingUtilities.invokeLater(() -> {
            try {
                new GameApplication().start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to start game", e);
            }
        });
    }

    public void start() {
        logger.info("Starting Tank Battle game...");

        JFrame frame = new JFrame("坦克大战");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        GameStateMachine stateMachine = new GameStateMachine();
        InputController inputController = new InputController();
        GameLoop gameLoop = new GameLoop(stateMachine, inputController);

        GamePanel gamePanel = new GamePanel();
        gamePanel.setGameLoop(gameLoop);
        gamePanel.addKeyListener(inputController);

        frame.add(gamePanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        gamePanel.requestFocusInWindow();

        gameLoop.setLastTime(System.currentTimeMillis());

        Timer timer = new Timer(GameConfig.FRAME_INTERVAL, e -> {
            gameLoop.update();
            gamePanel.repaint();
        });
        timer.start();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                timer.stop();
                gameLoop.getAudioManager().stopAll();
                logger.info("Game closed.");
            }
        });

        logger.info("Game started successfully.");
    }
}