/**
 * 摸鱼游戏集中注册表
 * ——————————————————
 * 一处定义 → 游戏列表 + 路由 + iframe 页面 均从此派生
 * type: 'iframe' → 纯静态 /public/games/<id>/index.html，通过 MoyuGameEmbed 渲染
 * type: 'vue'    → 独立 Vue 组件实现（如记忆翻牌、斗地主）
 */
export interface MoyuGame {
  id: string
  path: string
  name: string
  icon: string
  desc: string
  type: 'iframe' | 'vue'
}

export const MOYU_GAMES: MoyuGame[] = [
  { id: '2048',        path: '/moyu/2048',        name: '2048',      icon: '2048', desc: '合并数字，挑战 2048（支持撤销）', type: 'iframe' },
  { id: 'snake',       path: '/moyu/snake',       name: '贪吃蛇',    icon: 'SNK',  desc: '方向键/滑动控制，吃食物变长',       type: 'iframe' },
  { id: 'puzzle15',    path: '/moyu/puzzle15',    name: '数字华容道', icon: '15P',  desc: '滑动方块，按 1~15 排列',           type: 'iframe' },
  { id: 'tetris',      path: '/moyu/tetris',      name: '俄罗斯方块', icon: 'TET',  desc: '经典下落，触摸键 + 滑动',           type: 'iframe' },
  { id: 'minesweeper', path: '/moyu/minesweeper', name: '扫雷',      icon: 'MIN',  desc: '多难度 · 长按插旗 · 计时',          type: 'iframe' },
  { id: 'breakout',    path: '/moyu/breakout',    name: '打砖块',     icon: 'BRK',  desc: '滑动挡板 + 左右键',               type: 'iframe' },
  { id: 'gomoku',      path: '/moyu/gomoku',      name: '五子棋',     icon: 'GMK',  desc: '大屏棋盘 · 触摸落子',              type: 'iframe' },
  { id: 'memory-card', path: '/moyu/memory-card', name: '记忆翻牌',  icon: 'MEM',  desc: '翻牌配对 · 记忆力挑战 · 计时排名', type: 'vue' },
  { id: 'doudizhu',    path: '/moyu/doudizhu',    name: '斗地主',     icon: 'DDZ',  desc: '完整叫地主与出牌流程，含电脑 AI',   type: 'vue' },
]

/** iframe 游戏列表（用于动态路由匹配） */
export const IFRAME_GAME_IDS = MOYU_GAMES.filter(g => g.type === 'iframe').map(g => g.id)

/** 根据 id 查找游戏 */
export function getMoyuGame(id: string): MoyuGame | undefined {
  return MOYU_GAMES.find(g => g.id === id)
}
