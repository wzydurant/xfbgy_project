package com.hexwarfare.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.hexwarfare.app.domain.model.Faction
import com.hexwarfare.app.domain.model.GameMap
import com.hexwarfare.app.domain.model.GameUnit
import com.hexwarfare.app.domain.model.HexCoord
import com.hexwarfare.app.domain.model.TerrainType
import kotlin.math.sqrt

/**
 * Pointy-top 六角格布局的地图视图
 */
@Composable
fun HexMapView(
    map: GameMap,
    units: List<GameUnit>,
    selectedCoord: HexCoord?,
    highlightedCoords: Set<HexCoord> = emptySet(),
    pathCoords: List<HexCoord> = emptyList(),
    moveCosts: Map<HexCoord, Int> = emptyMap(),
    onTileClick: (HexCoord) -> Unit,
    onTileHover: ((HexCoord) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isInitialized by remember { mutableStateOf(false) }

    // 六角格尺寸
    val hexSize = 60f

    // 地形颜色
    val terrainColors = mapOf(
        TerrainType.PLAINS to Color(0xFF90EE90),
        TerrainType.MOUNTAIN to Color(0xFF8B8B83),
        TerrainType.RIVER to Color(0xFF4169E1),
        TerrainType.BUILDING to Color(0xFFD2691E),
        TerrainType.IMPASSABLE to Color(0xFF2F2F2F)
    )

    // 颜色定义
    val defaultTerrainColor = Color.Gray
    val selectedColor = Color(0xFFFFD700)
    val strokeColor = Color.DarkGray
    val playerColor = Color(0xFF2196F3)
    val enemyColor = Color(0xFFF44336)
    val neutralColor = Color.Gray
    val reachableColor = Color(0x4400FF00) // 半透明绿色
    val pathColor = Color(0xFFFF6B00) // 橙色路径
    val unitBorderColor = Color.White

    // 计算地图边界
    val mapBounds = remember(map) {
        if (map.tiles.isEmpty()) {
            HexCoord(0, 0) to HexCoord(0, 0)
        } else {
            val minQ = map.tiles.keys.minOf { it.q }
            val maxQ = map.tiles.keys.maxOf { it.q }
            val minR = map.tiles.keys.minOf { it.r }
            val maxR = map.tiles.keys.maxOf { it.r }
            HexCoord(minQ, minR) to HexCoord(maxQ, maxR)
        }
    }

    // 首次初始化时计算居中偏移（在 Compose 阶段计算，不在 drawScope 中）
    LaunchedEffect(map, mapBounds) {
        if (!isInitialized && map.tiles.isNotEmpty()) {
            val (minCoord, maxCoord) = mapBounds
            // 计算地图中心点的屏幕坐标
            val centerQ = (minCoord.q + maxCoord.q) / 2f
            val centerR = (minCoord.r + maxCoord.r) / 2f
            val (mapCenterX, mapCenterY) = hexToScreen(HexCoord(centerQ.toInt(), centerR.toInt()), hexSize)

            // 使用固定尺寸估算画布中心（实际尺寸在 draw 时获取）
            // 这里使用一个估算值，draw 时会重新调整
            val canvasCenterX = 400f  // 估算值
            val canvasCenterY = 300f  // 估算值
            offset = Offset(canvasCenterX - mapCenterX, canvasCenterY - mapCenterY)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.3f, 3f)
                    offset += pan
                    isInitialized = true
                }
            }
            .pointerInput(map) {
                detectTapGestures { tapOffset ->
                    // 转换点击坐标到地图坐标（考虑缩放和偏移）
                    val mapX = (tapOffset.x - offset.x) / scale
                    val mapY = (tapOffset.y - offset.y) / scale

                    // 找到点击的六角格
                    val clickedCoord = screenToHex(mapX, mapY, hexSize)
                    if (map.tiles.containsKey(clickedCoord)) {
                        onTileClick(clickedCoord)
                    }
                }
            }
    ) {
        // 首次初始化时计算精确居中偏移
        if (!isInitialized && map.tiles.isNotEmpty()) {
            val (minCoord, maxCoord) = mapBounds
            val centerQ = (minCoord.q + maxCoord.q) / 2f
            val centerR = (minCoord.r + maxCoord.r) / 2f
            val (mapCenterX, mapCenterY) = hexToScreen(HexCoord(centerQ.toInt(), centerR.toInt()), hexSize)

            val canvasCenterX = size.width / 2f
            val canvasCenterY = size.height / 2f

            offset = Offset(canvasCenterX - mapCenterX, canvasCenterY - mapCenterY)
            isInitialized = true
        }

        val currentScale = scale
        val currentOffset = offset

        // 绘制所有瓦片
        map.tiles.values.forEach { tile ->
            val (pixelX, pixelY) = hexToScreen(tile.coord, hexSize)
            val isSelected = tile.coord == selectedCoord
            val isHighlighted = tile.coord in highlightedCoords
            val isOnPath = tile.coord in pathCoords

            // 绘制可达高亮
            if (isHighlighted) {
                drawHexTile(
                    centerX = pixelX * currentScale + currentOffset.x,
                    centerY = pixelY * currentScale + currentOffset.y,
                    size = hexSize * currentScale,
                    fillColor = reachableColor,
                    isSelected = false,
                    selectedColor = Color.Transparent,
                    strokeColor = Color(0xFF00CC00)
                )
            }

            // 绘制路径
            if (isOnPath) {
                drawHexTile(
                    centerX = pixelX * currentScale + currentOffset.x,
                    centerY = pixelY * currentScale + currentOffset.y,
                    size = hexSize * currentScale,
                    fillColor = pathColor.copy(alpha = 0.5f),
                    isSelected = false,
                    selectedColor = Color.Transparent,
                    strokeColor = pathColor
                )
            }

            // 绘制普通地块
            drawHexTile(
                centerX = pixelX * currentScale + currentOffset.x,
                centerY = pixelY * currentScale + currentOffset.y,
                size = hexSize * currentScale,
                fillColor = terrainColors[tile.terrain] ?: defaultTerrainColor,
                isSelected = isSelected,
                selectedColor = selectedColor,
                strokeColor = strokeColor
            )

            // 绘制坐标文字（每个格子都显示）
            drawContext.canvas.nativeCanvas.drawText(
                "${tile.coord.q},${tile.coord.r}",
                pixelX * currentScale + currentOffset.x,
                pixelY * currentScale + currentOffset.y - 8f * currentScale,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 9f * currentScale
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    setShadowLayer(1f, 0.5f, 0.5f, android.graphics.Color.WHITE)
                }
            )

            // 绘制移动力数字
            if (isHighlighted && moveCosts.containsKey(tile.coord)) {
                val cost = moveCosts[tile.coord]!!
                drawContext.canvas.nativeCanvas.drawText(
                    cost.toString(),
                    pixelX * currentScale + currentOffset.x,
                    pixelY * currentScale + currentOffset.y + 5f * currentScale,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 12f * currentScale
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        setShadowLayer(2f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                )
            }
        }

        // 绘制路径连接线
        if (pathCoords.size > 1) {
            for (i in 0 until pathCoords.size - 1) {
                val from = pathCoords[i]
                val to = pathCoords[i + 1]
                val (x1, y1) = hexToScreen(from, hexSize)
                val (x2, y2) = hexToScreen(to, hexSize)

                drawLine(
                    color = pathColor,
                    start = Offset(x1 * currentScale + currentOffset.x, y1 * currentScale + currentOffset.y),
                    end = Offset(x2 * currentScale + currentOffset.x, y2 * currentScale + currentOffset.y),
                    strokeWidth = 4f * currentScale
                )
            }
        }

        // 绘制单位
        units.forEach { unit ->
            val (pixelX, pixelY) = hexToScreen(unit.coord, hexSize)
            val unitColor = when (unit.faction) {
                Faction.PLAYER -> playerColor
                Faction.ENEMY -> enemyColor
                Faction.NEUTRAL -> neutralColor
            }

            // 单位圆形
            drawCircle(
                color = unitColor,
                radius = 15f * currentScale,
                center = Offset(pixelX * currentScale + currentOffset.x, pixelY * currentScale + currentOffset.y)
            )

            // 单位白色边框
            drawCircle(
                color = unitBorderColor,
                radius = 15f * currentScale,
                center = Offset(pixelX * currentScale + currentOffset.x, pixelY * currentScale + currentOffset.y),
                style = Stroke(width = 2f * currentScale)
            )

            // 如果是将军/参谋长单位，绘制特殊标记
            if (unit.isGeneralUnit) {
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = 18f * currentScale,
                    center = Offset(pixelX * currentScale + currentOffset.x, pixelY * currentScale + currentOffset.y),
                    style = Stroke(width = 3f * currentScale)
                )
            }

            // 单位名称首字母
            drawContext.canvas.nativeCanvas.drawText(
                unit.name.first().toString(),
                pixelX * currentScale + currentOffset.x,
                pixelY * currentScale + currentOffset.y + 5f * currentScale,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 14f * currentScale
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )
        }
    }
}

