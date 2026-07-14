const GRID_SIZE = 20;
const CELL_SIZE = 30;
const CANVAS_WIDTH = 600;
const CANVAS_HEIGHT = 600;
const INITIAL_SPEED = 150;
const MIN_SPEED = 60;
const SPEED_INCREMENT = 3;

const COLOR_BG = '#16213e';
const COLOR_SNAKE = '#00d4ff';
const COLOR_SNAKE_HEAD = '#0f3460';
const COLOR_FOOD = '#e94560';
const COLOR_GRID = '#1a1a2e';

class SnakeGame {
    constructor() {
        this.canvas = document.getElementById('game-canvas');
        this.ctx = this.canvas.getContext('2d');
        this.overlay = document.getElementById('overlay');
        this.scoreEl = document.getElementById('current-score');
        this.bestScoreEl = document.getElementById('best-score');
        this.statusIndicator = document.getElementById('status-indicator');

        this.state = 'START';
        this.score = 0;
        this.highScore = this.loadHighScore();
        this.bestScoreEl.textContent = this.highScore;
        this.speed = INITIAL_SPEED;
        this.lastMoveTime = 0;
        this.frameCount = 0;
        this.startTime = 0;

        this.reset();
        this.bindEvents();
        this.render();
        this.gameLoop();
    }

    reset() {
        const centerX = Math.floor(CANVAS_WIDTH / CELL_SIZE / 2);
        const centerY = Math.floor(CANVAS_HEIGHT / CELL_SIZE / 2);
        this.snake = [
            { x: centerX, y: centerY },
            { x: centerX - 1, y: centerY },
            { x: centerX - 2, y: centerY }
        ];
        this.direction = { x: 1, y: 0 };
        this.pendingDirection = { x: 1, y: 0 };
        this.score = 0;
        this.speed = INITIAL_SPEED;
        this.scoreEl.textContent = '0';
        this.generateFood();
    }

    loadHighScore() {
        try {
            const saved = localStorage.getItem('snakeHighScore');
            return saved ? parseInt(saved, 10) : 0;
        } catch (e) {
            return 0;
        }
    }

    saveHighScore() {
        try {
            localStorage.setItem('snakeHighScore', String(this.highScore));
        } catch (e) {}
    }

    generateFood() {
        let valid = false;
        while (!valid) {
            this.food = {
                x: Math.floor(Math.random() * (CANVAS_WIDTH / CELL_SIZE)),
                y: Math.floor(Math.random() * (CANVAS_HEIGHT / CELL_SIZE))
            };
            valid = !this.snake.some(seg => seg.x === this.food.x && seg.y === this.food.y);
        }
    }

    bindEvents() {
        document.addEventListener('keydown', (e) => {
            const key = e.key.toLowerCase();

            if (this.state === 'START') {
                this.state = 'RUNNING';
                this.overlay.classList.add('hidden');
                this.startTime = performance.now();
                e.preventDefault();
                return;
            }

            if (this.state === 'GAME_OVER') {
                if (key === 'r') {
                    this.reset();
                    this.state = 'RUNNING';
                    this.overlay.classList.add('hidden');
                    this.statusIndicator.classList.add('hidden');
                    this.startTime = performance.now();
                } else if (key === 'escape') {
                    this.state = 'EXITED';
                    this.overlay.innerHTML = '<h1>游戏已退出</h1>';
                    this.overlay.classList.remove('hidden');
                }
                e.preventDefault();
                return;
            }

            if (this.state === 'RUNNING') {
                switch (key) {
                    case 'arrowup': case 'w':
                        if (this.direction.y !== 1) this.pendingDirection = { x: 0, y: -1 };
                        e.preventDefault(); break;
                    case 'arrowdown': case 's':
                        if (this.direction.y !== -1) this.pendingDirection = { x: 0, y: 1 };
                        e.preventDefault(); break;
                    case 'arrowleft': case 'a':
                        if (this.direction.x !== 1) this.pendingDirection = { x: -1, y: 0 };
                        e.preventDefault(); break;
                    case 'arrowright': case 'd':
                        if (this.direction.x !== -1) this.pendingDirection = { x: 1, y: 0 };
                        e.preventDefault(); break;
                    case ' ':
                        this.state = 'PAUSED';
                        this.statusIndicator.textContent = '已暂停';
                        this.statusIndicator.classList.remove('hidden');
                        e.preventDefault(); break;
                    case 'escape':
                        this.state = 'EXITED';
                        this.overlay.innerHTML = '<h1>游戏已退出</h1>';
                        this.overlay.classList.remove('hidden');
                        e.preventDefault(); break;
                }
                return;
            }

            if (this.state === 'PAUSED') {
                if (key === ' ') {
                    this.state = 'RUNNING';
                    this.statusIndicator.classList.add('hidden');
                    e.preventDefault();
                } else if (key === 'escape') {
                    this.state = 'EXITED';
                    this.overlay.innerHTML = '<h1>游戏已退出</h1>';
                    this.overlay.classList.remove('hidden');
                    e.preventDefault();
                }
            }
        });
    }

