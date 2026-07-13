import random
from typing import Deque, List

from settings import Point, GRID_W, GRID_H


class Food:
    def __init__(self) -> None:
        self._position: Point = (GRID_W // 4, GRID_H // 4)

    def respawn(self, snake_body: Deque[Point]) -> bool:
        occupied = set(snake_body)
        free_cells: List[Point] = [
            (x, y)
            for x in range(GRID_W)
            for y in range(GRID_H)
            if (x, y) not in occupied
        ]
        if not free_cells:
            return False
        self._position = random.choice(free_cells)
        return True

    @property
    def position(self) -> Point:
        return self._position