/**
 * 将六角格坐标转换为屏幕坐标 (Pointy-top)
 */
private fun hexToScreen(coord: HexCoord, size: Float): Pair<Float, Float> {
    val x = size * sqrt(3f) * (coord.q + coord.r / 2f)
    val y = size * 3f / 2f * coord.r
    return x to y
}

/**
 * 将屏幕坐标转换为六角格坐标 (Pointy-top)
 * 使用立方坐标舍入算法确保准确找到最近的六角格
 */
private fun screenToHex(screenX: Float, screenY: Float, size: Float): HexCoord {
    // 将屏幕像素坐标转换为轴向坐标（浮点数）
    val q = (screenX * sqrt(3f) / 3f - screenY / 3f) / size
    val r = (screenY * 2f / 3f) / size
    val s = -q - r

    // 立方坐标舍入算法 - 将浮点坐标舍入到最近的整数六角格
    var rq = kotlin.math.round(q).toInt()
    var rr = kotlin.math.round(r).toInt()
    var rs = kotlin.math.round(s).toInt()

    val dq = kotlin.math.abs(rq - q)
    val dr = kotlin.math.abs(rr - r)
    val ds = kotlin.math.abs(rs - s)

    // 调整使 q + r + s = 0
    if (dq > dr && dq > ds) {
        rq = -rr - rs
    } else if (dr > ds) {
        rr = -rq - rs
    } else {
        rs = -rq - rr
    }

    return HexCoord(rq, rr, rs)
}

