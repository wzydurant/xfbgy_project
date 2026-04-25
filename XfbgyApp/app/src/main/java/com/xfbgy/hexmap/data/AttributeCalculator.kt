package com.xfbgy.hexmap.data

/**
 * 初始属性计算器
 *
 * 按照文档规定的叠加顺序计算所有属性：
 * Step 1: 地形 → 格子移动力消耗
 * Step 2: 地形 → 6条边的移动破坏、条件列表、进攻/防御优势
 * Step 3: 河流 → 有河流的边，叠加河流效果
 * Step 4: 防御工事 → 有工事的边，叠加工事效果
 */
object AttributeCalculator {

    /**
     * 对整个地图应用初始属性计算
     * @param hexMap 地图对象
     */
    fun calculateAll(hexMap: HexMap) {
        // 清除现有属性
        hexMap.clearAllEdges()

        // Step 1 & Step 2: 地形效果
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val cell = hexMap.getCell(x, y) ?: continue
                cell.applyTerrainEffects()
            }
        }

        // Step 3: 河流效果
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    val edge = hexMap.getEdge(x, y, dir) ?: continue
                    edge.applyRiverEffect()
                }
            }
        }

        // Step 4: 防御工事效果
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    val edge = hexMap.getEdge(x, y, dir) ?: continue
                    edge.applyFortBonus()
                }
            }
        }
    }

    /**
     * 计算所有效果（在边属性已设置后调用，只叠加不清除）
     */
    fun calculateEffects(hexMap: HexMap) {
        // Step 1 & Step 2: 地形效果（基础效果）
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                val cell = hexMap.getCell(x, y) ?: continue
                cell.applyTerrainEffects()
            }
        }

        // Step 3: 河流效果（叠加到已有河流标记）
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    val edge = hexMap.getEdge(x, y, dir) ?: continue
                    edge.applyRiverEffect()
                }
            }
        }

        // Step 4: 防御工事效果（叠加到已有防御工事标记）
        for (y in 0 until hexMap.height) {
            for (x in 0 until hexMap.width) {
                for (dir in 0..5) {
                    val edge = hexMap.getEdge(x, y, dir) ?: continue
                    edge.applyFortBonus()
                }
            }
        }
    }

    /**
     * 对单个格子应用地形效果
     */
    fun applyTerrainEffect(hexMap: HexMap, x: Int, y: Int) {
        val cell = hexMap.getCell(x, y) ?: return
        cell.applyTerrainEffects()
    }

    /**
     * 对单条边应用河流效果
     */
    fun applyRiverEffect(edge: HexEdge) {
        edge.applyRiverEffect()
    }

    /**
     * 对单条边应用防御工事效果
     */
    fun applyFortEffect(edge: HexEdge) {
        edge.applyFortBonus()
    }

    /**
     * 计算格子总防御力（含地形和所有边）
     */
    fun calculateTotalDefenseBonus(hexMap: HexMap, x: Int, y: Int): Int {
        val cell = hexMap.getCell(x, y) ?: return 0
        var total = 0
        for (dir in 0..5) {
            total += cell.edges[dir].defenseBonus
        }
        return total
    }

    /**
     * 计算格子总进攻力加成
     */
    fun calculateTotalAttackBonus(hexMap: HexMap, x: Int, y: Int): Int {
        val cell = hexMap.getCell(x, y) ?: return 0
        var total = 0
        for (dir in 0..5) {
            total += cell.edges[dir].attackBonus
        }
        return total
    }

    /**
     * 获取格子所有条件防御优势
     */
    fun getAllDefenseConditions(hexMap: HexMap, x: Int, y: Int): List<String> {
        val conditions = mutableSetOf<String>()
        val cell = hexMap.getCell(x, y) ?: return emptyList()
        for (dir in 0..5) {
            conditions.addAll(cell.edges[dir].defenseConditions)
        }
        return conditions.toList()
    }

    /**
     * 获取格子所有条件进攻优势
     */
    fun getAllAttackConditions(hexMap: HexMap, x: Int, y: Int): List<String> {
        val conditions = mutableSetOf<String>()
        val cell = hexMap.getCell(x, y) ?: return emptyList()
        for (dir in 0..5) {
            conditions.addAll(cell.edges[dir].attackConditions)
        }
        return conditions.toList()
    }

    /**
     * 获取边的完整信息描述
     */
    fun getEdgeDescription(hexMap: HexMap, x: Int, y: Int, direction: Int): String {
        val edge = hexMap.getEdge(x, y, direction) ?: return "无效边"
        val dirName = HexMap.DIR_NAMES.getOrElse(direction) { "未知" }

        return buildString {
            appendLine("方向: $dirName")
            appendLine("河流: ${if (edge.hasRiver) "有" else "无"}")
            appendLine("工事: ${edge.fortification.chineseName}")
            appendLine("移动破坏: ${edge.movementPenalty}")
            appendLine("防御优势: ${edge.defenseBonus}")
            if (edge.defenseConditions.isNotEmpty()) {
                appendLine("条件防御: ${edge.defenseConditions.joinToString("、")}")
            }
            appendLine("进攻优势: ${edge.attackBonus}")
            if (edge.attackConditions.isNotEmpty()) {
                appendLine("条件进攻: ${edge.attackConditions.joinToString("、")}")
            }
        }
    }

    /**
     * 获取格子完整信息描述
     */
    fun getCellDescription(hexMap: HexMap, x: Int, y: Int): String {
        val cell = hexMap.getCell(x, y) ?: return "无效格子"

        return buildString {
            appendLine("坐标: ($x, $y)")
            appendLine("地形: ${cell.terrain.chineseName} (${cell.terrain.colorHex})")
            appendLine("移动力消耗: ${cell.movementCost}")
            appendLine("ZOC: ${cell.getZOCDescription()}")

            val defenseBonus = calculateTotalDefenseBonus(hexMap, x, y)
            val attackBonus = calculateTotalAttackBonus(hexMap, x, y)
            val defenseCond = getAllDefenseConditions(hexMap, x, y)
            val attackCond = getAllAttackConditions(hexMap, x, y)

            appendLine("总防御优势: $defenseBonus")
            if (defenseCond.isNotEmpty()) {
                appendLine("条件防御: ${defenseCond.joinToString("、")}")
            }
            appendLine("总进攻优势: $attackBonus")
            if (attackCond.isNotEmpty()) {
                appendLine("条件进攻: ${attackCond.joinToString("、")}")
            }

            if (cell.units.isNotEmpty()) {
                appendLine("单位数: ${cell.units.size}")
            }
        }
    }
}
