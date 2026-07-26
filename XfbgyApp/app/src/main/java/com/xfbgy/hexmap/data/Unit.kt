package com.xfbgy.hexmap.data

/**
 * 预留单位类（后续单位模块使用）
 * 当前仅作为占位符
 */
data class Unit(
    val id: Int,
    val name: String,
    val faction: Int,  // 阵营编号 1-3
    val attack: Int,
    val defense: Int,
    val movement: Int
)