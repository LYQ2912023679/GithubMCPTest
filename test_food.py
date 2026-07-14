import pytest
from food import Food
from snake import Snake
from settings import GRID_W, GRID_H


class TestFoodInit:
    def test_initial_position(self):
        f = Food()
        assert f.position == (GRID_W // 4, GRID_H // 4)

    def test_position_in_bounds(self):
        f = Food()
        x, y = f.position
        assert 0 <= x < GRID_W
        assert 0 <= y < GRID_H


class TestFoodRespawn:
    def test_respawn_returns_true_with_space(self):
        f = Food()
        s = Snake()
        assert f.respawn(s.body) is True

    def test_respawn_avoids_snake_body(self):
        f = Food()
        s = Snake()
        f.respawn(s.body)
        assert f.position not in set(s.body)

    def test_respawn_in_bounds(self):
        f = Food()
        s = Snake()
        f.respawn(s.body)
        x, y = f.position
        assert 0 <= x < GRID_W
        assert 0 <= y < GRID_H

    def test_respawn_false_when_full(self):
        from collections import deque
        f = Food()
        full_body = deque((x, y) for x in range(GRID_W) for y in range(GRID_H))
        assert f.respawn(full_body) is False
