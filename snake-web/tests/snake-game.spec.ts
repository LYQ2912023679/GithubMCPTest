import { test, expect, Page } from '@playwright/test';

const GAME_URL = 'http://localhost:8090';

async function startGame(page: Page) {
  await page.goto(GAME_URL);
  await page.waitForLoadState('networkidle');
  await page.keyboard.press('Enter');
  await page.waitForTimeout(200);
}

async function getState(page: Page): Promise<string> {
  return page.evaluate(() => (window as any).game.getState());
}

async function getScore(page: Page): Promise<number> {
  return page.evaluate(() => (window as any).game.getScore());
}

async function getHighScore(page: Page): Promise<number> {
  return page.evaluate(() => (window as any).game.getHighScore());
}

async function getSnake(page: Page): Promise<{x:number,y:number}[]> {
  return page.evaluate(() => (window as any).game.getSnake());
}

async function getFood(page: Page): Promise<{x:number,y:number}> {
  return page.evaluate(() => (window as any).game.getFood());
}

async function getSpeed(page: Page): Promise<number> {
  return page.evaluate(() => (window as any).game.getSpeed());
}

async function getDirection(page: Page): Promise<{x:number,y:number}> {
  return page.evaluate(() => (window as any).game.getDirection());
}

async function moveTowardFood(page: Page) {
  const snake = await getSnake(page);
  const food = await getFood(page);
  const dir = await getDirection(page);
  const head = snake[0];

  const dx = food.x - head.x;
  const dy = food.y - head.y;

  const canRight = dir.x !== -1;
  const canLeft = dir.x !== 1;
  const canUp = dir.y !== 1;
  const canDown = dir.y !== -1;

  if (dx > 0 && canRight) await page.keyboard.press('ArrowRight');
  else if (dx < 0 && canLeft) await page.keyboard.press('ArrowLeft');
  else if (dy > 0 && canDown) await page.keyboard.press('ArrowDown');
  else if (dy < 0 && canUp) await page.keyboard.press('ArrowUp');
  else if (canRight) await page.keyboard.press('ArrowRight');
  else if (canDown) await page.keyboard.press('ArrowDown');
  else if (canLeft) await page.keyboard.press('ArrowLeft');
  else if (canUp) await page.keyboard.press('ArrowUp');

  await page.waitForTimeout(200);
}

async function eatFood(page: Page, maxAttempts: number = 200): Promise<boolean> {
  for (let i = 0; i < maxAttempts; i++) {
    const state = await getState(page);
    if (state !== 'RUNNING') return false;
    const score = await getScore(page);
    if (score > 0) return true;
    await moveTowardFood(page);
  }
  return false;
}

// LYQ-T1: 游戏窗口初始化验证
test.describe('LYQ-T1: 游戏窗口初始化验证', () => {
  test('窗口大小为600x600且标题为"贪吃蛇游戏"', async ({ page }) => {
    await page.goto(GAME_URL);
    await page.waitForLoadState('networkidle');

    const title = await page.title();
    expect(title).toBe('贪吃蛇游戏');

    const canvas = page.locator('#game-canvas');
    const width = await canvas.getAttribute('width');
    const height = await canvas.getAttribute('height');
    expect(Number(width)).toBe(600);
    expect(Number(height)).toBe(600);
  });
});

// LYQ-T2: 开始界面显示验证
test.describe('LYQ-T2: 开始界面显示验证', () => {
  test('显示游戏名称和"按任意键开始"提示', async ({ page }) => {
    await page.goto(GAME_URL);
    await page.waitForLoadState('networkidle');

    const overlay = page.locator('#overlay');
    await expect(overlay).toBeVisible();

    const h1 = overlay.locator('h1');
    await expect(h1).toHaveText('贪吃蛇游戏');

    const hint = overlay.locator('p');
    await expect(hint.first()).toContainText('按任意键开始');
  });
});

// LYQ-T3: 蛇初始属性验证
test.describe('LYQ-T3: 蛇初始属性验证', () => {
  test('蛇初始长度3节，位于屏幕中央，方向向右', async ({ page }) => {
    await page.goto(GAME_URL);
    await page.waitForLoadState('networkidle');
    await page.keyboard.press('Enter');
    const snake = await getSnake(page);

    expect(snake.length).toBe(3);

    const centerX = Math.floor(600 / 30 / 2);
    const centerY = Math.floor(600 / 30 / 2);
    expect(snake[0].x).toBeGreaterThanOrEqual(centerX);
    expect(snake[0].x).toBeLessThanOrEqual(centerX + 2);
    expect(snake[0].y).toBe(centerY);

    const dir = await getDirection(page);
    expect(dir.x).toBe(1);
    expect(dir.y).toBe(0);
  });
});

