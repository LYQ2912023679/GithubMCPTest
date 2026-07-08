package com.tankbattle.model;

import com.tankbattle.view.Renderable;

import java.awt.Graphics2D;
import java.awt.Color;

public class Explosion implements Updatable, Renderable {
    private int x;
    private int y;
    private long startTime;
    private long duration;
    private boolean big;

    public Explosion(int x, int y, long startTime, long duration, boolean big) {
        this.x = x;
        this.y = y;
        this.startTime = startTime;
        this.duration = duration;
        this.big = big;
    }

    public boolean isFinished(long currentTime) {
        return currentTime - startTime >= duration;
    }

    @Override
    public void update(long deltaTime) {}

    @Override
    public boolean isActive() { return true; }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.ORANGE);
        int size = big ? 40 : 20;
        g.fillOval(x - size / 2, y - size / 2, size, size);
        g.setColor(Color.YELLOW);
        int inner = size / 2;
        g.fillOval(x - inner / 2, y - inner / 2, inner, inner);
    }

    @Override
    public int getZOrder() { return 100; }

    public int getX() { return x; }
    public int getY() { return y; }
    public long getStartTime() { return startTime; }
    public long getDuration() { return duration; }
    public boolean isBig() { return big; }
}