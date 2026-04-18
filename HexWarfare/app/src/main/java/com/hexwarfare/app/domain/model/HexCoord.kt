package com.hexwarfare.app.domain.model

/**
 * 六角格坐标类，使用 Cube 坐标系统
 * @param q 轴坐标
 * @param r 径向坐标
 * @param s 第三轴坐标（q + r + s = 0）
 */
data class HexCoord(val q: Int, val r: Int, val s: Int = -q - r) {
    init {
        require(q + r + s == 0) { "Cube coordinates must sum to zero" }
    }

    /**
     * 获取邻居坐标
     */
    fun getNeighbors(): List<HexCoord> = listOf(
        HexCoord(q + 1, r - 1),
        HexCoord(q + 1, r),
        HexCoord(q, r + 1),
        HexCoord(q - 1, r + 1),
        HexCoord(q - 1, r),
        HexCoord(q, r - 1)
    )

    /**
     * 计算到另一个坐标的距离
     */
    fun distanceTo(other: HexCoord): Int {
        return maxOf(
            kotlin.math.abs(q - other.q),
            kotlin.math.abs(r - other.r),
            kotlin.math.abs(s - other.s)
        )
    }

    /**
     * 转换为 Offset 坐标（奇数行偏移）
     */
    fun toOffset(): Pair<Int, Int> {
        val col = q + (r - (r and 1)) / 2
        return col to r
    }

    companion object {
        /**
         * 从 Offset 坐标转换
         */
        fun fromOffset(col: Int, row: Int): HexCoord {
            val q = col - (row - (row and 1)) / 2
            val r = row
            return HexCoord(q, r)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HexCoord
        return q == other.q && r == other.r
    }

    override fun hashCode(): Int = 31 * q + r
}
