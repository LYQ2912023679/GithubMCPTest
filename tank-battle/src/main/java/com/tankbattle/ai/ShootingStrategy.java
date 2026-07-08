package com.tankbattle.ai;

import com.tankbattle.model.EnemyTank;

public class ShootingStrategy {
    public boolean decideShooting(EnemyTank tank) {
        if (tank.isFrozen()) return false;
        return true;
    }
}