# GameActivity 游戏页面 - 参考文档

> 文件路径：`app/src/main/java/com/xfbgy/hexmap/ui/GameActivity.kt`

## 概述

`GameActivity` 是游戏主页面，全屏显示六角格地图，支持回合制操作。负责：
- 地图生成与显示
- 回合制UI交互（玩家面板、结束回合、占领弹窗）
- 格子信息展示
- 地形/建筑/河流/资源点生成算法

---

## 页面布局结构

```
FrameLayout（根布局，深色背景 #0D1117）
├── GameMapGridView          （全屏地图，支持拖动/缩放）
├── mapInfoBar: TextView     （顶部地图统计信息，半透明背景）
├── cellInfoPanel: TextView  （底部格子详情面板，半透明，默认隐藏）
├── bottomBar: LinearLayout  （底部操作栏，48dp高）
│   ├── playerScrollView: HorizontalScrollView （可滑动，防止小屏幕展示不全）
│   │   └── playerInfoBar: LinearLayout         （玩家信息区域，水平排列）
│   │       ├── 玩家1面板（纵向：名称 + 资源统计）
│   │       └── 玩家2面板（纵向：名称 + 资源统计）
│   └── endTurnButton: Button       （"结束回合"按钮，蓝色 #1E90FF）
└── turnInfoText: TextView   （右上角回合信息浮窗，橙色 #FF9800）
```

### 布局关键参数

| 组件 | 位置 | 背景色 | 文字色 |
|------|------|--------|--------|
| mapInfoBar | 顶部 | 半透明黑 | #B0FFFFFF |
| cellInfoPanel | 底部（bottomBar上方） | 半透明黑 | #E0E0E0 |
| bottomBar | 底部 | #CC000000 | — |
| turnInfoText | 右上角 | 半透明黑 | #FF9800 |
| endTurnButton | bottomBar右侧 | #1E90FF | 白色 |

---

## Intent 参数

| Key | 类型 | 默认值 | 说明 |
|-----|------|--------|------|
| `EXTRA_MAP_SIZE` | `Int` | 10 | 地图边长 |
| `EXTRA_RIVER_COUNT` | `Int` | 2 | 河流条数 |

---

## 成员变量

| 变量 | 类型 | 说明 |
|------|------|------|
| `mapGridView` | `GameMapGridView` | 地图自定义View |
| `cellInfoPanel` | `TextView` | 格子详情面板 |
| `mapInfoBar` | `TextView` | 顶部地图统计 |
| `playerInfoBar` | `LinearLayout` | 玩家信息区域容器 |
| `turnInfoText` | `TextView` | 右上角回合浮窗 |
| `endTurnButton` | `Button` | 结束回合按钮 |
| `currentMap` | `DebugHexMap?` | 当前地图数据 |
| `turnManager` | `TurnManager?` | 回合管理器 |

---

## 核心方法

### 生命周期

| 方法 | 说明 |
|------|------|
| `onCreate()` | 读取Intent参数 → 创建布局 → 自动生成地图 |

### 布局创建

| 方法 | 说明 |
|------|------|
| `createLayout()` | 构建全屏FrameLayout布局，包含所有UI组件 |

### 地图生成

| 方法 | 说明 |
|------|------|
| `generateAndShowMap(size, riverCount)` | 主入口：生成地形、河流、资源点、工事，显示地图，初始化回合管理器 |
| `generateUrbanCells(size, random)` | 11步算法生成建筑群聚团和离散建筑，返回 `(clusterSet, discreteSet)` |
| `assignUrbanFortifications(map, urbanCells, clusterSet, random)` | 分配防御工事：聚团→石墙、离散→2/3栅栏+1/3土墙 |
| `expandToCluster(center, size, occupied, random)` | 从中心点扩展为2~3格聚团 |
| `countClusters(clusterSet)` | BFS统计聚团数量 |
| `hexDistance(p1, p2)` | 六角格距离计算（cube坐标） |
| `getNeighborCoords(x, y, size)` | 获取六角格6个邻居坐标（奇偶行偏移） |
| `removeNeighborsFromSets(...)` | 从空图集中移除已占据格子的邻居 |

### 回合制交互

