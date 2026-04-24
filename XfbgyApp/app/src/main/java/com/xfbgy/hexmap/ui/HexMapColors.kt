package com.xfbgy.hexmap.ui

import android.graphics.Color

/**
 * 兵棋手游颜色常量定义
 * 对应开发方案文档第六节颜色规范
 */
object HexMapColors {
    // 地形颜色
    const val PLAIN = 0xFFA8D5A2.toInt()       // 平原 - 浅绿色
    const val FOREST = 0xFF2D6A4F.toInt()      // 树林 - 深绿色
    const val HILL = 0xFF8B5E3C.toInt()        // 山地 - 褐色
    const val MOUNTAIN = 0xFF222222.toInt()    // 高山 - 黑色
    const val URBAN = 0xFF9E9E9E.toInt()       // 建筑群 - 灰色

    // 边界与线条
    const val BORDER = 0xFF333333.toInt()      // 边界线 - 黑色细线
    const val RIVER = 0xFF1E90FF.toInt()        // 河流 - 蓝色粗线

    // 防御工事线
    const val FENCE = 0xFF8B5E3C.toInt()        // 栅栏 - 褐色小叉线
    const val EARTHWALL = 0xFF757575.toInt()    // 土墙 - 灰色小叉线
    const val STONEWALL = 0xFFFFFFFF.toInt()    // 石墙 - 白色小叉线

    // 调试模式
    const val COORD_TEXT = Color.BLACK         // 坐标标签文字
    const val COORD_BG = 0x64FFFFFF            // 坐标背景半透明白 (argb(100,255,255,255))

    // 信息面板
    const val PANEL_BG = 0xFF2D2D2D.toInt()     // 面板背景深灰色
    const val PANEL_TEXT = Color.WHITE          // 面板文字白色
    const val PANEL_ACCENT = 0xFF1E90FF.toInt() // 面板强调色蓝色
}