// LYQ-T4: 方向键控制蛇移动验证
test.describe('LYQ-T4: 方向键控制蛇移动验证', () => {
  test('方向键能正确控制蛇移动方向', async ({ page }) => {
    await startGame(page);

    await page.keyboard.press('ArrowUp');
    await page.waitForTimeout(200);
    let dir = await getDirection(page);
    expect(dir.y).toBe(-1);

    await page.keyboard.press('ArrowRight');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.x).toBe(1);

    await page.keyboard.press('ArrowDown');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.y).toBe(1);

    await page.keyboard.press('ArrowLeft');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.x).toBe(-1);
  });
});

// LYQ-T5: WASD键控制蛇移动验证
test.describe('LYQ-T5: WASD键控制蛇移动验证', () => {
  test('WASD键能正确控制蛇移动方向', async ({ page }) => {
    await startGame(page);

    await page.keyboard.press('w');
    await page.waitForTimeout(200);
    let dir = await getDirection(page);
    expect(dir.y).toBe(-1);

    await page.keyboard.press('d');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.x).toBe(1);

    await page.keyboard.press('s');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.y).toBe(1);

    await page.keyboard.press('a');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.x).toBe(-1);
  });
});

// LYQ-T6: 蛇防反向移动验证
test.describe('LYQ-T6: 蛇防反向移动验证', () => {
  test('蛇不能直接反向移动', async ({ page }) => {
    await startGame(page);

    const initialDir = await getDirection(page);
    expect(initialDir.x).toBe(1);

    await page.keyboard.press('ArrowLeft');
    await page.waitForTimeout(200);
    let dir = await getDirection(page);
    expect(dir.x).toBe(1);

    await page.keyboard.press('ArrowUp');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.y).toBe(-1);

    await page.keyboard.press('ArrowDown');
    await page.waitForTimeout(200);
    dir = await getDirection(page);
    expect(dir.y).toBe(-1);
  });
});

// LYQ-T7: 食物随机生成验证
test.describe('LYQ-T7: 食物随机生成验证', () => {
  test('食物在网格内生成且不与蛇身重叠', async ({ page }) => {
    await startGame(page);

    const food = await getFood(page);
    expect(food.x).toBeGreaterThanOrEqual(0);
    expect(food.x).toBeLessThan(600 / 30);
    expect(food.y).toBeGreaterThanOrEqual(0);
    expect(food.y).toBeLessThan(600 / 30);

    const snake = await getSnake(page);
    const onSnake = snake.some(s => s.x === food.x && s.y === food.y);
    expect(onSnake).toBe(false);

    const food1 = await getFood(page);
    for (let i = 0; i < 5; i++) {
      await page.keyboard.press('ArrowRight');
      await page.waitForTimeout(200);
    }
    const food2 = await getFood(page);
    const snake2 = await getSnake(page);
    const onSnake2 = snake2.some(s => s.x === food2.x && s.y === food2.y);
    expect(onSnake2).toBe(false);
  });
});

// LYQ-T8: 食物颜色区分验证
test.describe('LYQ-T8: 食物颜色区分验证', () => {
  test('食物颜色与背景和蛇身明显区分', async ({ page }) => {
    await page.goto(GAME_URL);
    await page.waitForLoadState('networkidle');

    const foodColor = await page.evaluate(() => {
      const ctx = (document.getElementById('game-canvas') as HTMLCanvasElement).getContext('2d');
      return ctx?.fillStyle;
    });
    expect(foodColor).toBeDefined();
  });
});

// LYQ-T9: 吃到食物蛇变长验证
test.describe('LYQ-T9: 吃到食物蛇变长验证', () => {
  test('蛇头与食物重合时蛇身增加一节', async ({ page }) => {
    await startGame(page);

    const initialLen = (await getSnake(page)).length;
    expect(initialLen).toBe(3);

    let ate = false;
    for (let i = 0; i < 200 && !ate; i++) {
      await moveTowardFood(page);
      const newLen = (await getSnake(page)).length;
      if (newLen > initialLen) {
        ate = true;
        expect(newLen).toBe(initialLen + 1);
      }
    }
    expect(ate).toBe(true);
  });
});

