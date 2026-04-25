package com.xfbgy.hexmap.generator

import com.xfbgy.hexmap.data.HexMap
import com.xfbgy.hexmap.data.TerrainType
import kotlin.random.Random

/**
 * 河流生成器
 * 
 * 边编号（顺时针）：
 * - 0: 上边
 * - 1: 右上边
 * - 2: 右下边
 * - 3: 下边
 * - 4: 左下边
 * - 5: 左上边
 * 
 * 共边关系：格子A的边N 与 格子B的边(N+3)%6 是同一物理边
 * 
 * 河流规则：
 * 1. 河流只沿着两个格子之间的"共边"流动
 * 2. 每条共边只标记一次，两侧格子自动同步
 * 3. 河流从地图一条边缘进入，从对侧边缘离开
 */
object RiverGenerator {

    /**
     * 单条河流最大长度
     */
    private const val MAX_RIVER_LENGTH = 200

    /**
     * 顺时针流动方向
     */
    private const val CLOCKWISE = 0
    
    /**
     * 逆时针流动方向
     */
    private const val COUNTER_CLOCKWISE = 1

    /**
     * 生成完整地图的河流系统
     */
    fun generateRivers(hexMap: HexMap, seed: Long? = null): Boolean {
        val random = if (seed != null) Random(seed) else Random

        // 清除所有现有河流
        clearAllRivers(hexMap)

        // 生成2条河流
        val numRivers = 2

        for (i in 0 until numRivers) {
            generateSingleRiver(hexMap, random)
        }

        // 后处理：确保共边两侧都有河流（使河流变粗）
        synchronizeRiverEdges(hexMap)

        return true
    }

