package com.tankbattle.ai;

import com.tankbattle.model.Direction;
import com.tankbattle.model.EnemyTank;
import com.tankbattle.model.GameMap;
import com.tankbattle.model.Base;

public class MovementStrategy {
    public Direction decideMovement(EnemyTank tank, GameMap map, Base base) {
        if (base == null || base.isDestroyed()) {
            return randomDirection();
        }
        if (Math.random() < 0.5) {
            return directionTowardBase(tank, base);
        }
        return randomDirection();
    }

    private Direction directionTowardBase(EnemyTank tank, Base base) {
        int dx = base.getX() - tank.getX();
        int dy = base.getY() - tank.getY();
        if (dx == 0 && dy == 0) {
            return randomDirection();
        }
        if (Math.abs(dy) > Math.abs(dx)) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        } else {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        }
    }

    private Direction randomDirection() {
        Direction[] dirs = Direction.values();
        return dirs[(int) (Math.random() * dirs.length)];
    }
}