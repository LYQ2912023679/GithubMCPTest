import pytest
import json
import os
import tempfile

os.chdir(tempfile.mkdtemp())

from api_server import app, GameSession, GameState, Snake, Food, GRID_W, GRID_H, WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE


@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as c:
        yield c


@pytest.fixture
def game_session(client):
    resp = client.post("/api/game", json={"difficulty": "medium"})
    assert resp.status_code == 201
    data = resp.get_json()
    return data["session_id"]


class TestHealth:
    def test_health(self, client):
        resp = client.get("/api/health")
        assert resp.status_code == 200
        data = resp.get_json()
        assert data["status"] == "ok"


class TestCreateGame:
    def test_create_game(self, client):
        resp = client.post("/api/game", json={"difficulty": "medium"})
        assert resp.status_code == 201
        data = resp.get_json()
        assert data["state"] == "RUNNING"
        assert data["snake_length"] == 3
        assert data["window"]["width"] == 600
        assert data["window"]["height"] == 600
        assert data["window"]["title"] == GAME_TITLE

    def test_create_game_default_difficulty(self, client):
        resp = client.post("/api/game", json={})
        assert resp.status_code == 201


class TestGetGame:
    def test_get_game(self, client, game_session):
        resp = client.get(f"/api/game/{game_session}")
        assert resp.status_code == 200
        data = resp.get_json()
        assert data["session_id"] == game_session

    def test_get_nonexistent_game(self, client):
        resp = client.get("/api/game/nonexistent")
        assert resp.status_code == 404


class TestMoveSnake:
    def test_move_up(self, client, game_session):
        resp = client.post(f"/api/game/{game_session}/move", json={"direction": "up"})
        assert resp.status_code == 200
        data = resp.get_json()
        assert data["success"] is True

    def test_invalid_direction(self, client, game_session):
        resp = client.post(f"/api/game/{game_session}/move", json={"direction": "invalid"})
        assert resp.status_code == 400

    def test_move_nonexistent(self, client):
        resp = client.post("/api/game/nonexistent/move", json={"direction": "up"})
        assert resp.status_code == 404


class TestStepGame:
    def test_step_once(self, client, game_session):
        resp = client.post(f"/api/game/{game_session}/step", json={"steps": 1})
        assert resp.status_code == 200
        data = resp.get_json()
        assert len(data["steps"]) == 1

    def test_step_multiple(self, client, game_session):
        resp = client.post(f"/api/game/{game_session}/step", json={"steps": 5})
        assert resp.status_code == 200
        data = resp.get_json()
        assert len(data["steps"]) == 5


class TestPauseResume:
    def test_pause(self, client, game_session):
        resp = client.post(f"/api/game/{game_session}/pause")
        assert resp.status_code == 200
        data = resp.get_json()
        assert data["success"] is True
        assert data["game"]["state"] == "PAUSED"

    def test_resume(self, client, game_session):
        client.post(f"/api/game/{game_session}/pause")
        resp = client.post(f"/api/game/{game_session}/resume")
        assert resp.status_code == 200
        data = resp.get_json()
        assert data["success"] is True
        assert data["game"]["state"] == "RUNNING"


class TestReset:
    def test_reset(self, client, game_session):
        client.post(f"/api/game/{game_session}/step", json={"steps": 3})
        resp = client.post(f"/api/game/{game_session}/reset")
        assert resp.status_code == 200
        data = resp.get_json()
        assert data["score"] == 0
        assert data["snake_length"] == 3
        assert data["state"] == "RUNNING"


class TestScore:
    def test_get_score(self, client, game_session):
        resp = client.get(f"/api/game/{game_session}/score")
        assert resp.status_code == 200
        data = resp.get_json()
        assert "score" in data
        assert "high_score" in data


class TestConfig:
    def test_config(self, client):
        resp = client.get("/api/config")
        assert resp.status_code == 200
        data = resp.get_json()
        assert data["window"]["width"] == 600
        assert data["grid"]["width"] == GRID_W
        assert "difficulties" in data


class TestGameSession:
    def test_initial_state(self):
        gs = GameSession("test-1", "medium")
        assert gs.state == GameState.RUNNING
        assert gs.score == 0

    def test_step_changes_snake(self):
        gs = GameSession("test-2", "easy")
        old_body = list(gs.snake.body)
        gs.step()
        assert list(gs.snake.body) != old_body

    def test_pause_then_step_no_move(self):
        gs = GameSession("test-3", "easy")
        gs.pause()
        old_body = list(gs.snake.body)
        gs.step()
        assert list(gs.snake.body) == old_body