| 方法 | 说明 |
|------|------|
| `initTurnManager()` | 创建 `TurnManager`，绑定回调监听，设置 `mapGridView.turnManager` |
| `onCellSelected(x, y)` | 格子点击回调：更新信息面板 + 弹出操作弹窗 |
| `showOccupyDialog(x, y)` | 根据格子占领状态弹出不同弹窗：未占领→"是否占领"、己方→"是否撤出"、对方→"是否夺取" |
| `handleOccupyResult(result, player, x, y)` | 处理占领/夺取结果，更新信息面板 |
| `updateCellInfoAfterOccupy(player, x, y)` | 占领/夺取后更新面板文字 |
| `updateCellInfoAfterWithdraw(x, y)` | 撤出后更新面板文字 |
| `onEndTurnClicked()` | 结束回合按钮：调用 `turnManager.endTurn()`，隐藏信息面板 |
| `updatePlayerInfoBar()` | 刷新底部玩家面板：名称（当前玩家高亮+▸箭头）、占领格数、资源统计 |
| `updateTurnInfo()` | 刷新右上角回合信息："第N回合 \| 玩家名" |

### 工具方法

| 方法 | 说明 |
|------|------|
| `dpToPx(dp)` | dp转px |
| `parsePlayerColor(colorHex)` | 解析颜色Hex字符串 |

---

## 地图生成算法（11步）

详见 `docs/地图类型算法生成.md`，核心流程：

1. 在 `x < n/6` 区域放聚团1
2. 移除聚团坐标 + 距聚团 `< 2n/3` 的坐标
3. 空图集放聚团2并移除邻居
4. 统计聚团数 x
5. 添加 x×5 离散建筑
6-9. 循环至总建筑 ≥ m×10% 或空图集空
10. 设 URBAN 地形
11. 剩余格子：60%平原 / 15%山地 / 10%森林 / 5%高山（归一化90%），高山须邻山地

### 地形比例

| 地形 | 比例 | 备注 |
|------|------|------|
| 平原 | 60% | 剩余非建筑格子 |
| 山地 | 15% | 随机分布 |
| 森林 | 10% | 随机分布 |
| 高山 | 5% | 必须邻山地 |
| 城市 | ~10% | 算法生成 |

---

## 格子操作弹窗逻辑

```
用户点击格子
  └─ onCellSelected(x, y)
       ├─ 更新 cellInfoPanel（坐标、地形、资源点、占领状态、6边信息）
       └─ showOccupyDialog(x, y)
            ├─ 未被占领 → 弹出"是否占领"弹窗
            │    ├─ "是" → turnManager.occupyCell(x, y)
            │    │    ├─ Success → 更新面板文字 "未占领" → "已占领: 玩家N"
            │    │    └─ 其他结果 → 忽略
            │    └─ "否" → 关闭弹窗
            ├─ 己方占领 → 弹出"是否撤出"弹窗（都市显示"聚团一并撤出"提示）
            │    ├─ "是" → turnManager.withdrawCell(x, y)
            │    │    ├─ Success → 更新面板文字 "已占领: 玩家N" → "未占领"
            │    │    └─ 其他结果 → 忽略
            │    └─ "否" → 关闭弹窗
            └─ 对方占领 → 弹出"是否夺取"弹窗（都市显示"聚团一并夺取"提示）
                 ├─ "是" → turnManager.occupyCell(x, y)
                 │    ├─ Takeover → 更新面板文字为当前玩家
                 │    ├─ Success → 更新面板文字
                 │    └─ 其他结果 → 忽略
                 └─ "否" → 关闭弹窗
```

---

## 玩家面板显示

每个玩家面板包含：
- **名称行**：当前玩家显示 `▸ 玩家N`（加粗+玩家颜色），非当前玩家显示 `  玩家N`
- **统计行**：`格:X 资源:Y 类型图标数量...`
- 当前玩家面板有橙色高亮背景 `#40FF9800`

---

## 依赖关系

```
GameActivity
  ├── GameMapGridView          （地图自定义View）
  │     └── turnManager        （占领标记绘制）
  ├── TurnManager              （回合逻辑管理）
  │     ├── onTurnChanged      → updatePlayerInfoBar() + updateTurnInfo()
  │     └── onOccupationChanged → updatePlayerInfoBar() + invalidate()
  ├── DebugHexMap              （地图数据）
  ├── DebugRiverGenerator      （河流生成）
  ├── ResourcePointScanner     （资源点识别）
  ├── OccupyResult             （占领结果密封类：Success/Takeover/AlreadyOwned/InvalidCell）
  └── WithdrawResult           （撤出结果密封类：Success/NotOwned/InvalidCell）
```

---

## 后续开发注意事项

1. **玩家扩展**：当前硬编码2个玩家，扩展时需修改 `TurnManager.players` 初始化
2. **占领规则增强**：可在 `TurnManager.occupyCell()` 中添加更多限制（如邻接己方领地、消耗资源等）
3. **地图生成参数**：地形比例、建筑比例等硬编码在 `generateAndShowMap()` 中，可抽取为配置
4. **UI优化**：当前使用纯代码创建布局，后续可迁移到 XML 布局或 Compose
5. **状态恢复**：`onSaveInstanceState` 尚未实现，屏幕旋转会丢失回合状态