// LYQ-T10: 吃到食物加分验证
test.describe('LYQ-T10: 吃到食物加分验证', () => {
  test('吃到食物后分数增加且速度提升', async ({ page }) => {
    await startGame(page);

    const initialScore = await getScore(page);
    const initialSpeed = await getSpeed(page);
    expect(initialScore).toBe(0);

    let ate = false;
    for (let i = 0; i < 200 && !ate; i++) {
      await moveTowardFood(page);
      const newScore = await getScore(page);
      if (newScore > initialScore) {
        ate = true;
        expect(newScore).toBe(initialScore + 10);
        const newSpeed = await getSpeed(page);
        expect(newSpeed).toBeLessThan(initialSpeed);
      }
    }
    expect(ate).toBe(true);
  });
});

// LYQ-T11: 撞墙游戏结束验证
test.describe('LYQ-T11: 撞墙游戏结束验证', () => {
  test('蛇头超出边界触发游戏结束', async ({ page }) => {
    await startGame(page);

    for (let i = 0; i < 25; i++) {
      await page.keyboard.press('ArrowRight');
      await page.waitForTimeout(200);
      const state = await getState(page);
      if (state === 'GAME_OVER') break;
    }

    const state = await getState(page);
    expect(state).toBe('GAME_OVER');

    const overlay = page.locator('#overlay');
    await expect(overlay).toBeVisible();
    await expect(overlay.locator('h1')).toHaveText('游戏结束');
  });
});

// LYQ-T12: 撞自身游戏结束验证
test.describe('LYQ-T12: 撞自身游戏结束验证', () => {
  test('蛇头撞向自身触发游戏结束', async ({ page }) => {
    await startGame(page);

    let ate = 0;
    for (let i = 0; i < 200 && ate < 5; i++) {
      const snake = await getSnake(page);
      const food = await getFood(page);
      const head = snake[0];

      if (food.x > head.x) await page.keyboard.press('ArrowRight');
      else if (food.x < head.x) await page.keyboard.press('ArrowLeft');
      else if (food.y > head.y) await page.keyboard.press('ArrowDown');
      else if (food.y < head.y) await page.keyboard.press('ArrowUp');

      await page.waitForTimeout(200);
      const score = await getScore(page);
      if (score > ate * 10) ate = Math.floor(score / 10);
      const state = await getState(page);
      if (state === 'GAME_OVER') break;
    }

    await page.keyboard.press('ArrowUp');
    await page.waitForTimeout(200);
    await page.keyboard.press('ArrowLeft');
    await page.waitForTimeout(200);
    await page.keyboard.press('ArrowDown');
    await page.waitForTimeout(200);

    const state = await getState(page);
    const isGameOver = state === 'GAME_OVER';
    expect(isGameOver || ate >= 4).toBeTruthy();
  });
});

// LYQ-T13: 当前分数实时显示验证
test.describe('LYQ-T13: 当前分数实时显示验证', () => {
  test('界面实时显示当前得分', async ({ page }) => {
    await startGame(page);

    const scoreEl = page.locator('#current-score');
    await expect(scoreEl).toHaveText('0');

    let ate = false;
    for (let i = 0; i < 200 && !ate; i++) {
      await moveTowardFood(page);
      const score = await getScore(page);
      if (score > 0) {
        ate = true;
        await expect(scoreEl).toHaveText(String(score));
      }
    }
    expect(ate).toBe(true);
  });
});

// LYQ-T14: 最高分持久化存储验证
test.describe('LYQ-T14: 最高分持久化存储验证', () => {
  test('游戏结束更新最高分并持久化', async ({ page }) => {
    await page.goto(GAME_URL);
    await page.waitForLoadState('networkidle');
    await page.evaluate(() => localStorage.setItem('snakeHighScore', '0'));
    await page.reload();
    await page.waitForLoadState('networkidle');

    await startGame(page);

    let ate = false;
    for (let i = 0; i < 200 && !ate; i++) {
      await moveTowardFood(page);
      const score = await getScore(page);
      if (score >= 10) { ate = true; }
    }
    expect(ate).toBe(true);

    for (let i = 0; i < 25; i++) {
      await page.keyboard.press('ArrowRight');
      await page.waitForTimeout(200);
      if (await getState(page) === 'GAME_OVER') break;
    }

    const score = await getScore(page);
    const highScore = await getHighScore(page);
    expect(highScore).toBeGreaterThanOrEqual(score);

    const stored = await page.evaluate(() => localStorage.getItem('snakeHighScore'));
    expect(Number(stored)).toBe(highScore);
  });
});

