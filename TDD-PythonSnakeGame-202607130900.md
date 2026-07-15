# Python贪吃蛇游戏技术设计文档 (TDD)

| 项目 | 内容 |
|------|------|
| 项目名称 | Python贪吃蛇游戏 |
| 文档版本 | V1.0 |
| 编写日期 | 2026-07-13 |
| 关联需求 | Python贪吃蛇游戏需求文档 (PRD) V1.0 |
| 技术栈 | Python 3.6+ / pygame |

---

## 1. 文档概述

### 1.1 编写目的

本文档基于《Python贪吃蛇游戏需求文档 (PRD)》编写，旨在将功能需求与非功能需求转化为具体的技术设计方案，为开发人员提供明确的架构指导、模块接口定义和实现细节，确保开发过程有据可依。

### 1.2 设计范围

本设计文档覆盖以下内容：
- 系统架构与模块划分
- 核心数据结构设计
- 各模块接口与行为定义
- 游戏状态机设计
- 关键算法设计（碰撞检测、食物生成、方向控制）
- 非功能性技术方案（性能、UI/UX、持久化）

### 1.3 术语定义

| 术语 | 说明 |
|------|------|
| Grid | 隐形网格单元，游戏画面最小定位单位 |
| FPS | Frames Per Second，渲染帧率 |
| Tick | 蛇移动逻辑的单次推进周期 |
| State Machine | 管理游戏生命周期状态转换的有限状态机 |

---

## 2. 系统架构设计

### 2.1 总体架构

采用分层模块化架构，各模块职责单一，通过主循环驱动：

```
┌─────────────────────────────────────────────┐
│                  main.py                     │
│            (主循环 & 状态控制)                │
├──────┬──────────┬──────────┬────────────────┤
│snake │  food    │ settings │   game_utils    │
│ .py  │  .py     │  .py     │     .py         │
└──────┴──────────┴──────────┴────────────────┘
```

### 2.2 模块职责划分

| 模块 | 职责 | 主要类/函数 |
|------|------|-------------|
| `main.py` | 主循环、事件分发、状态机驱动、渲染调度 | `Game` |
| `snake.py` | 蛇的数据结构与行为（移动、增长、碰撞） | `Snake` |
| `food.py` | 食物生成与位置管理 | `Food` |
| `settings.py` | 全局常量配置（颜色、尺寸、速度、按键映射） | — |
| `game_utils.py` | 辅助功能（计分、最高分持久化、绘制工具） | `ScoreManager`, `Renderer` |

### 2.3 依赖关系

- `main.py` 依赖 `snake.py`、`food.py`、`settings.py`、`game_utils.py`
- `snake.py` 依赖 `settings.py`
- `food.py` 依赖 `settings.py`、`snake.py`（用于避障判定）
- `game_utils.py` 依赖 `settings.py`
- `settings.py` 无外部依赖，纯常量定义

---

## 3. 核心数据结构设计

### 3.1 坐标点

使用二元元组表示网格坐标：

    Point = Tuple[int, int]  # (x, y)，以网格为单位

### 3.2 蛇的数据结构

    class Snake:
        body: Deque[Point]          # 蛇身坐标队列，头部为 body[0]
        direction: Point            # 当前移动方向向量，如 (1,0) 表示向右
        grow_pending: bool          # 是否有待增长标记（吃到食物时置True）

设计说明：
- 使用 `collections.deque` 存储蛇身，支持 O(1) 头部插入和尾部弹出
- `body[0]` 始终代表蛇头位置
- `grow_pending` 标记避免在移动逻辑中频繁判断

### 3.3 食物数据结构

    class Food:
        position: Point             # 食物当前网格坐标

### 3.4 方向定义

    # settings.py
    UP    = (0, -1)
    DOWN  = (0, 1)
    LEFT  = (-1, 0)
    RIGHT = (1, 0)

    # 防反向映射
    OPPOSITE = {
        UP: DOWN,
        DOWN: UP,
        LEFT: RIGHT,
        RIGHT: LEFT,
    }

---

## 4. 模块详细设计

### 4.1 settings.py — 全局配置

