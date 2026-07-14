import pytest
from settings import (
    WINDOW_WIDTH, WINDOW_HEIGHT, GRID_SIZE, GRID_W, GRID_H,
    SNAKE_SPEED, MIN_SNAKE_SPEED, FPS, INITIAL_SNAKE_LENGTH,
    SCORE_PER_FOOD, SPEED_UP_INTERVAL,
    UP, DOWN, LEFT, RIGHT, OPPOSITE,
    COLOR_BG, COLOR_SNAKE, COLOR_FOOD,
    GAME_TITLE, Difficulty, DIFFICULTY_CONFIG,
)


class TestWindowSettings:
    def test_window_dimensions(self):
        assert WINDOW_WIDTH == 600
        assert WINDOW_HEIGHT == 600

    def test_grid_dimensions(self):
        assert GRID_W == WINDOW_WIDTH // GRID_SIZE
        assert GRID_H == WINDOW_HEIGHT // GRID_SIZE

    def test_game_title(self):
        assert GAME_TITLE == "贪吃蛇游戏"


class TestDirections:
    def test_up(self):
        assert UP == (0, -1)

    def test_down(self):
        assert DOWN == (0, 1)

    def test_left(self):
        assert LEFT == (-1, 0)

    def test_right(self):
        assert RIGHT == (1, 0)

    def test_opposite_up_down(self):
        assert OPPOSITE[UP] == DOWN

    def test_opposite_left_right(self):
        assert OPPOSITE[LEFT] == RIGHT


class TestColors:
    def test_background_black(self):
        assert COLOR_BG == (0, 0, 0)

    def test_snake_green(self):
        assert COLOR_SNAKE[1] > 0

    def test_food_red(self):
        assert COLOR_FOOD[0] > 0

    def test_food_not_same_as_bg(self):
        assert COLOR_FOOD != COLOR_BG


class TestDifficulty:
    def test_difficulty_count(self):
        assert len(Difficulty) == 3

    def test_all_difficulties_have_config(self):
        for diff in Difficulty:
            assert diff in DIFFICULTY_CONFIG

    def test_easy_slower_than_hard(self):
        assert DIFFICULTY_CONFIG[Difficulty.EASY]["speed"] > DIFFICULTY_CONFIG[Difficulty.HARD]["speed"]
