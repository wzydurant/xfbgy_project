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
import androidx.compose.ui.input.pointer.pointerInput
import com.hexwarfare.app.domain.model.Faction
import com.hexwarfare.app.domain.model.GameMap
import com.hexwarfare.app.domain.model.HexCoord
import com.hexwarfare.app.domain.model.TerrainType
import com.hexwarfare.app.domain.model.Unit as GameUnit
import kotlin.math.sqrt

/**
 * Pointy-top 六角格布局的地图视图
 */
@Composable
fun HexMapView(
    map: GameMap,
    units: List<GameUnit>,
    selectedCoord: HexCoord?,
    onTileClick: (HexCoord) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

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
    val playerColor = Color.Blue
    val enemyColor = Color.Red
    val neutralColor = Color.Gray

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offset += pan
                }
            }
            .pointerInput(map) {
                detectTapGestures { tapOffset ->
                    // 转换点击坐标到地图坐标
                    val adjustedX = (tapOffset.x - offset.x) / scale
                    val adjustedY = (tapOffset.y - offset.y) / scale

                    // 找到点击的六角格
                    val clickedCoord = screenToHex(adjustedX, adjustedY, hexSize)
                    if (map.tiles.containsKey(clickedCoord)) {
                        onTileClick(clickedCoord)
                    }
                }
            }
    ) {
        // 应用缩放和平移
        val currentScale = scale
        val currentOffset = offset

        // 绘制所有瓦片
        map.tiles.values.forEach { tile ->
            val (pixelX, pixelY) = hexToScreen(tile.coord, hexSize)
            val isSelected = tile.coord == selectedCoord

            drawHexTile(
                centerX = pixelX * currentScale + currentOffset.x,
                centerY = pixelY * currentScale + currentOffset.y,
                size = hexSize * currentScale,
                fillColor = terrainColors[tile.terrain] ?: defaultTerrainColor,
                isSelected = isSelected,
                selectedColor = selectedColor,
                strokeColor = strokeColor
            )
        }

        // 绘制单位
        units.forEach { unit ->
            val (pixelX, pixelY) = hexToScreen(unit.coord, hexSize)
            val unitColor = when (unit.faction) {
                Faction.PLAYER -> playerColor
                Faction.ENEMY -> enemyColor
                Faction.NEUTRAL -> neutralColor
            }
            drawCircle(
                color = unitColor,
                radius = 12f * currentScale,
                center = Offset(pixelX * currentScale + currentOffset.x, pixelY * currentScale + currentOffset.y)
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
 */
private fun screenToHex(screenX: Float, screenY: Float, size: Float): HexCoord {
    val q = (screenX * sqrt(3f) / 3f - screenY / 3f) / size
    val r = (screenY * 2f / 3f) / size
    return HexCoord.fromOffset(q.toInt(), r.toInt())
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

    // 填充
    drawPath(path, fillColor)

    // 边框
    val actualStrokeColor = if (isSelected) selectedColor else strokeColor
    val strokeWidth = if (isSelected) 4f else 1f
    drawPath(path, actualStrokeColor, style = Stroke(width = strokeWidth))
}