| 配置项 | 值 | 说明 |
|--------|----|------|
| `WINDOW_WIDTH` | 600 | 窗口宽度（像素） |
| `WINDOW_HEIGHT` | 600 | 窗口高度（像素） |
| `GRID_SIZE` | 20 | 每格像素大小 |
| `GRID_W` | 30 | 水平网格数 (600/20) |
| `GRID_H` | 30 | 垂直网格数 (600/20) |
| `SNAKE_SPEED` | 0.15 | 蛇移动间隔（秒） |
| `FPS` | 60 | 渲染帧率 |
| `INITIAL_SNAKE_LENGTH` | 3 | 蛇初始长度 |
| `SCORE_PER_FOOD` | 10 | 每个食物得分 |
| `COLOR_BG` | (0, 0, 0) | 背景色（黑色） |
| `COLOR_SNAKE` | (0, 255, 0) | 蛇身色（绿色） |
| `COLOR_FOOD` | (255, 0, 0) | 食物色（红色） |
| `COLOR_TEXT` | (255, 255, 255) | 文字色（白色） |
| `HIGHSCORE_FILE` | "highscore.json" | 最高分存储文件 |

### 4.2 snake.py — 蛇模块

**类: `Snake`**

**初始化:**
- 蛇初始位置：屏幕中央，即 `(GRID_W // 2, GRID_H // 2)`
- 初始方向：`RIGHT`
- 初始长度：3节，水平排列

**核心方法:**

| 方法 | 签名 | 说明 |
|------|------|------|
| `__init__` | `(self) -> None` | 初始化蛇身、方向 |
| `set_direction` | `(self, new_dir: Point) -> None` | 设置方向，执行防反向校验 |
| `move` | `(self) -> None` | 推进蛇移动一格；若 `grow_pending` 为真则不弹出尾部并重置标记 |
| `grow` | `(self) -> None` | 设置 `grow_pending = True` |
| `head` | `(self) -> Point` | 返回蛇头坐标 |
| `collides_with` | `(self, point: Point) -> bool` | 判断蛇头是否与指定坐标重合 |
| `self_collision` | `(self) -> bool` | 判断蛇头是否与蛇身重合 |
| `hits_wall` | `(self) -> bool` | 判断蛇头是否超出边界 |

**移动算法:**
1. 计算新蛇头位置：`new_head = (head.x + direction.x, head.y + direction.y)`
2. 将 `new_head` 插入 `body` 队首
3. 若 `grow_pending` 为 `False`，弹出 `body` 队尾
4. 若 `grow_pending` 为 `True`，重置为 `False`

**防反向算法:**
- 当 `new_dir == OPPOSITE[current_direction]` 时，忽略该输入，保持原方向不变

### 4.3 food.py — 食物模块

**类: `Food`**

**核心方法:**

| 方法 | 签名 | 说明 |
|------|------|------|
| `__init__` | `(self) -> None` | 初始化食物位置 |
| `respawn` | `(self, snake_body: List[Point]) -> None` | 在非蛇身位置随机生成食物 |
| `position` | `-> Point` | 返回当前食物坐标 |

**食物生成算法:**
1. 计算所有空闲格子：`all_cells - snake_body`
2. 从空闲格子集合中随机选取一个作为食物位置
3. 若空闲格子为空（蛇填满全屏，胜利场景），可触发特殊处理

### 4.4 game_utils.py — 工具模块

#### 4.4.1 ScoreManager

| 方法 | 签名 | 说明 |
|------|------|------|
| `__init__` | `(self) -> None` | 初始化当前分数为0，加载历史最高分 |
| `add` | `(self, points: int) -> None` | 增加当前分数 |
| `current_score` | `-> int` | 返回当前分数 |
| `high_score` | `-> int` | 返回历史最高分 |
| `update_high_score` | `(self) -> bool` | 若当前分 > 历史最高分则更新并持久化，返回是否更新 |
| `_load` | `(self) -> None` | 从 `highscore.json` 读取最高分 |
| `_save` | `(self) -> None` | 将最高分写入 `highscore.json` |

**持久化格式 (highscore.json):**

    {
        "high_score": 150
    }

#### 4.4.2 Renderer

