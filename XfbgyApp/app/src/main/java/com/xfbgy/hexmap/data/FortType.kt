package com.xfbgy.hexmap.data

/**
 * 防御工事枚举
 *
 * @param chineseName 中文名称
 * @param visualStyle 视觉样式描述
 * @param movementPenalty 移动破坏值
 * @param defenseBonus 防御优势等级
 */
enum class FortType(
    val chineseName: String,
    val visualStyle: String,
    val movementPenalty: Int,
    val defenseBonus: Int
) {
    NONE("无", "—", 0, 0),
    FENCE("栅栏", "褐色小叉连线", 0, 1),
    EARTHWALL("土墙", "灰色小叉连线", 1, 2),
    STONEWALL("石墙", "白色小叉连线", 1, 1);

    companion object {
        /**
         * 根据索引获取工事类型
         */
        fun fromOrdinal(ordinal: Int): FortType {
            return entries.getOrElse(ordinal) { NONE }
        }

        /**
         * 获取随机非空工事类型（用于建筑群生成）
         */
        fun randomFortification(): FortType {
            val nonNone = entries.filter { it != NONE }
            return nonNone.random()
        }
    }
}