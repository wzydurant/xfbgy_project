package com.xfbgy.hexmap.data

/**
 * 资源点数据类
 *
 * 存储资源点的基础信息和资源储存，关联到 HexCell
 *
 * @param type 资源点类型
 * @param faction 归属阵营（0=未占领, 1/2/3=阵营编号）
 * @param isSuppressed 是否被压制
 * @param manpowerStock 人力储存
 * @param cattleStock 牛储存
 * @param horseStock 马储存
 * @param equipStock 各类装备数量
 * @param productionQueue 装备生产序列（村庄/马场无此字段）
 * @param currentQueueIndex 当前生产项索引
 * @param foodReceivedThisRound 本回合获得的粮食（供给结算用，运行时不持久化）
 */
data class ResourcePoint(
    var type: ResourcePointType,
    var faction: Int = 0,
    var isSuppressed: Boolean = false,
    var manpowerStock: Int = 0,
    var cattleStock: Int = 0,
    var horseStock: Int = 0,
    val equipStock: MutableMap<EquipmentType, Int> = mutableMapOf(),
    val productionQueue: MutableList<ProductionItem> = mutableListOf(),
    var currentQueueIndex: Int = 0,
    @Transient
    var foodReceivedThisRound: Int = 0,
    /** 本回合是否有装备点（运行时临时标记，不持久化） */
    @Transient
    var hasEquipPointThisRound: Boolean = false
) {
    /**
     * 获取资源点描述
     */
    fun getDescription(): String {
        val factionDesc = if (faction == 0) "未占领" else "阵营$faction"
        val suppressDesc = if (isSuppressed) " [被压制]" else ""
        return "${type.chineseName}($factionDesc)$suppressDesc"
    }

    /**
     * 获取资源储存的简短描述
     */
    fun getStockDescription(): String {
        val parts = mutableListOf<String>()
        if (manpowerStock > 0) parts.add("人力:$manpowerStock")
        if (cattleStock > 0) parts.add("牛:$cattleStock")
        if (horseStock > 0) parts.add("马:$horseStock")
        val equipParts = equipStock.filter { it.value > 0 }.map { "${it.key.chineseName}×${it.value}" }
        if (equipParts.isNotEmpty()) parts.add("装备(${equipParts.joinToString(",")})")
        return if (parts.isEmpty()) "无储存" else parts.joinToString(" ")
    }

    /**
     * 获取生产序列的简短描述
     */
    fun getProductionDescription(): String {
        if (productionQueue.isEmpty()) return "无生产"
        val sb = StringBuilder()
        for ((index, item) in productionQueue.withIndex()) {
            val prefix = if (index == currentQueueIndex) "▶" else " "
            sb.append("$prefix${item.equipType.chineseName}[${item.progressRounds}/${item.totalRounds}] ")
        }
        return sb.toString().trim()
    }
}
