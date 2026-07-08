package com.tankbattle.view;

import java.awt.Graphics2D;

public interface Renderable {
    void render(Graphics2D g);
    int getZOrder();
}