    update(timestamp) {
        if (this.state !== 'RUNNING') return;

        if (timestamp - this.lastMoveTime < this.speed) return;
        this.lastMoveTime = timestamp;

        this.direction = this.pendingDirection;

        const head = {
            x: this.snake[0].x + this.direction.x,
            y: this.snake[0].y + this.direction.y
        };

        if (head.x < 0 || head.x >= CANVAS_WIDTH / CELL_SIZE ||
            head.y < 0 || head.y >= CANVAS_HEIGHT / CELL_SIZE) {
            this.gameOver();
            return;
        }

        if (this.snake.some(seg => seg.x === head.x && seg.y === head.y)) {
            this.gameOver();
            return;
        }

        this.snake.unshift(head);

        if (head.x === this.food.x && head.y === this.food.y) {
            this.score += 10;
            this.scoreEl.textContent = this.score;
            if (this.speed > MIN_SPEED) this.speed -= SPEED_INCREMENT;
            this.generateFood();
        } else {
            this.snake.pop();
        }
    }

    gameOver() {
        this.state = 'GAME_OVER';
        if (this.score > this.highScore) {
            this.highScore = this.score;
            this.bestScoreEl.textContent = this.highScore;
            this.saveHighScore();
        }
        this.overlay.innerHTML = `
            <h1>游戏结束</h1>
            <p>本局得分: ${this.score}</p>
            <p>历史最高分: ${this.highScore}</p>
            <p class="hint">按R键重新开始 / 按ESC键退出</p>
        `;
        this.overlay.classList.remove('hidden');
    }

    render() {
        this.ctx.fillStyle = COLOR_BG;
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        this.ctx.strokeStyle = COLOR_GRID;
        this.ctx.lineWidth = 0.5;
        for (let i = 0; i <= CANVAS_WIDTH / CELL_SIZE; i++) {
            this.ctx.beginPath();
            this.ctx.moveTo(i * CELL_SIZE, 0);
            this.ctx.lineTo(i * CELL_SIZE, CANVAS_HEIGHT);
            this.ctx.stroke();
            this.ctx.beginPath();
            this.ctx.moveTo(0, i * CELL_SIZE);
            this.ctx.lineTo(CANVAS_WIDTH, i * CELL_SIZE);
            this.ctx.stroke();
        }

        this.ctx.fillStyle = COLOR_FOOD;
        this.ctx.beginPath();
        this.ctx.arc(
            this.food.x * CELL_SIZE + CELL_SIZE / 2,
            this.food.y * CELL_SIZE + CELL_SIZE / 2,
            CELL_SIZE / 2 - 2, 0, Math.PI * 2
        );
        this.ctx.fill();

        this.snake.forEach((seg, i) => {
            this.ctx.fillStyle = i === 0 ? COLOR_SNAKE_HEAD : COLOR_SNAKE;
            this.ctx.fillRect(
                seg.x * CELL_SIZE + 1,
                seg.y * CELL_SIZE + 1,
                CELL_SIZE - 2,
                CELL_SIZE - 2
            );
        });
    }

    gameLoop() {
        const loop = (timestamp) => {
            this.frameCount++;
            this.update(timestamp);
            this.render();
            requestAnimationFrame(loop);
        };
        requestAnimationFrame(loop);
    }

    getFps() {
        if (this.startTime === 0) return 0;
        const elapsed = (performance.now() - this.startTime) / 1000;
        return elapsed > 0 ? this.frameCount / elapsed : 0;
    }

    getState() { return this.state; }
    getScore() { return this.score; }
    getHighScore() { return this.highScore; }
    getSnake() { return this.snake; }
    getFood() { return this.food; }
    getSpeed() { return this.speed; }
    getDirection() { return this.direction; }
}

window.SnakeGame = SnakeGame;
window.addEventListener('load', () => {
    window.game = new SnakeGame();
});
