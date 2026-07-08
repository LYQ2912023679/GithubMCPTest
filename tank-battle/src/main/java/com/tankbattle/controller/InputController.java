package com.tankbattle.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class InputController implements KeyListener, InputHandler {
    private Set<KeyCode> pressedKeys = new HashSet<>();
    private Set<KeyCode> keyDownEvents = new HashSet<>();
    private boolean escPressed = false;

    @Override
    public void onKeyDown(KeyCode code) {
        pressedKeys.add(code);
        keyDownEvents.add(code);
    }

    @Override
    public void onKeyUp(KeyCode code) {
        pressedKeys.remove(code);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        KeyCode code = mapKeyEvent(e);
        if (code != null) {
            if (code == KeyCode.ESC) {
                escPressed = true;
            } else {
                onKeyDown(code);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        KeyCode code = mapKeyEvent(e);
        if (code != null && code != KeyCode.ESC) {
            onKeyUp(code);
        }
    }

    private KeyCode mapKeyEvent(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: return KeyCode.UP;
            case KeyEvent.VK_DOWN: return KeyCode.DOWN;
            case KeyEvent.VK_LEFT: return KeyCode.LEFT;
            case KeyEvent.VK_RIGHT: return KeyCode.RIGHT;
            case KeyEvent.VK_SPACE: return KeyCode.FIRE;
            case KeyEvent.VK_ESCAPE: return KeyCode.ESC;
            case KeyEvent.VK_W: return KeyCode.UP;
            case KeyEvent.VK_S: return KeyCode.DOWN;
            case KeyEvent.VK_A: return KeyCode.LEFT;
            case KeyEvent.VK_D: return KeyCode.RIGHT;
            default: return null;
        }
    }

    public boolean isPressed(KeyCode code) { return pressedKeys.contains(code); }
    public boolean consumeEsc() {
        if (escPressed) {
            escPressed = false;
            return true;
        }
        return false;
    }
    public void clearFrameEvents() { keyDownEvents.clear(); }
}