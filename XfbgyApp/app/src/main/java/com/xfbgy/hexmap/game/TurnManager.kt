package com.xfbgy.hexmap.game

import com.xfbgy.hexmap.data.DebugHexMap
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.data.ResourcePointType

/**
 * 玩家数据类
 *
 * @param id 玩家唯一ID（1或2）
 * @param name 显示名称
 * @param colorHex 颜色Hex值（用于UI标记）
 */
data class Player(
    val id: Int,
    val name: String,
    val colorHex: String
)

/**
 * 占领结果
 */
sealed class OccupyResult {
    /** 占领成功（空格子） */
    data class Success(val cell: HexCell) : OccupyResult()

    /** 共享占领成功（格子已被对方占领，当前玩家也占领了该格） */
    data class SharedOccupation(val cell: HexCell, val existingOwners: List<Player>) : OccupyResult()

    /** 格子已被自己占领 */
    data class AlreadyOwned(val cell: HexCell) : OccupyResult()

    /** 格子无效 */
    object InvalidCell : OccupyResult()
}

/**
 * 撤出结果
 */
sealed class WithdrawResult {
    /** 撤出成功 */
    data class Success(val cell: HexCell) : WithdrawResult()

    /** 格子未被当前玩家占领，无法撤出 */
    data class NotOwned(val cell: HexCell) : WithdrawResult()

    /** 格子无效 */
    object InvalidCell : WithdrawResult()
}

/**
 * 回合制游戏管理器
 *
 * 职责：
 * - 管理玩家列表和回合轮转
 * - 处理格子占领逻辑
 * - 统计玩家占领的资源信息
 * - 不包含任何UI逻辑
 */
