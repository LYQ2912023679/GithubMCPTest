import sys
import pygame
from enum import IntEnum

from settings import (
    Point, UP, DOWN, LEFT, RIGHT,
    WINDOW_WIDTH, WINDOW_HEIGHT, FPS, SNAKE_SPEED, MIN_SNAKE_SPEED,
    SCORE_PER_FOOD, SPEED_UP_INTERVAL, GAME_TITLE,
)
from snake import Snake
from food import Food
from game_utils import ScoreManager, Renderer


class GameState(IntEnum):
    START = 0
    RUNNING = 1
    PAUSED = 2
    GAME_OVER = 3
    WIN = 4


class Game:
    def __init__(self) -> None:
        pygame.init()
        self.screen = pygame.display.set_mode((WINDOW_WIDTH, WINDOW_HEIGHT))
        pygame.display.set_caption(GAME_TITLE)
        self.clock = pygame.time.Clock()
        self.renderer = Renderer(self.screen)
        self.state: GameState = GameState.START
        self.snake: Snake = Snake()
        self.food: Food = Food()
        self.food.respawn(self.snake.body)
        self.score: ScoreManager = ScoreManager()
        self.move_timer: float = 0.0
        self.current_speed: float = SNAKE_SPEED
        self.food_eaten: int = 0
        self.running: bool = True

    def reset(self) -> None:
        self.snake = Snake()
        self.food = Food()
        self.food.respawn(self.snake.body)
        self.score.reset()
        self.move_timer = 0.0
        self.current_speed = SNAKE_SPEED
        self.food_eaten = 0

    def run(self) -> None:
        while self.running:
            dt = self.clock.tick(FPS) / 1000.0
            for event in pygame.event.get():
                self.handle_event(event)
            self.update(dt)
            self.render()
            pygame.display.flip()
        pygame.quit()
        sys.exit()

    def handle_event(self, event: pygame.event.Event) -> None:
        if event.type == pygame.QUIT:
            self.running = False
            return

        if event.type != pygame.KEYDOWN:
            return

        if event.key == pygame.K_ESCAPE:
            self.running = False
            return

        if self.state == GameState.START:
            self.state = GameState.RUNNING

        elif self.state == GameState.RUNNING:
            if event.key in (pygame.K_UP, pygame.K_w):
                self.snake.set_direction(UP)
            elif event.key in (pygame.K_DOWN, pygame.K_s):
                self.snake.set_direction(DOWN)
            elif event.key in (pygame.K_LEFT, pygame.K_a):
                self.snake.set_direction(LEFT)
            elif event.key in (pygame.K_RIGHT, pygame.K_d):
                self.snake.set_direction(RIGHT)
            elif event.key == pygame.K_SPACE:
                self.state = GameState.PAUSED

        elif self.state == GameState.PAUSED:
            if event.key == pygame.K_SPACE:
                self.state = GameState.RUNNING

        elif self.state in (GameState.GAME_OVER, GameState.WIN):
            if event.key == pygame.K_r:
                self.reset()
                self.state = GameState.RUNNING

    def update(self, dt: float) -> None:
        if self.state != GameState.RUNNING:
            return
        self.move_timer += dt
        if self.move_timer < self.current_speed:
            return
        self.move_timer = 0.0
        self.snake.move()
        self._check_collisions()

    def _check_collisions(self) -> None:
        if self.snake.head() == self.food.position:
            self.snake.grow()
            self.score.add(SCORE_PER_FOOD)
            self.food_eaten += 1
            if self.food_eaten % SPEED_UP_INTERVAL == 0:
                self.current_speed = max(
                    self.current_speed * 0.95, MIN_SNAKE_SPEED
                )
            if not self.food.respawn(self.snake.body):
                self.score.update_high_score()
                self.state = GameState.WIN
                return

        if self.snake.hits_wall() or self.snake.self_collision():
            self.score.update_high_score()
            self.state = GameState.GAME_OVER

    def render(self) -> None:
        if self.state == GameState.START:
            self.renderer.draw_start_screen()
            return

        self.renderer.draw_background()
        self.renderer.draw_snake(self.snake)
        self.renderer.draw_food(self.food)
        self.renderer.draw_score(self.score.current_score, self.score.high_score)

        if self.state == GameState.PAUSED:
            self.renderer.draw_pause_overlay()
        elif self.state == GameState.GAME_OVER:
            self.renderer.draw_game_over(
                self.score.current_score, self.score.high_score
            )
        elif self.state == GameState.WIN:
            self.renderer.draw_win(self.score.current_score)


def main() -> None:
    game = Game()
    game.run()


if __name__ == "__main__":
    main()
