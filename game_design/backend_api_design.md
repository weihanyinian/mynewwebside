# 后端 API 设计文档
# 《魔法割草 · 无尽觉醒》存档系统

---

## 技术栈

```
Spring Boot 3.x
  ├─ 认证：JWT (jjwt)
  ├─ ORM：MyBatis Plus
  ├─ 数据库：MySQL 8.0
  ├─ 缓存：Redis（可选，用于Token黑名单）
  └─ 接口文档：Knife4j Swagger
```

---

## 一、数据库设计

### 1.1 用户表

```sql
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1正常 0封禁',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_login_at` DATETIME DEFAULT NULL,
  INDEX `idx_username` (`username`),
  INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 1.2 游戏存档表

```sql
CREATE TABLE `game_save` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `character_type` VARCHAR(20) NOT NULL DEFAULT 'mage' COMMENT '当前角色',
  `max_level` INT DEFAULT 1 COMMENT '历史最高等级',
  `total_kills` INT DEFAULT 0 COMMENT '历史总击杀',
  `total_games` INT DEFAULT 0 COMMENT '总游戏次数',
  `total_wins` INT DEFAULT 0 COMMENT '通关次数',
  `total_playtime` INT DEFAULT 0 COMMENT '累计游戏时长（秒）',
  `unlocked_characters` JSON COMMENT '已解锁角色列表',
  `skill_points` INT DEFAULT 0 COMMENT '剩余技能点',
  `achievements` JSON COMMENT '成就进度',
  `best_scores` JSON COMMENT '每角色最高分',
  `settings` JSON COMMENT '游戏设置（音量、画质等）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏存档表';
```

### 1.3 技能树表

```sql
CREATE TABLE `skill_tree` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `skill_id` VARCHAR(10) NOT NULL COMMENT '技能ID如A01,B02',
  `skill_level` INT DEFAULT 0 COMMENT '技能等级0-3',
  `unlocked_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次解锁时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_user_skill` (`user_id`, `skill_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户技能树';
```

---

## 二、API 接口设计

### 基础信息

```
Base URL: /api
Content-Type: application/json
认证方式: Bearer Token (JWT)
```

### 2.1 认证相关

#### 注册
```
POST /api/auth/register
Request:
{
  "username": "player001",
  "password": "yourpassword123",
  "email": "player@example.com"
}
Response 200:
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": 1,
    "username": "player001",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
