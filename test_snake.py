import pytest
from snake import Snake
from settings import (
    GRID_W, GRID_H, INITIAL_SNAKE_LENGTH,
    UP, DOWN, LEFT, RIGHT, OPPOSITE,
)


class TestSnakeInit:
    def test_initial_length(self):
        s = Snake()
        assert len(s.body) == INITIAL_SNAKE_LENGTH

    def test_initial_direction_right(self):
        s = Snake()
        assert s.direction == RIGHT

    def test_head_at_center(self):
        s = Snake()
        cx, cy = GRID_W // 2, GRID_H // 2
        assert s.head() == (cx, cy)

    def test_body_horizontal_at_center(self):
        s = Snake()
        cy = GRID_H // 2
        for x, y in s.body:
            assert y == cy

    def test_grow_pending_false(self):
        s = Snake()
        assert s.grow_pending is False


class TestSnakeDirection:
    def test_set_valid_direction(self):
        s = Snake()
        s.set_direction(UP)
        s.move()
        assert s.direction == UP

    def test_set_opposite_direction_ignored(self):
        s = Snake()
        s.set_direction(LEFT)
        s.move()
        assert s.direction == RIGHT

    def test_set_direction_then_90_turn(self):
        s = Snake()
        s.set_direction(UP)
        s.move()
        s.set_direction(LEFT)
        s.move()
        assert s.direction == LEFT

    def test_pending_dir_not_applied_until_move(self):
        s = Snake()
        s.set_direction(UP)
        assert s.direction == RIGHT
        s.move()
        assert s.direction == UP


class TestSnakeMove:
    def test_move_right(self):
        s = Snake()
        old_head = s.head()
        s.move()
        assert s.head() == (old_head[0] + 1, old_head[1])

    def test_move_up(self):
        s = Snake()
        s.set_direction(UP)
        s.move()
        old_head = s.head()
        s.move()
        assert s.head() == (old_head[0], old_head[1] - 1)

    def test_move_preserves_length(self):
        s = Snake()
        initial_len = len(s.body)
        s.move()
        assert len(s.body) == initial_len

    def test_grow_increases_length(self):
        s = Snake()
        initial_len = len(s.body)
        s.grow()
        s.move()
        assert len(s.body) == initial_len + 1

    def test_grow_only_one_step(self):
        s = Snake()
        s.grow()
        s.move()
        len_after_grow = len(s.body)
        s.move()
        assert len(s.body) == len_after_grow


class TestSnakeCollision:
    def test_no_self_collision_initial(self):
        s = Snake()
        assert s.self_collision() is False

    def test_hits_wall_left(self):
        s = Snake()
        s.set_direction(LEFT)
        for _ in range(GRID_W // 2 + 1):
            s.move()
            if s.hits_wall():
                break
        assert s.hits_wall() is True

    def test_no_wall_hit_initial(self):
        s = Snake()
        assert s.hits_wall() is False

    def test_collides_with_head(self):
        s = Snake()
        assert s.collides_with(s.head()) is True

    def test_not_collide_with_other(self):
        s = Snake()
        assert s.collides_with((0, 0)) is False or (0, 0) == s.head()
