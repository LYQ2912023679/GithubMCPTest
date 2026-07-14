import json
import random
from collections import deque
from enum import IntEnum
from typing import Dict, Deque, Tuple, List
from flask import Flask, jsonify, request

app = Flask(__name__)

WINDOW_WIDTH = 600
WINDOW_HEIGHT = 600
GRID_SIZE = 20
GRID_W = WINDOW_WIDTH // GRID_SIZE
GRID_H = WINDOW_HEIGHT // GRID_SIZE
INITIAL_SNAKE_LENGTH = 3
SCORE_PER_FOOD = 10
SPEED_UP_INTERVAL = 5
GAME_TITLE = "贪吃蛇游戏"

UP = (0, -1)
DOWN = (0, 1)
LEFT = (-1, 0)
RIGHT = (1, 0)
OPPOSITE = {UP: DOWN, DOWN: UP, LEFT: RIGHT, RIGHT: LEFT}

DIR_MAP = {"up": UP, "down": DOWN, "left": LEFT, "right": RIGHT}

DIFFICULTY_CONFIG = {
    "easy": {"speed": 0.20, "min_speed": 0.10, "speed_up_interval": 8, "speed_up_factor": 0.97},
    "medium": {"speed": 0.15, "min_speed": 0.06, "speed_up_interval": 5, "speed_up_factor": 0.95},
    "hard": {"speed": 0.10, "min_speed": 0.04, "speed_up_interval": 3, "speed_up_factor": 0.92},
}

COLOR_BG = [0, 0, 0]
COLOR_SNAKE = [0, 255, 0]
COLOR_SNAKE_HEAD = [0, 200, 0]
COLOR_FOOD = [255, 0, 0]


class GameState(IntEnum):
    START = 0
    RUNNING = 1
    PAUSED = 2
    GAME_OVER = 3
    WIN = 4


class Snake:
    def __init__(self):
        cx, cy = GRID_W // 2, GRID_H // 2
        self.body: Deque[Tuple[int, int]] = deque()
        for i in range(INITIAL_SNAKE_LENGTH):
            self.body.append((cx - i, cy))
        self.direction = RIGHT
        self._pending_dir = RIGHT
        self.grow_pending = False

    def set_direction(self, new_dir):
        if new_dir == OPPOSITE.get(self.direction):
            return False
        self._pending_dir = new_dir
        return True

    def move(self):
        self.direction = self._pending_dir
        hx, hy = self.body[0]
        dx, dy = self.direction
        new_head = (hx + dx, hy + dy)
        self.body.appendleft(new_head)
        if self.grow_pending:
            self.grow_pending = False
        else:
            self.body.pop()

    def grow(self):
        self.grow_pending = True

    def head(self):
        return self.body[0]

    def self_collision(self):
        return self.body[0] in list(self.body)[1:]

    def hits_wall(self):
        hx, hy = self.body[0]
        return hx < 0 or hx >= GRID_W or hy < 0 or hy >= GRID_H


