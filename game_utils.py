import json
import os
import pygame
from typing import Optional

from settings import (
    Point, GRID_SIZE, WINDOW_WIDTH, WINDOW_HEIGHT,
    COLOR_BG, COLOR_SNAKE, COLOR_SNAKE_HEAD, COLOR_FOOD, COLOR_TEXT, COLOR_PAUSE,
    HIGHSCORE_FILE, GAME_TITLE,
)
from snake import Snake
from food import Food


class ScoreManager:
    def __init__(self) -> None:
        self._score: int = 0
        self._high_score: int = 0
        self._load()

    def add(self, points: int) -> None:
        self._score += points

    def reset(self) -> None:
        self._score = 0

    @property
    def current_score(self) -> int:
        return self._score

    @property
    def high_score(self) -> int:
        return self._high_score

    def update_high_score(self) -> bool:
        if self._score > self._high_score:
            self._high_score = self._score
            self._save()
            return True
        return False

    def _load(self) -> None:
        try:
            with open(HIGHSCORE_FILE, "r", encoding="utf-8") as f:
                data = json.load(f)
                self._high_score = int(data.get("high_score", 0))
        except (FileNotFoundError, json.JSONDecodeError, ValueError, IOError):
            self._high_score = 0

    def _save(self) -> None:
        try:
            with open(HIGHSCORE_FILE, "w", encoding="utf-8") as f:
                json.dump({"high_score": self._high_score}, f)
        except (IOError, OSError):
            pass


class Renderer:
    def __init__(self, surface: pygame.Surface) -> None:
        self.surface = surface
        self.font_large = pygame.font.SysFont(
            "microsoftyahei,simhei,arial", 36, bold=True
        )
        self.font_medium = pygame.font.SysFont(
            "microsoftyahei,simhei,arial", 24, bold=True
        )
        self.font_small = pygame.font.SysFont(
            "microsoftyahei,simhei,arial", 18
        )

    def draw_background(self) -> None:
        self.surface.fill(COLOR_BG)

    def draw_snake(self, snake: Snake) -> None:
        for i, (x, y) in enumerate(snake.body):
            rect = pygame.Rect(
                x * GRID_SIZE, y * GRID_SIZE, GRID_SIZE, GRID_SIZE
            )
            color = COLOR_SNAKE_HEAD if i == 0 else COLOR_SNAKE
            pygame.draw.rect(self.surface, color, rect)
            pygame.draw.rect(self.surface, (0, 100, 0), rect, 1)

    def draw_food(self, food: Food) -> None:
        fx, fy = food.position
        rect = pygame.Rect(
            fx * GRID_SIZE, fy * GRID_SIZE, GRID_SIZE, GRID_SIZE
        )
        pygame.draw.rect(self.surface, COLOR_FOOD, rect)
        pygame.draw.rect(self.surface, (150, 0, 0), rect, 1)

    def draw_score(self, score: int, high: int) -> None:
        text = f"得分: {score}    最高分: {high}"
        rendered = self.font_small.render(text, True, COLOR_TEXT)
        self.surface.blit(rendered, (10, 5))

    def draw_text(
        self, text: str, pos: Point, size: int = 24, color: Optional[Point] = None
    ) -> None:
        if color is None:
            color = COLOR_TEXT
        font = self.font_medium if size == 24 else (
            self.font_large if size >= 36 else self.font_small
        )
        rendered = font.render(text, True, color)
        rect = rendered.get_rect(center=pos)
        self.surface.blit(rendered, rect)

    def draw_start_screen(self) -> None:
        self.draw_background()
        cx, cy = WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2
        self.draw_text(GAME_TITLE, (cx, cy - 40), size=48)
        self.draw_text("按任意键开始", (cx, cy + 20), size=24)
        self.draw_text(
            "方向键/WASD 移动 | 空格 暂停 | ESC 退出",
            (cx, cy + 70), size=18,
        )

    def draw_pause_overlay(self) -> None:
        overlay = pygame.Surface((WINDOW_WIDTH, WINDOW_HEIGHT), pygame.SRCALPHA)
        overlay.fill((0, 0, 0, 128))
        self.surface.blit(overlay, (0, 0))
        cx, cy = WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2
        self.draw_text("已暂停", (cx, cy), size=36)
        self.draw_text("按空格键继续", (cx, cy + 40), size=18)

    def draw_game_over(self, score: int, high: int) -> None:
        overlay = pygame.Surface((WINDOW_WIDTH, WINDOW_HEIGHT), pygame.SRCALPHA)
        overlay.fill((0, 0, 0, 180))
        self.surface.blit(overlay, (0, 0))
        cx, cy = WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2
        self.draw_text("游戏结束", (cx, cy - 60), size=48)
        self.draw_text(f"本局得分: {score}", (cx, cy), size=24)
        self.draw_text(f"历史最高分: {high}", (cx, cy + 35), size=24)
        self.draw_text(
            "按 R 键重新开始 / 按 ESC 键退出",
            (cx, cy + 85), size=18,
        )

    def draw_win(self, score: int) -> None:
        overlay = pygame.Surface((WINDOW_WIDTH, WINDOW_HEIGHT), pygame.SRCALPHA)
        overlay.fill((0, 0, 0, 180))
        self.surface.blit(overlay, (0, 0))
        cx, cy = WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2
        self.draw_text("恭喜通关!", (cx, cy - 40), size=48)
        self.draw_text(f"最终得分: {score}", (cx, cy + 10), size=24)
        self.draw_text(
            "按 R 键重新开始 / 按 ESC 键退出",
            (cx, cy + 60), size=18,
        )
