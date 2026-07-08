package com.tankbattle.ai;

import com.tankbattle.model.Direction;
import com.tankbattle.model.EnemyTank;
import com.tankbattle.model.GameMap;
import com.tankbattle.model.Base;

public class EnemyAI implements AIDecision {
    private MovementStrategy movementStrategy;
    private ShootingStrategy shootingStrategy;

    public EnemyAI() {
        this.movementStrategy = new MovementStrategy();
        this.shootingStrategy = new ShootingStrategy();
    }

    @Override
    public Direction decideMovement(EnemyTank tank, GameMap map, Base base) {
        if (tank.isFrozen()) return tank.getDirection();
        return movementStrategy.decideMovement(tank, map, base);
    }

    @Override
    public boolean decideShooting(EnemyTank tank) {
        return shootingStrategy.decideShooting(tank);
    }
}