# TurnManager 回合制游戏逻辑 - 参考文档

> 文件路径：`app/src/main/java/com/xfbgy/hexmap/game/TurnManager.kt`

## 概述

`TurnManager` 是回合制游戏的核心逻辑管理器，与UI完全解耦。负责玩家管理、回合轮转、格子占领/夺取/撤出逻辑和资源统计。所有UI交互由 `GameActivity` 通过回调监听器处理。

---

## 数据类

### Player

| 属性 | 类型 | 说明 |
|------|------|------|
| `id` | `Int` | 玩家唯一ID（1或2） |
| `name` | `String` | 显示名称 |
| `colorHex` | `String` | 颜色Hex值，用于UI标记 |

默认玩家：
- 玩家1：`Player(1, "玩家1", "#4CAF50")` — 绿色
- 玩家2：`Player(2, "玩家2", "#F44336")` — 红色

### OccupyResult（密封类）

| 子类 | 属性 | 说明 |
|------|------|------|
| `Success` | `cell: HexCell` | 占领成功（空格子） |
| `Takeover` | `cell: HexCell, previousOwner: Player` | 夺取对方占领的格子成功 |
| `AlreadyOwned` | `cell: HexCell` | 格子已被自己占领 |
| `InvalidCell` | 无 | 格子坐标无效 |

### WithdrawResult（密封类）

| 子类 | 属性 | 说明 |
|------|------|------|
| `Success` | `cell: HexCell` | 撤出成功 |
| `NotOwned` | `cell: HexCell` | 格子未被当前玩家占领，无法撤出 |
| `InvalidCell` | 无 | 格子坐标无效 |

---

## TurnManager 类

### 构造参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `map` | `DebugHexMap` | 游戏地图引用 |

### 属性

| 属性 | 类型 | 可写 | 说明 |
|------|------|------|------|
| `turnNumber` | `Int` | 只读 | 当前回合数（从1开始） |
| `onTurnChanged` | `((Player, Int) -> Unit)?` | 可设 | 回合变更监听器，参数=(当前玩家, 回合数) |
| `onOccupationChanged` | `((Int, Int, Player?) -> Unit)?` | 可设 | 占领变更监听器，参数=(x, y, 玩家?)，撤出时player=null |

### 初始化

构造时自动执行 `findCityClusters()` 扫描地图中的都市聚团，构建 `cityClusterMap` 映射。

### 公开方法

| 方法签名 | 返回值 | 说明 |
|----------|--------|------|
| `getCurrentPlayer()` | `Player` | 获取当前回合玩家 |
| `getPlayers()` | `List<Player>` | 获取所有玩家列表 |
| `getPlayerById(id: Int)` | `Player?` | 按ID查找玩家 |
| `endTurn()` | `Player` | 结束当前回合，轮转至下一玩家。当所有玩家都行动后回合数+1。返回切换后的当前玩家，触发 `onTurnChanged` |
| `occupyCell(x: Int, y: Int)` | `OccupyResult` | 尝试占领格子。空格子→Success；被对方占→Takeover（夺取）；已被自己占→AlreadyOwned；无效坐标→InvalidCell。都市格子占领时整个聚团一并占领。触发 `onOccupationChanged` |
| `withdrawCell(x: Int, y: Int)` | `WithdrawResult` | 撤出格子。当前玩家占领→Success（都市聚团一并撤出）；非当前玩家占→NotOwned；无效坐标→InvalidCell。触发 `onOccupationChanged`，player=null |
| `getOccupiedCellCount(playerId: Int)` | `Int` | 指定玩家占领的格子总数 |
| `getOccupiedResourceCounts(playerId: Int)` | `Map<ResourcePointType, Int>` | 指定玩家各资源类型的占领数量 |
| `getOccupiedResourceCount(playerId: Int)` | `Int` | 指定玩家占领的总资源点数量 |
| `getCellOwner(x: Int, y: Int)` | `Player?` | 查询格子占领者，null=未被占领 |
| `getPlayerColor(playerId: Int)` | `String` | 获取玩家颜色Hex值 |

### 私有方法

| 方法签名 | 说明 |
|----------|------|
| `performOccupation(cell, player)` | 执行单格占领：`clearAllZOC()` → `setZOC()` → 设置 `faction` |
| `performWithdrawal(cell, player)` | 执行单格撤出：`clearZOC()` → 重置 `faction=0` |
| `findCityClusters()` | BFS扫描都市聚团，构建 `cityClusterMap`，初始化时调用一次 |

---

## 占领规则

### 三种情况

1. **空格子**：当前玩家可以占领 → `Success`
2. **已被对方占领**：可以夺取（清除对方ZOC，设置己方ZOC） → `Takeover`
3. **已被自己占领**：不可重复占领 → `AlreadyOwned`
4. **无效坐标**：→ `InvalidCell`

### 底层操作

**占领时**（`performOccupation`）：
- `cell.clearAllZOC()` — 清除所有阵营ZOC（独占占领）
- `cell.setZOC(playerId)` — 设置当前玩家ZOC
- `rp.faction = playerId` — 设置资源点归属

**撤出时**（`performWithdrawal`）：
- `cell.clearZOC(playerId)` — 仅清除当前玩家ZOC
- `rp.faction = 0` — 重置资源点归属为未占领

---

## 都市聚团机制

### 原理

都市（CITY）资源点的格子通常成组出现（聚团），由 `ResourcePointScanner` 通过BFS连通判定生成。`TurnManager` 初始化时同样用BFS扫描所有CITY格子，构建聚团映射。

### 聚团映射 `cityClusterMap`

- 类型：`Map<Pair<Int, Int>, List<Pair<Int, Int>>>`
- 每个CITY格子坐标 → 同一聚团所有格子坐标列表
- 非CITY格子的key不存在于map中

### 聚团联动规则

| 操作 | 联动行为 |
|------|----------|
| 占领CITY格子 | 聚团内所有格子一并被当前玩家占领 |
| 夺取CITY格子 | 聚团内所有格子一并被夺取（清除对方ZOC） |
| 撤出CITY格子 | 聚团内所有格子一并撤出 |

---

## 回合轮转逻辑

```
玩家1 → 玩家2 → 玩家1 → 玩家2 → ...
                     ↑
              回合数+1发生在此处
              （所有玩家各行动一次后）
```

- `currentPlayerIndex` 从0开始
- 每次调用 `endTurn()`，索引 = (index + 1) % players.size
- 当索引回到0时，`turnNumber++`

---

## 依赖关系

```
TurnManager
  ├── DebugHexMap（地图数据）
  │     ├── HexCell.clearAllZOC() / setZOC() / isControlledBy() / clearZOC()
  │     ├── HexCell.resourcePoint?.faction
  │     ├── getNeighborCoord() / isValidCell()
  │     └── cells[][] / width / height
  ├── ResourcePointType（枚举：村庄/城镇/都市/马场）
  └── Player（数据类）
```

---

## UI 集成方式

`GameActivity` 通过以下方式与 `TurnManager` 交互：

1. **初始化**：`TurnManager(map)` 创建后赋值给 `GameMapGridView.turnManager`
2. **回调监听**：
   - `onTurnChanged` → 更新玩家面板和回合信息
   - `onOccupationChanged` → 更新玩家面板和地图重绘（撤出时player=null）
3. **用户操作**：
   - 点击空格子 → `occupyCell()` → 弹窗"是否占领"
   - 点击己方格子 → `withdrawCell()` → 弹窗"是否撤出"
   - 点击对方格子 → `occupyCell()` → 弹窗"是否夺取"
   - 结束回合 → `endTurn()` 切换玩家