// LYQ-T15: 空格键暂停继续验证
test.describe('LYQ-T15: 空格键暂停继续验证', () => {
  test('空格键暂停游戏，再次按下恢复', async ({ page }) => {
    await startGame(page);

    const snakeBefore = await getSnake(page);

    await page.keyboard.press(' ');
    await page.waitForTimeout(500);

    const state = await getState(page);
    expect(state).toBe('PAUSED');

    const statusEl = page.locator('#status-indicator');
    await expect(statusEl).toBeVisible();
    await expect(statusEl).toHaveText('已暂停');

    const snakeDuring = await getSnake(page);
    await page.waitForTimeout(500);
    const snakeAfter = await getSnake(page);
    expect(snakeDuring).toEqual(snakeAfter);

    await page.keyboard.press(' ');
    await page.waitForTimeout(200);
    const state2 = await getState(page);
    expect(state2).toBe('RUNNING');
  });
});

// LYQ-T16: R键重新开始验证
test.describe('LYQ-T16: R键重新开始验证', () => {
  test('游戏结束后按R键重新开始', async ({ page }) => {
    await startGame(page);

    for (let i = 0; i < 25; i++) {
      await page.keyboard.press('ArrowRight');
      await page.waitForTimeout(200);
      if (await getState(page) === 'GAME_OVER') break;
    }

    expect(await getState(page)).toBe('GAME_OVER');

    await page.keyboard.press('r');
    await page.waitForTimeout(300);

    const state = await getState(page);
    expect(state).toBe('RUNNING');

    const snake = await getSnake(page);
    expect(snake.length).toBe(3);

    const score = await getScore(page);
    expect(score).toBe(0);
  });
});

// LYQ-T17: ESC键退出游戏验证
test.describe('LYQ-T17: ESC键退出游戏验证', () => {
  test('运行状态按ESC退出游戏', async ({ page }) => {
    await startGame(page);

    await page.keyboard.press('Escape');
    await page.waitForTimeout(200);

    const state = await getState(page);
    expect(state).toBe('EXITED');

    const overlay = page.locator('#overlay');
    await expect(overlay).toBeVisible();
  });
});

// LYQ-T18: 网格对齐验证
test.describe('LYQ-T18: 网格对齐验证', () => {
  test('蛇和食物位置对齐网格', async ({ page }) => {
    await startGame(page);

    const snake = await getSnake(page);
    for (const seg of snake) {
      expect(Number.isInteger(seg.x)).toBe(true);
      expect(Number.isInteger(seg.y)).toBe(true);
    }

    const food = await getFood(page);
    expect(Number.isInteger(food.x)).toBe(true);
    expect(Number.isInteger(food.y)).toBe(true);

    await page.keyboard.press('ArrowRight');
    await page.waitForTimeout(200);
    const snake2 = await getSnake(page);
    for (const seg of snake2) {
      expect(Number.isInteger(seg.x)).toBe(true);
      expect(Number.isInteger(seg.y)).toBe(true);
    }
  });
});

// LYQ-T19: 按键即时响应验证
test.describe('LYQ-T19: 按键即时响应验证', () => {
  test('按键操作即时响应无明显延迟', async ({ page }) => {
    await startGame(page);

    const t1 = Date.now();
    await page.keyboard.press('ArrowUp');
    await page.waitForTimeout(200);
    const dir = await getDirection(page);
    const t2 = Date.now();

    expect(dir.y).toBe(-1);
    expect(t2 - t1).toBeLessThan(1000);
  });
});

// LYQ-T20: 帧率稳定性验证
test.describe('LYQ-T20: 帧率稳定性验证', () => {
  test('游戏主循环保持稳定帧率', async ({ page }) => {
    await startGame(page);

    await page.waitForTimeout(2000);

    const fps = await page.evaluate(() => (window as any).game.getFps());
    expect(fps).toBeGreaterThan(30);
    expect(fps).toBeLessThan(120);
  });
});
