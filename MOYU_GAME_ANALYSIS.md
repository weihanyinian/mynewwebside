# 摸鱼小游戏模块 — 全局分析与移动端适配方案

> 2026-05-15 | 基于代码审查与移动端测试

## 执行状态

| Phase | 内容 | 状态 |
|-------|------|------|
| — | 删除猜数字、贪吃蛇、Snakeball（3 款游戏） | ✅ 完成 |
| 1 | 斗地主返回按钮修复 | ✅ 完成 |
| 2 | 记忆翻牌迁移到 MoyuGameShell | ✅ 完成 |
| 3 | 创建 MoyuTopBar 全局顶栏组件 | ✅ 完成 |
| 4 | 移动端触控增强 | ✅ 完成 |

当前 7 款游戏：2048、斗地主、俄罗斯方块、扫雷、打砖块、五子棋、记忆翻牌

---

## 一、模块架构

### 1.1 游戏总览（10 款）

| 游戏 | 路由 | 渲染方式 | 外壳组件 |
|------|------|----------|----------|
| 2048 | `/moyu/2048` | iframe 嵌入 (`/public/games/2048/`) | `MoyuGameEmbed` → `MoyuGameShell` |
| 贪吃蛇 | `/moyu/snake` | iframe 嵌入 (`/public/games/snake/`) | `MoyuGameEmbed` → `MoyuGameShell` |
| 俄罗斯方块 | `/moyu/tetris` | iframe 嵌入 (`/public/games/tetris/`) | `MoyuGameEmbed` → `MoyuGameShell` |
| 扫雷 | `/moyu/minesweeper` | iframe 嵌入 (`/public/games/minesweeper/`) | `MoyuGameEmbed` → `MoyuGameShell` |
| 打砖块 | `/moyu/breakout` | iframe 嵌入 (`/public/games/breakout/`) | `MoyuGameEmbed` → `MoyuGameShell` |
| 五子棋 | `/moyu/gomoku` | iframe 嵌入 (`/public/games/gomoku/`) | `MoyuGameEmbed` → `MoyuGameShell` |
| 猜数字 | `/moyu/guess` | **Vue 原生** | **无外壳（自定义布局）** |
| 记忆翻牌 | `/moyu/memory-card` | **Vue 原生** | **无外壳（自定义布局）** |
| Snakeball | `/moyu/snakeball` | 外部 iframe | `MoyuGameShell`（直接使用） |
| 斗地主 | `/moyu/doudizhu` | **独立 Vue 子应用** | **无外壳（自定义 GameBoard）** |

### 1.2 共享组件

```
components/moyu/
├── MoyuGameShell.vue       # 毛玻璃统一外壳（安全区、返回、标题、统计、加载态、插槽）
├── MoyuGameEmbed.vue       # iframe 游戏包装器（使用 MoyuGameShell + 游戏结束弹窗）
└── MoyuBackToHubButton.vue # 返回摸鱼中心按钮（→ /moyu）

public/games/_shared/
├── bridge.js               # iframe ↔ 父页通信桥（postMessage: stats / gameover / theme）
└── base.css                # iframe 内共享样式（moyu-* 设计变量、画布、DPad）
```

### 1.3 导航关系

```
摸鱼中心 (/moyu)  ←─── MoyuBackToHubButton
    ├── 2048, snake, tetris, minesweeper, breakout, gomoku
    │     └── MoyuGameShell → MoyuBackToHubButton → /moyu  ✅
    ├── 猜数字, 记忆翻牌
    │     └── 自定义布局 → MoyuBackToHubButton → /moyu  ✅ (但无统一外壳)
    ├── snakeball
    │     └── MoyuGameShell → MoyuBackToHubButton → /moyu  ✅
    └── 斗地主
          └── GameBoard → BackToHomeButton → / (首页!)  ❌ BUG
```

---

## 二、Bug 清单

### 2.1 导航不一致（严重）

| 问题 | 位置 | 影响 |
|------|------|------|
| **斗地主返回首页而非摸鱼中心** | `GameBoard.vue:323` 使用 `BackToHomeButton`（→ `/`） | 用户从斗地主无法回到游戏列表 |
| **猜数字/记忆翻牌未使用统一外壳** | 各自实现 layout、back button、header | 新增全局导航功能时需修改 2 个额外文件 |
| **4 种不同的页面布局模式** | iframe 嵌入 / Vue 原生 / 外部 iframe / 独立子应用 | 维护成本高，样式不一致 |

### 2.2 斗地主主题不跟随

`GameBoard.vue` 使用硬编码样式：
```html
<div class="min-h-screen bg-[radial-gradient(circle_at_top,#0f5132,#0b3d28_38%,#07251a)] ... text-white">
```
不响应全局亮/暗模式切换，与其他游戏的毛玻璃风格不一致。

### 2.3 猜数字 CSS 类名拼写错误

`GameGuessNumber.vue:348` — `.stat.l` 应为 `.stat-l`。

### 2.4 移动端问题汇总

