package com.xfbgy.hexmap.data

/**
 * 资源点数据类
 *
 * 存储资源点的基础信息，关联到 HexCell
 *
 * @param type 资源点类型
 * @param faction 归属阵营（0=未占领, 1/2/3=阵营编号）
 * @param isSuppressed 是否被压制
 */
data class ResourcePoint(
    var type: ResourcePointType,
    var faction: Int = 0,
    var isSuppressed: Boolean = false
) {
    /**
     * 获取资源点描述
     */
    fun getDescription(): String {
        val factionDesc = if (faction == 0) "未占领" else "阵营$faction"
        val suppressDesc = if (isSuppressed) " [被压制]" else ""
        return "${type.chineseName}($factionDesc)$suppressDesc"
    }
}