| 方法 | 签名 | 说明 |
|------|------|------|
| `draw_snake` | `(self, surface, snake: Snake) -> None` | 绘制蛇身 |
| `draw_food` | `(self, surface, food: Food) -> None` | 绘制食物 |
| `draw_score` | `(self, surface, score: int, high: int) -> None` | 绘制分数面板 |
| `draw_text` | `(self, surface, text: str, pos: Point, size: int) -> None` | 绘制文字 |
| `draw_pause_overlay` | `(self, surface) -> None` | 绘制暂停遮罩 |
| `draw_game_over` | `(self, surface, score: int, high: int) -> None` | 绘制游戏结束界面 |

---

## 5. 游戏状态机设计

### 5.1 状态定义

| 状态 | 枚举值 | 说明 |
|------|--------|------|
| `START` | 0 | 开始界面，等待玩家按键 |
| `RUNNING` | 1 | 游戏运行中 |
| `PAUSED` | 2 | 游戏暂停 |
| `GAME_OVER` | 3 | 游戏结束 |

### 5.2 状态转换图

```
          任意键
START ──────────→ RUNNING
                     │  ↑
              空格键  │  │ 空格键
                     ↓  │
                  PAUSED
                     │
        撞墙/撞自身   │
                     ↓
                 GAME_OVER
                     │
               R键    │
                     ↓
                  RUNNING (重置)
```

### 5.3 各状态行为

| 状态 | 事件处理 | 更新逻辑 | 渲染逻辑 |
|------|----------|----------|----------|
| `START` | 任意键 → 切换至 RUNNING | 无 | 标题 + "按任意键开始" |
| `RUNNING` | 方向键/WASD → 设置方向；空格 → 切换至 PAUSED；ESC → 退出 | Tick 到期时推进蛇移动、碰撞检测、食物判定 | 蛇 + 食物 + 分数 |
| `PAUSED` | 空格 → 切换至 RUNNING；ESC → 退出 | 无 | 当前画面 + "已暂停"遮罩 |
| `GAME_OVER` | R → 重置并切换至 RUNNING；ESC → 退出 | 无 | "游戏结束" + 分数 + 最高分 + 操作提示 |

---

## 6. 主循环设计 (main.py)

### 6.1 Game 类

    class Game:
        state: GameState              # 当前游戏状态
        snake: Snake                  # 蛇实例
        food: Food                    # 食物实例
        score: ScoreManager           # 计分管理器
        renderer: Renderer            # 渲染器
        clock: pygame.time.Clock      # 帧率时钟
        move_timer: float             # 移动计时器累积值

### 6.2 主循环流程

    while running:
        dt = clock.tick(FPS) / 1000.0     # 获取帧间隔（秒）

        # 1. 事件处理
        for event in pygame.event.get():
            handle_event(event, current_state)

        # 2. 状态更新
        if state == RUNNING:
            move_timer += dt
            if move_timer >= SNAKE_SPEED:
                move_timer = 0
                snake.move()
                check_collisions()

        # 3. 渲染
        render(current_state)
        pygame.display.flip()

### 6.3 碰撞检测流程

    def check_collisions():
        # 吃食物判定
        if snake.head() == food.position:
            snake.grow()
            score.add(SCORE_PER_FOOD)
            food.respawn(snake.body)

        # 撞墙判定
        if snake.hits_wall():
            transition_to(GAME_OVER)

        # 撞自身判定
        if snake.self_collision():
            transition_to(GAME_OVER)

---

## 7. 按键映射设计

### 7.1 按键事件映射表

| pygame 事件 | 适用状态 | 动作 |
|-------------|----------|------|
| `KEYDOWN` (任意键) | `START` | → `RUNNING` |
| `K_UP` / `K_w` | `RUNNING` | `snake.set_direction(UP)` |
| `K_DOWN` / `K_s` | `RUNNING` | `snake.set_direction(DOWN)` |
| `K_LEFT` / `K_a` | `RUNNING` | `snake.set_direction(LEFT)` |
| `K_RIGHT` / `K_d` | `RUNNING` | `snake.set_direction(RIGHT)` |
| `K_SPACE` | `RUNNING` | → `PAUSED` |
| `K_SPACE` | `PAUSED` | → `RUNNING` |
| `K_r` | `GAME_OVER` | 重置游戏 → `RUNNING` |
| `K_ESCAPE` | 任意状态 | 退出游戏 |
| `QUIT` | 任意状态 | 退出游戏 |