| 问题 | 影响游戏 | 严重度 |
|------|----------|--------|
| **iframe viewport 禁止缩放** | 所有 iframe 游戏启用了 `maximum-scale=1,user-scalable=no` | 低（游戏通常不需缩放，但影响无障碍） |
| **小型触控目标** | 扫雷（~32px 格子）、2048（~72px 格子）、五子棋（~40px 交叉点） | 中 — 手指误触 |
| **虚拟键盘遮挡输入** | 猜数字（数字输入框可能被键盘遮挡） | 中 |
| **画布 touch-action 不一致** | Snake/Tetris 的 canvas 使用 `touch-action: none`，但 Breakout/Gomoku 未明确设置 | 中 — 页面滑动冲突 |
| **DPad 按钮过小** | Snake 方向键、Tetris 控制键(56px)、Breakout 左右键 | 低 |
| **无横屏提示** | 俄罗斯方块、打砖块更适合横屏，但无提示 | 低 |
| **缺少触觉反馈** | 所有游戏 | 低 |

---

## 三、全局适配方案

### 3.1 统一所有游戏外壳 → `MoyuGameShell`

**目标**：所有 10 款游戏使用同一外壳组件，顶部导航栏在一处定义，全局生效。

```
改造前:
  猜数字      → 自定义 div.guess-page + MoyuBackToHubButton
  记忆翻牌    → 自定义 div.memory-page + MoyuBackToHubButton
  斗地主      → 自定义 GameBoard + BackToHomeButton ❌
  Snakeball  → MoyuGameShell (直接)
  其余6款    → MoyuGameEmbed → MoyuGameShell

改造后:
  全部10款    → 统一外壳（MoyuGameShell 或增强版）
```

**实施**：
- 猜数字/记忆翻牌：移除自定义 layout，包裹 `MoyuGameShell`
- 斗地主：`GameBoard` 最外层替换为 `MoyuGameShell`，`BackToHomeButton` → `MoyuBackToHubButton`
- Snakeball：保持不变（已使用 `MoyuGameShell`）
- iframe 游戏：保持不变（已使用 `MoyuGameEmbed` → `MoyuGameShell`）

### 3.2 创建全局游戏顶栏组件 `MoyuTopBar`

将 `MoyuGameShell` 的 header 提取为独立组件，便于未来扩展：

```vue
<MoyuTopBar
  title="2048"
  high-text="最高分 2048"
  :show-back="true"
  :show-theme-toggle="true"   <!-- 未来功能 -->
  :show-settings="false"       <!-- 未来功能 -->
/>
```

**好处**：新增功能（如"分享战绩"、"排行榜入口"）只需改 `MoyuTopBar` 一处，全部游戏生效。

### 3.3 移动端适配改进

| 改进项 | 方案 |
|--------|------|
| **安全区统一** | `MoyuGameShell` 已处理 `safe-area-inset-*`，统一后全部游戏受益 |
| **触控优化** | iframe `base.css` 添加 `touch-action: manipulation` 全局，canvas 添加 `touch-action: none` |
| **DPad 增强** | 最小触控区域提升至 48×48px（WCAG 2.1），添加 `:active` 缩放反馈 |
| **键盘避让** | 猜数字输入框使用 `visualViewport` API 或 `scrollIntoView` 保证可见 |
| **小型触控目标** | 扫雷格子设为 `min(var(--cell, 36px), 10vw)` 保证最小尺寸；五子棋棋盘增大 |
| **横屏提示** | 俄罗斯方块/打砖块检测 `matchMedia('(orientation: portrait)')` 时显示旋转建议 |

### 3.4 主题同步

- **iframe 游戏**：`bridge.js` 通过 `postMessage({ action: 'theme' })` 实时同步（已实现）
- **Vue 原生游戏**：使用 `var(--text-color)` 等 CSS 变量自动响应（已实现）
- **斗地主**：需改造为使用 CSS 变量或 Tailwind `dark:` 前缀替代硬编码颜色

---

## 四、实施步骤

### Step 1: 统一导航 — 修复斗地主返回按钮
- `GameBoard.vue`: `BackToHomeButton` → `MoyuBackToHubButton`
- 确保斗地主可以正常返回 `/moyu`

### Step 2: 统一外壳 — 猜数字 + 记忆翻牌迁移
- `GameGuessNumber.vue`: 移除自定义 `.guess-page` 布局，包裹 `MoyuGameShell`
- `GameMemoryCard.vue`: 移除自定义 `.memory-page` 布局，包裹 `MoyuGameShell`
- 删除冗余的 CSS（safe-area、fadeIn、header 等由壳提供）

### Step 3: 创建 `MoyuTopBar` 组件
- 从 `MoyuGameShell` 提取 header 部分
- 暴露插槽和 props 支持未来扩展

### Step 4: 移动端增强
- iframe `base.css`: 统一 touch-action 策略
- DPad 按钮最小尺寸 48×48px
- 猜数字键盘避让

### Step 5: 斗地主主题适配
- `GameBoard.vue` 硬编码颜色 → CSS 变量 / Tailwind dark mode

---

## 五、风险评估

| 风险 | 缓解 |
|------|------|
| 更改外壳可能破坏现有游戏 | 逐个游戏测试，保留降级方案 |
| iframe 游戏通信复杂 | `bridge.js` 不变，保持向后兼容 |
| 斗地主改造影响大 | 仅改导航按钮和主题，不碰核心逻辑 |
| 移动端 DPad 改动可能影响手感 | 保留现有尺寸作为 min 值，仅增大下限 |

---

## 六、后续建议（非本次范围）

- PWA manifest + service worker 支持离线游玩
- 游戏全局排行榜（后端存储，替代 localStorage）
- 触觉反馈 API (`navigator.vibrate`)
- 游戏内截屏分享功能
- 斗地主 Vue 2 → Vue 3 迁移（`doudizhu/main.js` 仍为独立入口）
