from typing import Tuple, Dict
from enum import IntEnum

WINDOW_WIDTH: int = 600
WINDOW_HEIGHT: int = 600
GRID_SIZE: int = 20
GRID_W: int = WINDOW_WIDTH // GRID_SIZE
GRID_H: int = WINDOW_HEIGHT // GRID_SIZE

SNAKE_SPEED: float = 0.15
MIN_SNAKE_SPEED: float = 0.05
FPS: int = 60
INITIAL_SNAKE_LENGTH: int = 3
SCORE_PER_FOOD: int = 10
SPEED_UP_INTERVAL: int = 5


class Difficulty(IntEnum):
    EASY = 0
    MEDIUM = 1
    HARD = 2


DIFFICULTY_CONFIG: Dict[Difficulty, Dict[str, float]] = {
    Difficulty.EASY: {
        "speed": 0.20,
        "min_speed": 0.10,
        "speed_up_interval": 8,
        "speed_up_factor": 0.97,
    },
    Difficulty.MEDIUM: {
        "speed": 0.15,
        "min_speed": 0.06,
        "speed_up_interval": 5,
        "speed_up_factor": 0.95,
    },
    Difficulty.HARD: {
        "speed": 0.10,
        "min_speed": 0.04,
        "speed_up_interval": 3,
        "speed_up_factor": 0.92,
    },
}

DIFFICULTY_LABELS: Dict[Difficulty, str] = {
    Difficulty.EASY: "简单",
    Difficulty.MEDIUM: "中等",
    Difficulty.HARD: "困难",
}

COLOR_BG: Tuple[int, int, int] = (0, 0, 0)
COLOR_SNAKE: Tuple[int, int, int] = (0, 255, 0)
COLOR_SNAKE_HEAD: Tuple[int, int, int] = (0, 200, 0)
COLOR_FOOD: Tuple[int, int, int] = (255, 0, 0)
COLOR_TEXT: Tuple[int, int, int] = (255, 255, 255)
COLOR_PAUSE: Tuple[int, int, int] = (128, 128, 128)

HIGHSCORE_FILE: str = "highscore.json"

Point = Tuple[int, int]

UP: Point = (0, -1)
DOWN: Point = (0, 1)
LEFT: Point = (-1, 0)
RIGHT: Point = (1, 0)

OPPOSITE: Dict[Point, Point] = {
    UP: DOWN,
    DOWN: UP,
    LEFT: RIGHT,
    RIGHT: LEFT,
}

GAME_TITLE: str = "贪吃蛇游戏"
