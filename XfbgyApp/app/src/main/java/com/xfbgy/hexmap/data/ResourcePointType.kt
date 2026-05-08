package com.xfbgy.hexmap.data

/**
 * 资源点类型枚举
 *
 * 根据地形和工事要求自动识别：
 * - 村庄: 建筑群 + 全部6边均为栅栏
 * - 城镇: 建筑群 + 全部6边均为土墙
 * - 都市: 建筑群 + 全部6边均为石墙（相邻都市格之间的边除外）
 * - 马场: 平原 + 无工事 + 不与任何建筑群格相邻
 */
enum class ResourcePointType(
    val chineseName: String,
    val iconLabel: String,
    val colorHex: String
) {
    VILLAGE("村庄", "村", "#D4A574"),     // 暖褐色
    TOWN("城镇", "镇", "#B8860B"),       // 深金色
    CITY("都市", "都", "#C0C0C0"),        // 银色
    RANCH("马场", "马", "#8FBC8F");       // 暗海绿

    companion object {
        fun fromOrdinal(ordinal: Int): ResourcePointType {
            return entries.getOrElse(ordinal) { VILLAGE }
        }
    }
}