---

## 8. 非功能技术方案

### 8.1 性能设计

| 需求 | 技术方案 |
|------|----------|
| 稳定 60 FPS 渲染 | 使用 `pygame.time.Clock.tick(60)` 控制帧率 |
| 蛇移动速度独立于帧率 | 使用 `move_timer` 累积 `dt`，达到 `SNAKE_SPEED` 阈值才推进蛇移动 |
| 低 CPU/内存占用 | 仅维护蛇身 deque 和单个食物坐标，无大型数据结构；网格计算为整数运算 |
| 速度递增（可选） | 每吃 N 个食物后，`SNAKE_SPEED *= 0.95`（最小限制 0.05s） |

### 8.2 UI/UX 设计

| 需求 | 技术方案 |
|------|----------|
| 深色背景 | `COLOR_BG = (0, 0, 0)`，窗口 `fill` 填充 |
| 蛇身亮色 | `COLOR_SNAKE = (0, 255, 0)`，矩形绘制 |
| 食物对比色 | `COLOR_FOOD = (255, 0, 0)`，矩形绘制 |
| 即时按键响应 | 事件驱动模式，`pygame.event.get()` 每帧轮询 |
| 文字渲染 | `pygame.font.SysFont` + `font.render` |

### 8.3 持久化设计

| 需求 | 技术方案 |
|------|----------|
| 最高分存储 | JSON 格式写入 `highscore.json` |
| 读取容错 | 文件不存在或 JSON 解析失败时，默认最高分为 0 |
| 写入时机 | 仅在游戏结束且当前分超过历史最高分时写入 |

### 8.4 代码结构规范

- 每个模块单一职责，模块间通过明确接口通信
- 常量全部集中于 `settings.py`，避免魔法数字
- 类的公开方法有明确类型注解
- 模块间无循环依赖

---

## 9. 接口定义汇总

### 9.1 Snake 接口

    class Snake:
        def __init__(self) -> None
        def set_direction(self, new_dir: Point) -> None
        def move(self) -> None
        def grow(self) -> None
        def head(self) -> Point
        def collides_with(self, point: Point) -> bool
        def self_collision(self) -> bool
        def hits_wall(self) -> bool

### 9.2 Food 接口

    class Food:
        def __init__(self) -> None
        def respawn(self, snake_body: Deque[Point]) -> None
        @property
        def position(self) -> Point

### 9.3 ScoreManager 接口

    class ScoreManager:
        def __init__(self) -> None
        def add(self, points: int) -> None
        @property
        def current_score(self) -> int
        @property
        def high_score(self) -> int
        def update_high_score(self) -> bool

### 9.4 Game 接口

    class Game:
        def __init__(self) -> None
        def run(self) -> None          # 主循环入口
        def reset(self) -> None        # 重置游戏状态
        def handle_event(self, event) -> None
        def update(self, dt: float) -> None
        def render(self) -> None

---

## 10. 风险与约束

| 风险/约束 | 影响 | 缓解措施 |
|-----------|------|----------|
| pygame 非标准库 | 需额外安装 | 提供 `requirements.txt`，文档说明安装步骤 |
| 食物生成空间不足 | 极端情况蛇填满全屏 | 检测空闲格子为空时触发胜利结束 |
| 最高分文件权限 | 写入失败 | try/except 捕获异常，降级为内存存储 |
| 不同 OS 字体差异 | 文字显示不一致 | 使用 `SysFont` 并指定备选字体列表 |
| 方向快速切换 | 单 Tick 内多次变向导致蛇头穿越 | 方向变更仅记录到缓冲，Tick 执行时取最终方向 |

---

## 11. 交付物清单

| 交付物 | 说明 |
|--------|------|
| `main.py` | 主循环与状态控制 |
| `snake.py` | 蛇模块 |
| `food.py` | 食物模块 |
| `settings.py` | 全局配置 |
| `game_utils.py` | 计分与渲染工具 |
| `requirements.txt` | 依赖说明（`pygame`） |
| `highscore.json` | 最高分持久化文件（运行时生成） |
| 本设计文档 | TDD V1.0 |

---

*文档版本：V1.0  |  编写日期：2026-07-13  |  关联需求：Python贪吃蛇游戏需求文档 (PRD) V1.0*
