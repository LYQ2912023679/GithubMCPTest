from collections import deque
from typing import Deque

from settings import (
    Point, RIGHT, LEFT, OPPOSITE,
    GRID_W, GRID_H, INITIAL_SNAKE_LENGTH,
)


class Snake:
    def __init__(self) -> None:
        cx, cy = GRID_W // 2, GRID_H // 2
        self.body: Deque[Point] = deque()
        for i in range(INITIAL_SNAKE_LENGTH):
            self.body.append((cx - i, cy))
        self.direction: Point = RIGHT
        self._pending_dir: Point = RIGHT
        self.grow_pending: bool = False

    def set_direction(self, new_dir: Point) -> None:
        if new_dir == OPPOSITE.get(self.direction):
            return
        self._pending_dir = new_dir

    def move(self) -> None:
        self.direction = self._pending_dir
        hx, hy = self.body[0]
        dx, dy = self.direction
        new_head: Point = (hx + dx, hy + dy)
        self.body.appendleft(new_head)
        if self.grow_pending:
            self.grow_pending = False
        else:
            self.body.pop()

    def grow(self) -> None:
        self.grow_pending = True

    def head(self) -> Point:
        return self.body[0]

    def collides_with(self, point: Point) -> bool:
        return self.body[0] == point

    def self_collision(self) -> bool:
        return self.body[0] in list(self.body)[1:]

    def hits_wall(self) -> bool:
        hx, hy = self.body[0]
        return hx < 0 or hx >= GRID_W or hy < 0 or hy >= GRID_H