/**
 * 绘制六角格
 */
private fun DrawScope.drawHexTile(
    centerX: Float,
    centerY: Float,
    size: Float,
    fillColor: Color,
    isSelected: Boolean,
    selectedColor: Color,
    strokeColor: Color
) {
    val path = Path()
    for (i in 0..5) {
        val angle = Math.PI / 180 * (60 * i - 30)
        val x = centerX + size * kotlin.math.cos(angle).toFloat()
        val y = centerY + size * kotlin.math.sin(angle).toFloat()
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()

    // 选中时绘制外发光效果（两层）
    if (isSelected) {
        // 外层光晕
        val glowPath = Path()
        val glowSize = size * 1.15f
        for (i in 0..5) {
            val angle = Math.PI / 180 * (60 * i - 30)
            val x = centerX + glowSize * kotlin.math.cos(angle).toFloat()
            val y = centerY + glowSize * kotlin.math.sin(angle).toFloat()
            if (i == 0) {
                glowPath.moveTo(x, y)
            } else {
                glowPath.lineTo(x, y)
            }
        }
        glowPath.close()
        drawPath(glowPath, selectedColor.copy(alpha = 0.3f))

        // 内层高亮边框
        val innerGlowPath = Path()
        val innerGlowSize = size * 1.08f
        for (i in 0..5) {
            val angle = Math.PI / 180 * (60 * i - 30)
            val x = centerX + innerGlowSize * kotlin.math.cos(angle).toFloat()
            val y = centerY + innerGlowSize * kotlin.math.sin(angle).toFloat()
            if (i == 0) {
                innerGlowPath.moveTo(x, y)
            } else {
                innerGlowPath.lineTo(x, y)
            }
        }
        innerGlowPath.close()
        drawPath(innerGlowPath, selectedColor.copy(alpha = 0.6f))
    }

    // 填充
    drawPath(path, fillColor)

    // 边框
    val actualStrokeColor = if (isSelected) selectedColor else strokeColor
    val strokeWidth = if (isSelected) 5f else 1f
    drawPath(path, actualStrokeColor, style = Stroke(width = strokeWidth))

    // 选中时在中心绘制标记点
    if (isSelected) {
        drawCircle(
            color = selectedColor,
            radius = size * 0.25f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = size * 0.15f,
            center = Offset(centerX, centerY)
        )
    }
}