class Food:
    def __init__(self):
        self._position = (GRID_W // 4, GRID_H // 4)

    def respawn(self, snake_body):
        occupied = set(snake_body)
        free_cells = [(x, y) for x in range(GRID_W) for y in range(GRID_H) if (x, y) not in occupied]
        if not free_cells:
            return False
        self._position = random.choice(free_cells)
        return True

    @property
    def position(self):
        return self._position


class GameSession:
    def __init__(self, session_id, difficulty="medium"):
        self.session_id = session_id
        self.state = GameState.RUNNING
        self.snake = Snake()
        self.food = Food()
        self.food.respawn(self.snake.body)
        self.score = 0
        self.high_score = self._load_high_score()
        self.food_eaten = 0
        self.difficulty = difficulty
        cfg = DIFFICULTY_CONFIG.get(difficulty, DIFFICULTY_CONFIG["medium"])
        self.current_speed = cfg["speed"]
        self.min_speed = cfg["min_speed"]
        self.speed_up_interval = int(cfg["speed_up_interval"])
        self.speed_up_factor = cfg["speed_up_factor"]
        self.colors = {
            "background": COLOR_BG,
            "snake": COLOR_SNAKE,
            "snake_head": COLOR_SNAKE_HEAD,
            "food": COLOR_FOOD,
        }

    def _load_high_score(self):
        try:
            with open("highscore.json", "r", encoding="utf-8") as f:
                data = json.load(f)
                return int(data.get("high_score", 0))
        except Exception:
            return 0

    def _save_high_score(self):
        try:
            with open("highscore.json", "w", encoding="utf-8") as f:
                json.dump({"high_score": self.high_score}, f)
        except Exception:
            pass

    def step(self):
        if self.state != GameState.RUNNING:
            return False
        self.snake.move()
        return self._check_collisions()

    def _check_collisions(self):
        if self.snake.head() == self.food.position:
            self.snake.grow()
            self.score += SCORE_PER_FOOD
            self.food_eaten += 1
            if self.food_eaten % self.speed_up_interval == 0:
                self.current_speed = max(self.current_speed * self.speed_up_factor, self.min_speed)
            if not self.food.respawn(self.snake.body):
                self._update_high_score()
                self.state = GameState.WIN
                return True
        if self.snake.hits_wall() or self.snake.self_collision():
            self._update_high_score()
            self.state = GameState.GAME_OVER
            return True
        return False

    def _update_high_score(self):
        if self.score > self.high_score:
            self.high_score = self.score
            self._save_high_score()

    def set_direction(self, direction):
        if self.state != GameState.RUNNING:
            return False
        return self.snake.set_direction(direction)

    def pause(self):
        if self.state == GameState.RUNNING:
            self.state = GameState.PAUSED
            return True
        return False

    def resume(self):
        if self.state == GameState.PAUSED:
            self.state = GameState.RUNNING
            return True
        return False

    def reset(self):
        self.snake = Snake()
        self.food = Food()
        self.food.respawn(self.snake.body)
        self.score = 0
        self.food_eaten = 0
        self.state = GameState.RUNNING
        cfg = DIFFICULTY_CONFIG.get(self.difficulty, DIFFICULTY_CONFIG["medium"])
        self.current_speed = cfg["speed"]

    def to_dict(self):
        return {
            "session_id": self.session_id,
            "state": self.state.name,
            "snake_body": list(self.snake.body),
            "snake_direction": list(self.snake.direction),
            "snake_length": len(self.snake.body),
            "food_position": list(self.food.position),
            "score": self.score,
            "high_score": self.high_score,
            "food_eaten": self.food_eaten,
            "window": {"width": WINDOW_WIDTH, "height": WINDOW_HEIGHT, "title": GAME_TITLE},
            "grid": {"width": GRID_W, "height": GRID_H, "cell_size": GRID_SIZE},
            "colors": self.colors,
            "difficulty": self.difficulty,
            "speed": round(self.current_speed, 4),
        }


sessions: Dict[str, GameSession] = {}
session_counter = 0


@app.route("/api/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "service": "snake-game-api"})


@app.route("/api/game", methods=["POST"])
def create_game():
    global session_counter
    data = request.get_json(silent=True) or {}
    difficulty = data.get("difficulty", "medium")
    session_counter += 1
    sid = f"game-{session_counter}"
    sessions[sid] = GameSession(sid, difficulty)
    return jsonify(sessions[sid].to_dict()), 201


@app.route("/api/game/<session_id>", methods=["GET"])
def get_game(session_id):
    if session_id not in sessions:
        return jsonify({"error": "Session not found"}), 404
    return jsonify(sessions[session_id].to_dict())


@app.route("/api/game/<session_id>/move", methods=["POST"])
def move_snake(session_id):
    if session_id not in sessions:
        return jsonify({"error": "Session not found"}), 404
    data = request.get_json(silent=True) or {}
    direction = data.get("direction")
    if direction not in DIR_MAP:
        return jsonify({"error": "Invalid direction. Use: up, down, left, right"}), 400
    success = sessions[session_id].set_direction(DIR_MAP[direction])
    return jsonify({"success": success, "game": sessions[session_id].to_dict()})


@app.route("/api/game/<session_id>/step", methods=["POST"])
def step_game(session_id):
    if session_id not in sessions:
        return jsonify({"error": "Session not found"}), 404
    steps = (request.get_json(silent=True) or {}).get("steps", 1)
    results = []
    for _ in range(steps):
        game_over = sessions[session_id].step()
        results.append({"game_over": game_over})
        if game_over:
            break
    return jsonify({"steps": results, "game": sessions[session_id].to_dict()})


@app.route("/api/game/<session_id>/pause", methods=["POST"])
def pause_game(session_id):
    if session_id not in sessions:
        return jsonify({"error": "Session not found"}), 404
    success = sessions[session_id].pause()
    return jsonify({"success": success, "game": sessions[session_id].to_dict()})


@app.route("/api/game/<session_id>/resume", methods=["POST"])
def resume_game(session_id):
    if session_id not in sessions:
        return jsonify({"error": "Session not found"}), 404
    success = sessions[session_id].resume()
    return jsonify({"success": success, "game": sessions[session_id].to_dict()})


@app.route("/api/game/<session_id>/reset", methods=["POST"])
def reset_game(session_id):
    if session_id not in sessions:
        return jsonify({"error": "Session not found"}), 404
    sessions[session_id].reset()
    return jsonify(sessions[session_id].to_dict())


@app.route("/api/game/<session_id>/score", methods=["GET"])
def get_score(session_id):
    if session_id not in sessions:
        return jsonify({"error": "Session not found"}), 404
    g = sessions[session_id]
    return jsonify({"score": g.score, "high_score": g.high_score})


@app.route("/api/highscore", methods=["GET"])
def get_highscore():
    try:
        with open("highscore.json", "r", encoding="utf-8") as f:
            return jsonify(json.load(f))
    except Exception:
        return jsonify({"high_score": 0})


@app.route("/api/config", methods=["GET"])
def get_config():
    return jsonify({
        "window": {"width": WINDOW_WIDTH, "height": WINDOW_HEIGHT, "title": GAME_TITLE},
        "grid": {"width": GRID_W, "height": GRID_H, "cell_size": GRID_SIZE},
        "initial_snake_length": INITIAL_SNAKE_LENGTH,
        "score_per_food": SCORE_PER_FOOD,
        "colors": {"background": COLOR_BG, "snake": COLOR_SNAKE, "food": COLOR_FOOD},
        "difficulties": list(DIFFICULTY_CONFIG.keys()),
    })


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)
