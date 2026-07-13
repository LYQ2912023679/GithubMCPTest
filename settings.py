from typing import Tuple, Dict

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