Response 400:
{
  "code": 400,
  "msg": "用户名已存在"
}
```

#### 登录
```
POST /api/auth/login
Request:
{
  "username": "player001",
  "password": "yourpassword123"
}
Response 200:
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "userId": 1,
    "username": "player001",
    "nickname": "魔法少女",
    "avatarUrl": null,
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400
  }
}
```

#### 获取当前用户
```
GET /api/auth/me
Header: Authorization: Bearer <token>
Response 200:
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "player001",
    "nickname": "魔法少女",
    "avatarUrl": null,
    "status": "ACTIVE"
  }
}
Response 401:
{
  "code": 401,
  "msg": "未登录或Token已过期"
}
```

#### 登出
```
POST /api/auth/logout
Header: Authorization: Bearer <token>
Response 200:
{
  "code": 200,
  "msg": "登出成功"
}
```

---

### 2.2 存档相关

#### 获取存档
```
GET /api/game/save
Header: Authorization: Bearer <token>
Response 200:
{
  "code": 200,
  "data": {
    "characterType": "mage",
    "maxLevel": 23,
    "totalKills": 5847,
    "totalGames": 15,
    "totalWins": 3,
    "totalPlaytime": 8540,
    "unlockedCharacters": ["mage", "swordsman"],
    "skillPoints": 5,
    "achievements": {
      "kill_1000": true,
      "win_3": true,
      "reach_level_50": false
    },
    "bestScores": {
      "mage": 58420,
      "swordsman": 31200
    },
    "settings": {
      "musicVolume": 0.8,
      "sfxVolume": 1.0,
      "quality": "high"
    },
    "skillTree": {
      "A01": 3, "A02": 2, "A03": 1,
      "B01": 2, "B02": 1,
      "C01": 1, "C05": 2,
      "D01": 1
    },
    "updatedAt": "2026-04-21T22:00:00Z"
  }
}
```

#### 保存存档（完整覆盖）
```
PUT /api/game/save
Header: Authorization: Bearer <token>
Request:
{
  "characterType": "mage",
  "maxLevel": 25,
  "totalKills": 6200,
  "totalGames": 16,
  "totalWins": 3,
  "totalPlaytime": 9200,
  "unlockedCharacters": ["mage", "swordsman"],
  "skillPoints": 4,
  "achievements": {...},
  "bestScores": {...},
  "settings": {...},
  "skillTree": {
    "A01": 3, "A02": 2, "A03": 2,
    "B01": 2, "B02": 1,
    "C01": 1, "C05": 2,
    "D01": 1, "D05": 1
  }
}
Response 200:
{
  "code": 200,
  "msg": "存档成功",
  "data": {
    "updatedAt": "2026-04-21T22:15:00Z"
  }
}
```

#### 增量更新存档（推荐，减少数据传输）
```
POST /api/game/save/partial
Header: Authorization: Bearer <token>
Request:
{
  "partialType": "score",  // score | skill | achievement | settings
  "data": {
    "characterType": "mage",
    "maxLevel": 28,
    "totalKills": 7200,
    "totalGames": 17,
    "totalWins": 4,
    "bestScores": {
      "mage": 72000
    }
  }
}
Response 200:
{
  "code": 200,
  "msg": "更新成功"
}
```

#### 保存技能树
```
POST /api/game/skill
Header: Authorization: Bearer <token>
Request:
{
  "skillId": "A01",
  "level": 2  // 0-3
}
Response 200:
{
  "code": 200,
  "msg": "技能升级成功",
  "data": {
    "skillId": "A01",
    "level": 2,
    "remainingPoints": 3
  }
}
Response 400:
{
  "code": 400,
  "msg": "技能点不足"
}
```

#### 批量升级技能（重置技能点时使用）
```
POST /api/game/skill/batch
Header: Authorization: Bearer <token>
Request:
{
  "skillTree": {
    "A01": 3, "A02": 3, "A03": 2,
    "B01": 3, "B02": 2, "B03": 1,
    "C01": 3, "C05": 2, "C08": 2,
    "D01": 2, "D05": 1
  }
}
Response 200:
{
  "code": 200,
  "msg": "技能树保存成功"
}
```

---

### 2.3 游戏元数据

#### 获取技能树定义（纯配置，前端缓存）
```
GET /api/game/skills
Response 200:
{
  "code": 200,
  "data": {
    "version": "1.0.0",
    "lastUpdated": "2026-04-21",
    "skills": [
      {
        "id": "A01",
        "name": "魔法弹",
        "icon": "🌟",
        "category": "attack",
        "rarity": "common",
        "desc": "8方向发射魔法弹",
        "levels": ["8方向，每发10伤", "16方向，每发15伤", "穿透3个敌人，每发20伤"]
      },
      ... (60个技能完整定义)
    ]
  }
}
```

#### 获取游戏配置
```
GET /api/game/config
Response 200:
{
  "code": 200,
  "data": {
    "version": "1.0.0",
    "levels": [
      { "level": 1, "expRequired": 0 },
      { "level": 2, "expRequired": 10 },
      ... (1-50级完整经验曲线)
    ],
    "skillPointRewards": [5, 10, 15, 20, 25, ...],  // 每级奖励的技能点
    "waves": [...],  // 波次配置
    "enemies": [...],  // 敌人配置
    "bosses": [...]   // Boss配置
  }
}
```

---

## 三、Controller 代码结构

### 3.1 AuthController

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")     // 注册
    @PostMapping("/login")       // 登录
    @GetMapping("/me")           // 获取当前用户
    @PostMapping("/logout")      // 登出
    @PostMapping("/refresh")     // 刷新Token
}
```

### 3.2 GameController

```java
@RestController
@RequestMapping("/api/game")
public class GameController {

    @GetMapping("/save")                    // 获取存档
    @PutMapping("/save")                    // 完整保存存档
    @PostMapping("/save/partial")            // 增量更新

    @PostMapping("/skill")                  // 单个技能升级
    @PostMapping("/skill/batch")            // 批量保存技能树

    @GetMapping("/skills")                  // 获取技能定义
    @GetMapping("/config")                  // 获取游戏配置
}
```

---

## 四、安全设计

### 4.1 JWT 配置

```yaml
jwt:
  secret: "your-256-bit-secret-key-here-change-in-production"
  expiration: 86400  # 24小时
  refresh-expiration: 604800  # 7天
```

### 4.2 密码安全

- BCrypt 加密，强度 12
- 密码要求：8-32位，至少包含字母和数字
- 登录失败锁定：5次失败后锁定15分钟

### 4.3 游戏逻辑防作弊

```
重要原则：所有游戏核心逻辑（伤害计算、敌人AI、技能效果）
         全部在前端 Phaser 运行，后端只存储数据！

后端不信任任何来自客户端的：
  ❌ 伤害数值
  ❌ 击杀数量
  ✅ 击杀时间戳（用于统计）
  ✅ 波次完成状态（用于解锁内容）

后端只负责：
  ✅ 存档存取
  ✅ 排行榜数据
  ✅ 社交数据（如果有）
```

### 4.4 数据校验