    /**
     * 同步河流边：确保共边两侧格子都有河流标记
     * 这样渲染时河流会显示为更粗的线
     */
    private fun synchronizeRiverEdges(hexMap: HexMap) {
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    // 如果当前格子的这条边有河流
                    if (hexMap.cells[x][y].hasRiver(dir)) {
                        // 确保相邻格子对应边也有河流
                        val neighbors = hexMap.getNeighborCoords(x, y)
                        if (dir < neighbors.size) {
                            val neighbor = neighbors[dir]
                            if (hexMap.isValidCell(neighbor.first, neighbor.second)) {
                                val oppositeDir = (dir + 3) % 6
                                hexMap.cells[neighbor.first][neighbor.second].setRiver(oppositeDir, true)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 清除所有河流
     */
    private fun clearAllRivers(hexMap: HexMap) {
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    hexMap.cells[x][y].setRiver(dir, false)
                }
            }
        }
    }

    /**
     * 生成单条河流
     */
    private fun generateSingleRiver(hexMap: HexMap, random: Random) {
        // 随机选择入口边缘：0=top, 1=right, 2=bottom, 3=left
        val border = random.nextInt(4)
        
        val startCell: Pair<Int, Int>
        val entryEdge: Int
        
        when (border) {
            0 -> {
                // y=0 顶部：纵向河流
                val candidates = getTopBorderCells(hexMap)
                if (candidates.isEmpty()) return
                startCell = candidates.random(random)
                entryEdge = 0  // 上边
            }
            1 -> {
                // x=X 右侧：横向河流
                val candidates = getRightBorderCells(hexMap)
                if (candidates.isEmpty()) return
                startCell = candidates.random(random)
                entryEdge = if (random.nextBoolean()) 1 else 2
            }
            2 -> {
                // y=Y 底部：纵向河流
                val candidates = getBottomBorderCells(hexMap)
                if (candidates.isEmpty()) return
                startCell = candidates.random(random)
                entryEdge = 3  // 下边
            }
            else -> {
                // x=0 左侧：横向河流
                val candidates = getLeftBorderCells(hexMap)
                if (candidates.isEmpty()) return
                startCell = candidates.random(random)
                entryEdge = if (random.nextBoolean()) 4 else 5
            }
        }
        
        // 生成河流路径
        generateRiverPath(hexMap, startCell.first, startCell.second, entryEdge, random)
    }
    
    /**
     * 获取顶部边缘的格子（y=0）
     */
    private fun getTopBorderCells(hexMap: HexMap): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until hexMap.width) {
            val cell = hexMap.cells[x][0]
            if (cell.terrain != TerrainType.MOUNTAIN && cell.terrain != TerrainType.URBAN) {
                cells.add(Pair(x, 0))
            }
        }
        return cells
    }
    
    /**
     * 获取底部边缘的格子（y=Y-1）
     */
    private fun getBottomBorderCells(hexMap: HexMap): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        val y = hexMap.height - 1
        for (x in 0 until hexMap.width) {
            val cell = hexMap.cells[x][y]
            if (cell.terrain != TerrainType.MOUNTAIN && cell.terrain != TerrainType.URBAN) {
                cells.add(Pair(x, y))
            }
        }
        return cells
    }
    
    /**
     * 获取左侧边缘的格子（x=0）
     */
    private fun getLeftBorderCells(hexMap: HexMap): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until hexMap.height) {
            val cell = hexMap.cells[0][y]
            if (cell.terrain != TerrainType.MOUNTAIN && cell.terrain != TerrainType.URBAN) {
                cells.add(Pair(0, y))
            }
        }
        return cells
    }
    
    /**
     * 获取右侧边缘的格子（x=X-1）
     */
    private fun getRightBorderCells(hexMap: HexMap): List<Pair<Int, Int>> {
        val cells = mutableListOf<Pair<Int, Int>>()
        val x = hexMap.width - 1
        for (y in 0 until hexMap.height) {
            val cell = hexMap.cells[x][y]
            if (cell.terrain != TerrainType.MOUNTAIN && cell.terrain != TerrainType.URBAN) {
                cells.add(Pair(x, y))
            }
        }
        return cells
    }

    /**
     * 生成河流路径
     * 
     * 核心逻辑：
     * 1. 河流从入口边进入，但入口边不标记（因为入口边通向地图外）
     * 2. 选择出口边，标记这条边为河流（共边）
     * 3. 移动到下一个格子，新格子的入口边已经通过setRiver同步设置
     * 4. 选择新的出口边，重复
     * 5. 直到河流到达对侧边缘
     */
    private fun generateRiverPath(
        hexMap: HexMap,
        startX: Int,
        startY: Int,
        entryEdge: Int,
        random: Random
    ) {
        var currentX = startX
        var currentY = startY
        
        // 入口边：河流从地图边缘流入，不标记
        val entryDir = entryEdge
        
        // 选择出口边
        val exitEdges = getExitEdges(hexMap, currentX, currentY, entryDir)
        if (exitEdges.isEmpty()) return
        
        // 根据权重选择出口边
        val exitDir = chooseExitEdge(exitEdges, entryDir, random)
        
        // 标记出口边为河流（共边，两侧格子都会标记）
        hexMap.setRiver(currentX, currentY, exitDir, true)
        
        // 获取下一个格子
        val neighbors = hexMap.getNeighborCoords(currentX, currentY)
        if (exitDir >= neighbors.size) return
        var nextX = neighbors[exitDir].first
        var nextY = neighbors[exitDir].second
        
        // 检查是否到达地图边缘
        if (!hexMap.isValidCell(nextX, nextY)) return
        
        var steps = 0
        
        while (steps < MAX_RIVER_LENGTH) {
            steps++
            
            // 当前格子的入口边是出口边的对面
            // 这条共边已经在上一个格子的setRiver时同步设置了
            val currentEntryDir = (exitDir + 3) % 6
            
            // 检查下一个格子地形
            val nextCell = hexMap.cells[nextX][nextY]
            if (nextCell.terrain == TerrainType.MOUNTAIN || nextCell.terrain == TerrainType.URBAN) break
            
            // 选择出口边（排除入口边，避免回退）
            val nextExitEdges = getExitEdges(hexMap, nextX, nextY, currentEntryDir)
            if (nextExitEdges.isEmpty()) break
            
            // 选择出口边
            val nextExitDir = chooseExitEdge(nextExitEdges, currentEntryDir, random)
            
            // 标记出口边为河流
            hexMap.setRiver(nextX, nextY, nextExitDir, true)
            
            // 获取下一个格子
            val nextNeighbors = hexMap.getNeighborCoords(nextX, nextY)
            if (nextExitDir >= nextNeighbors.size) break
            val newX = nextNeighbors[nextExitDir].first
            val newY = nextNeighbors[nextExitDir].second
            
            // 检查是否到达地图边缘
            if (!hexMap.isValidCell(newX, newY)) break
            
            // 移动到下一个格子
            currentX = nextX
            currentY = nextY
            nextX = newX
            nextY = newY
        }
    }

    /**
     * 获取可以作为出口的边
     */
    private fun getExitEdges(hexMap: HexMap, x: Int, y: Int, excludeDir: Int? = null): List<Int> {
        val exitDirs = mutableListOf<Int>()
        
        for (dir in 0..5) {
            // 排除入口边（避免回退）
            if (dir == excludeDir) continue
            
            val neighbors = hexMap.getNeighborCoords(x, y)
            if (dir >= neighbors.size) continue
            val neighbor = neighbors[dir]
            
            // 检查邻居是否有效
            if (!hexMap.isValidCell(neighbor.first, neighbor.second)) continue
            
            // 检查邻居地形
            val neighborCell = hexMap.cells[neighbor.first][neighbor.second]
            if (neighborCell.terrain == TerrainType.MOUNTAIN) continue
            if (neighborCell.terrain == TerrainType.URBAN) continue
            
            exitDirs.add(dir)
        }
        
        return exitDirs
    }

    /**
     * 根据权重选择出口边
     */
    private fun chooseExitEdge(
        exitEdges: List<Int>,
        entryDir: Int,
        random: Random
    ): Int {
        if (exitEdges.size == 1) return exitEdges[0]
        
        // 计算每个出口边的权重
        val weights = mutableMapOf<Int, Double>()
        
        // 顺时针和逆时针的相邻边权重更高
        val cwDir = (entryDir + 1) % 6
        val ccwDir = (entryDir + 5) % 6
        
        for (dir in exitEdges) {
            var weight = 1.0
            
            if (dir == cwDir) {
                weight = 5.0
            } else if (dir == ccwDir) {
                weight = 5.0
            } else {
                weight = 0.5
            }
            
            weights[dir] = weight
        }
        
        // 加权随机选择
        val totalWeight = weights.values.sum()
        var r = random.nextDouble() * totalWeight
        
        for (dir in exitEdges) {
            r -= weights[dir] ?: 0.0
            if (r <= 0) return dir
        }
        
        return exitEdges.last()
    }
}
