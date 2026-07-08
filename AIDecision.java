package com.tankbattle.ai;

import com.tankbattle.model.Direction;
import com.tankbattle.model.EnemyTank;
import com.tankbattle.model.GameMap;
import com.tankbattle.model.Base;

public interface AIDecision {
    Direction decideMovement(EnemyTank tank, GameMap map, Base base);
    boolean decideShooting(EnemyTank tank);
}