class TurnManager(
    private val map: DebugHexMap
) {
    /** 玩家列表 */
    private val players: List<Player> = listOf(
        Player(1, "玩家1", "#4CAF50"),   // 绿色
        Player(2, "玩家2", "#F44336")    // 红色
    )

    /** 当前回合玩家索引 */
    private var currentPlayerIndex: Int = 0

    /** 当前回合数（从1开始） */
    var turnNumber: Int = 1
        private set

    /** 回合变更监听器 */
    var onTurnChanged: ((currentPlayer: Player, turnNumber: Int) -> Unit)? = null

    /** 占领变更监听器 */
    var onOccupationChanged: ((x: Int, y: Int, player: Player?) -> Unit)? = null

    /** 资源管理器（由外部设置） */
    var resourceManager: ResourceManager? = null

    /** 都市聚团映射：格子坐标 -> 同一聚团的所有格子坐标列表 */
    private val cityClusterMap: Map<Pair<Int, Int>, List<Pair<Int, Int>>>

    init {
        cityClusterMap = findCityClusters()
    }

    /**
     * 获取当前回合玩家
     */
    fun getCurrentPlayer(): Player = players[currentPlayerIndex]

    /**
     * 获取所有玩家
     */
    fun getPlayers(): List<Player> = players

    /**
     * 获取指定ID的玩家
     */
    fun getPlayerById(id: Int): Player? = players.find { it.id == id }

    /**
     * 结束当前回合，切换到下一个玩家
     *
     * 注意：资源结算由外部（GameActivity）在调用 endTurn() 之前手动触发
     *
     * @return 切换后的当前玩家
     */
    fun endTurn(): Player {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        if (currentPlayerIndex == 0) {
            turnNumber++
        }
        val currentPlayer = getCurrentPlayer()
        onTurnChanged?.invoke(currentPlayer, turnNumber)
        return currentPlayer
    }

    /**
     * 判断当前玩家是否为回合中最后一个行动的玩家
     * （即调用endTurn后回合数会+1）
     */
    fun isLastPlayerInRound(): Boolean = currentPlayerIndex == players.size - 1

    /**
     * 尝试占领指定格子（共享占领）
     *
     * 规则：
     * - 空格子可以被当前玩家占领
     * - 已被对方占领的格子可以共享占领（不清除对方ZOC）
     * - 已被自己占领的格子不可重复占领
     * - 都市格子占领时，同一聚团的所有格子一并占领
     * - 无效坐标返回InvalidCell
     *
     * @param x 格子列坐标
     * @param y 格子行坐标
     * @return 占领结果
     */
    fun occupyCell(x: Int, y: Int): OccupyResult {
        if (!map.isValidCell(x, y)) {
            return OccupyResult.InvalidCell
        }

        val cell = map.cells[x][y]
        val currentPlayer = getCurrentPlayer()

        // 检查是否已被当前玩家占领
        if (cell.isControlledBy(currentPlayer.id)) {
            return OccupyResult.AlreadyOwned(cell)
        }

        // 检查是否被对方占领（共享占领情况）
        val existingOwners = getCellOwners(x, y)

        // 执行占领（主格子）—不清除已有ZOC，叠加当前玩家
        performOccupation(cell, currentPlayer)

        // 如果是都市格子，占领整个聚团
        val cluster = cityClusterMap[Pair(x, y)]
        if (cluster != null) {
            for ((cx, cy) in cluster) {
                if (cx == x && cy == y) continue  // 已占领
                val clusterCell = map.cells[cx][cy]
                if (!clusterCell.isControlledBy(currentPlayer.id)) {
                    performOccupation(clusterCell, currentPlayer)
                }
            }
        }

        onOccupationChanged?.invoke(x, y, currentPlayer)

        return if (existingOwners.isNotEmpty()) {
            OccupyResult.SharedOccupation(cell, existingOwners)
        } else {
            OccupyResult.Success(cell)
        }
    }

    /**
     * 执行单个格子的占领操作（内部方法）
     * 共享占领：不清除已有ZOC，直接叠加当前玩家
     */
    private fun performOccupation(cell: HexCell, player: Player) {
        cell.setZOC(player.id)
        // 资源点归属：如果已有归属则不覆盖（保持先占者归属）；无归属则设置
        cell.resourcePoint?.let { rp ->
            if (rp.faction == 0) {
                rp.faction = player.id
            }
        }
    }

    /**
     * 撤出指定格子
     *
     * 规则：
     * - 只有当前玩家占领的格子才能撤出
     * - 都市格子撤出时，同一聚团的所有格子一并撤出
     * - 撤出后格子变为未占领状态
     *
     * @param x 格子列坐标
     * @param y 格子行坐标
     * @return 撤出结果
     */
    fun withdrawCell(x: Int, y: Int): WithdrawResult {
        if (!map.isValidCell(x, y)) {
            return WithdrawResult.InvalidCell
        }

        val cell = map.cells[x][y]
        val currentPlayer = getCurrentPlayer()

        if (!cell.isControlledBy(currentPlayer.id)) {
            return WithdrawResult.NotOwned(cell)
        }

        // 执行撤出（主格子）
        performWithdrawal(cell, currentPlayer)

        // 如果是都市格子，撤出整个聚团
        val cluster = cityClusterMap[Pair(x, y)]
        if (cluster != null) {
            for ((cx, cy) in cluster) {
                if (cx == x && cy == y) continue  // 已撤出
                val clusterCell = map.cells[cx][cy]
                if (clusterCell.isControlledBy(currentPlayer.id)) {
                    performWithdrawal(clusterCell, currentPlayer)
                }
            }
        }

        onOccupationChanged?.invoke(x, y, null)
        return WithdrawResult.Success(cell)
    }

    /**
     * 执行单个格子的撤出操作（内部方法）
     * 只清除自己的ZOC，不影响其他玩家
     */
    private fun performWithdrawal(cell: HexCell, player: Player) {
        cell.clearZOC(player.id)
        cell.resourcePoint?.let { rp ->
            if (rp.faction == player.id) {
                // 如果还有其他玩家占领，归属转移给另一个玩家
                val otherOwner = players.find { it.id != player.id && cell.isControlledBy(it.id) }
                rp.faction = otherOwner?.id ?: 0
            }
        }
    }

    /**
     * 查找都市聚团（BFS连通的都市格子）
     * 仅在初始化时调用一次
     */
    private fun findCityClusters(): Map<Pair<Int, Int>, List<Pair<Int, Int>>> {
        val cityCells = mutableSetOf<Pair<Int, Int>>()
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                if (map.cells[x][y].resourcePoint?.type == ResourcePointType.CITY) {
                    cityCells.add(Pair(x, y))
                }
            }
        }

        val visited = mutableSetOf<Pair<Int, Int>>()
        val clusterMap = mutableMapOf<Pair<Int, Int>, List<Pair<Int, Int>>>()

        for (cell in cityCells) {
            if (cell in visited) continue

            // BFS扩展连通的都市格子
            val cluster = mutableListOf<Pair<Int, Int>>()
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(cell)
            visited.add(cell)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                cluster.add(current)

                for (dir in 0..5) {
                    val (nx, ny) = map.getNeighborCoord(current.first, current.second, dir)
                    val neighbor = Pair(nx, ny)
                    if (neighbor in visited) continue
                    if (!map.isValidCell(nx, ny)) continue
                    if (map.cells[nx][ny].resourcePoint?.type != ResourcePointType.CITY) continue
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }

            // 聚团内每个格子映射到整个聚团
            for (member in cluster) {
                clusterMap[member] = cluster
            }
        }

        return clusterMap
    }

    /**
     * 获取指定玩家占领的格子数量
     */
    fun getOccupiedCellCount(playerId: Int): Int {
        var count = 0
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                if (map.cells[x][y].isControlledBy(playerId)) {
                    count++
                }
            }
        }
        return count
    }

    /**
     * 获取指定玩家占领的各资源类型数量
     *
     * @return Map<ResourcePointType, Int> 资源类型 -> 占领数量
     */
    fun getOccupiedResourceCounts(playerId: Int): Map<ResourcePointType, Int> {
        val counts = mutableMapOf<ResourcePointType, Int>()
        ResourcePointType.entries.forEach { counts[it] = 0 }

        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                val cell = map.cells[x][y]
                if (cell.isControlledBy(playerId)) {
                    val rp = cell.resourcePoint
                    if (rp != null) {
                        counts[rp.type] = counts[rp.type]!! + 1
                    }
                }
            }
        }
        return counts
    }

    /**
     * 获取指定玩家占领的总资源点数量
     */
    fun getOccupiedResourceCount(playerId: Int): Int {
        var count = 0
        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                val cell = map.cells[x][y]
                if (cell.isControlledBy(playerId) && cell.resourcePoint != null) {
                    count++
                }
            }
        }
        return count
    }

    /**
     * 查询格子的占领玩家
     *
     * @return 占领该格子的玩家，null表示未被占领
     */
    fun getCellOwner(x: Int, y: Int): Player? {
        if (!map.isValidCell(x, y)) return null
        val cell = map.cells[x][y]
        for (player in players) {
            if (cell.isControlledBy(player.id)) {
                return player
            }
        }
        return null
    }

    /**
     * 查询格子所有占领玩家（共享占领）
     *
     * @return 占领该格子的所有玩家列表，空列表表示未被占领
     */
    fun getCellOwners(x: Int, y: Int): List<Player> {
        if (!map.isValidCell(x, y)) return emptyList()
        val cell = map.cells[x][y]
        return players.filter { cell.isControlledBy(it.id) }
    }

    /**
     * 获取玩家颜色（用于UI绘制）
     */
    fun getPlayerColor(playerId: Int): String {
        return getPlayerById(playerId)?.colorHex ?: "#888888"
    }
}