```java
// 存档数据校验示例
@Validator
public class GameSaveValidator {
    public void validate(GameSaveDTO save) {
        // 角色必须在已解锁列表中
        if (!save.getUnlockedCharacters().contains(save.getCharacterType())) {
            throw new BusinessException("未解锁该角色");
        }
        // 技能等级不超过3
        for (Integer level : save.getSkillTree().values()) {
            if (level > 3) throw new BusinessException("技能等级超限");
        }
        // 技能点不能为负
        if (save.getSkillPoints() < 0) throw new BusinessException("技能点异常");
    }
}
```

---

## 五、Redis 缓存设计（可选）

```
# Token黑名单（登出时加入）
jwt:blacklist:{token} = userId
TTL: token剩余有效期

# 用户存档缓存
game:save:{userId} = JSON(GameSave)
TTL: 5分钟

# 技能定义缓存（全局）
game:skills:v1 = JSON(SkillsConfig)
TTL: 1小时
```

---

## 六、前端集成方案

### 6.1 Axios 封装

```typescript
// src/api/game.ts
import axios from 'axios'
import { useUserStore } from '@/stores/userStore'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL + '/api',
  timeout: 10000
})

// 请求拦截器：自动附加Token
api.interceptors.request.use(config => {
  const token = useUserStore().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：处理Token过期
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      useUserStore().logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export const authAPI = {
  login: (data: LoginDTO) => api.post('/auth/login', data),
  register: (data: RegisterDTO) => api.post('/auth/register', data),
  me: () => api.get('/auth/me')
}

export const gameAPI = {
  getSave: () => api.get('/game/save'),
  saveGame: (data: GameSaveDTO) => api.put('/game/save', data),
  partialSave: (type: string, data: object) =>
    api.post('/game/save/partial', { partialType: type, data }),
  upgradeSkill: (skillId: string, level: number) =>
    api.post('/game/skill', { skillId, level }),
  getSkills: () => api.get('/game/skills'),
  getConfig: () => api.get('/game/config')
}
```

### 6.2 Pinia Store

```typescript
// src/stores/userStore.ts
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token'),
    userId: null,
    username: ''
  }),

  actions: {
    async login(username, password) {
      const res = await authAPI.login({ username, password })
      this.token = res.data.token
      this.userId = res.data.userId
      this.username = res.data.username
      localStorage.setItem('token', this.token)
    },

    logout() {
      this.token = null
      this.userId = null
      localStorage.removeItem('token')
    }
  }
})

// src/stores/gameStore.ts
export const useGameStore = defineStore('game', {
  state: () => ({
    save: null as GameSave | null,
    skillsConfig: null,
    isLoading: false
  }),

  actions: {
    async loadSave() {
      this.isLoading = true
      try {
        const res = await gameAPI.getSave()
        this.save = res.data
      } finally {
        this.isLoading = false
      }
    },

    async saveGame(partial: Partial<GameSave>) {
      await gameAPI.partialSave('score', partial)
    },

    async upgradeSkill(skillId: string, level: number) {
      await gameAPI.upgradeSkill(skillId, level)
      if (this.save) {
        this.save.skillTree[skillId] = level
        this.save.skillPoints--
      }
    }
  }
})
```

### 6.3 游戏存档同步策略

```typescript
// 存档同步策略
// 1. 局内：每30秒自动存档一次（后台静默）
// 2. 局结束：立即存档（无论胜利/失败）
// 3. 技能升级：立即存档
// 4. 断线重连：自动从服务端恢复

let autoSaveTimer: number | null = null

function startAutoSave() {
  autoSaveTimer = window.setInterval(() => {
    if (gameStore.save && game.isPlaying) {
      const partialSave = {
        characterType: game.currentCharacter,
        totalKills: game.killCount,
        totalGames: gameStore.save.totalGames,
        maxLevel: Math.max(game.level, gameStore.save.maxLevel)
      }
      gameStore.saveGame(partialSave)
    }
  }, 30000)
}

function stopAutoSave() {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
}

// 游戏结束时
function onGameEnd(victory: boolean) {
  const partialSave = {
    characterType: game.currentCharacter,
    maxLevel: game.level,
    totalKills: game.killCount,
    totalGames: gameStore.save!.totalGames + 1,
    totalWins: victory ? gameStore.save!.totalWins + 1 : gameStore.save!.totalWins,
    totalPlaytime: gameStore.save!.totalPlaytime + Math.floor(game.playTime),
    bestScores: {
      ...gameStore.save!.bestScores,
      [game.currentCharacter]: Math.max(game.score, gameStore.save!.bestScores[game.currentCharacter] || 0)
    }
  }
  gameStore.saveGame(partialSave)
  stopAutoSave()
}
```

---

*API 文档版本 1.0 | 完整后可进入后端开发*
