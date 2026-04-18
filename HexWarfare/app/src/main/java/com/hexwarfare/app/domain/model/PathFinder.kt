package com.hexwarfare.app.domain.model

/**
 * A* 路径查找器，用于六角格地图
 */
object PathFinder {

    /**
     * 计算两点之间的最短路径
     * @param start 起点坐标
     * @param goal 终点坐标
     * @param gameMap 游戏地图
     * @param movePower 单位移动力
     * @param unit 当前单位（用于检查ZOC等）
     * @return 路径结果，包含路径、消耗和剩余移动力
     */
    fun findPath(
        start: HexCoord,
        goal: HexCoord,
        gameMap: GameMap,
        movePower: Int,
        unit: GameUnit? = null
    ): PathResult? {
        // 如果目标不可通行，直接返回null
        val goalTile = gameMap.tiles[goal] ?: return null
        if (!goalTile.terrain.canPass()) return null

        // A* 算法
        val openSet = mutableSetOf(start)
        val cameFrom = mutableMapOf<HexCoord, HexCoord>()
        val gScore = mutableMapOf(start to 0)
        val fScore = mutableMapOf(start to heuristic(start, goal))

        while (openSet.isNotEmpty()) {
            val current = openSet.minByOrNull { fScore[it] ?: Int.MAX_VALUE }!!

            if (current == goal) {
                val path = reconstructPath(cameFrom, current)
                val totalCost = gScore[current] ?: 0
                return PathResult(
                    path = path,
                    totalCost = totalCost,
                    remainingMovePower = movePower - totalCost
                )
            }

            openSet.remove(current)

            for (neighbor in current.getNeighbors()) {
                // 检查邻居是否在地图内
                if (!gameMap.tiles.containsKey(neighbor)) continue

                val tile = gameMap.tiles[neighbor]!!
                // 检查是否可通行
                if (!tile.terrain.canPass()) continue

                // 计算移动消耗
                val moveCost = getMoveCost(neighbor, gameMap, unit)
                val tentativeGScore = (gScore[current] ?: Int.MAX_VALUE) + moveCost

                if (tentativeGScore < (gScore[neighbor] ?: Int.MAX_VALUE)) {
                    cameFrom[neighbor] = current
                    gScore[neighbor] = tentativeGScore
                    fScore[neighbor] = tentativeGScore + heuristic(neighbor, goal)

                    if (neighbor !in openSet) {
                        openSet.add(neighbor)
                    }
                }
            }
        }

        return null // 没有找到路径
    }

    /**
     * 计算给定移动力范围内的所有可达格
     * @param start 起点坐标
     * @param gameMap 游戏地图
     * @param movePower 单位移动力
     * @param unit 当前单位
     * @return 可移动区域结果
     */
    fun findReachableTiles(
        start: HexCoord,
        gameMap: GameMap,
        movePower: Int,
        unit: GameUnit? = null
    ): MoveRangeResult {
        val reachable = mutableSetOf<HexCoord>()
        val moveCosts = mutableMapOf<HexCoord, Int>()
        val visited = mutableMapOf<HexCoord, Int>()

        // BFS 遍历
        val queue = ArrayDeque<Pair<HexCoord, Int>>()
        queue.add(start to 0)
        visited[start] = 0

        while (queue.isNotEmpty()) {
            val (current, currentCost) = queue.removeFirst()

            if (currentCost <= movePower) {
                reachable.add(current)
                moveCosts[current] = currentCost
            }

            for (neighbor in current.getNeighbors()) {
                // 检查邻居是否在地图内
                if (!gameMap.tiles.containsKey(neighbor)) continue

                val tile = gameMap.tiles[neighbor]!!
                // 检查是否可通行
                if (!tile.terrain.canPass()) continue

                val moveCost = getMoveCost(neighbor, gameMap, unit)
                val totalCost = currentCost + moveCost

                // 检查是否超出移动力或已有更优路径
                if (totalCost <= movePower && (neighbor !in visited || totalCost < visited[neighbor]!!)) {
                    visited[neighbor] = totalCost
                    queue.add(neighbor to totalCost)
                }
            }
        }

        // 移除起点（起点不需要高亮显示）
        reachable.remove(start)
        moveCosts.remove(start)

        return MoveRangeResult(reachable, moveCosts)
    }

    /**
     * A* 启发式函数，使用六角格距离
     */
    private fun heuristic(a: HexCoord, b: HexCoord): Int {
        return a.distanceTo(b)
    }

    /**
     * 重建路径
     */
    private fun reconstructPath(cameFrom: Map<HexCoord, HexCoord>, current: HexCoord): List<HexCoord> {
        val path = mutableListOf(current)
        var node = current
        while (cameFrom.containsKey(node)) {
            node = cameFrom[node]!!
            path.add(0, node)
        }
        return path
    }

    /**
     * 获取移动到某格的消耗
     */
    private fun getMoveCost(coord: HexCoord, gameMap: GameMap, unit: GameUnit?): Int {
        val tile = gameMap.tiles[coord] ?: return Int.MAX_VALUE
        return tile.terrain.moveCost
    }

    /**
     * 获取从起点到终点的路径，简化版本（不考虑障碍）
     */
    fun getSimplePath(start: HexCoord, goal: HexCoord): List<HexCoord> {
        val path = mutableListOf<HexCoord>()
        var current = start

        while (current != goal) {
            val neighbors = current.getNeighbors()
            val closest = neighbors.minByOrNull { it.distanceTo(goal) }
            if (closest != null) {
                current = closest
                path.add(current)
            } else {
                break
            }
        }

        return path
    }
}