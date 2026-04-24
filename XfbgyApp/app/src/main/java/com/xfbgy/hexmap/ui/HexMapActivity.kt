package com.xfbgy.hexmap.ui

import android.os.Bundle
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.xfbgy.hexmap.data.HexCell
import com.xfbgy.hexmap.generator.MapGenerator

/**
 * HexMapActivity - 地图显示与交互测试Activity
 *
 * 整合 HexMapView 和 InfoPanelView，展示完整地图功能：
 * 1. 生成随机地图
 * 2. 显示地图（支持缩放、拖拽）
 * 3. 点击格子显示信息面板
 * 4. 查看边缘属性
 */
class HexMapActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MAP_WIDTH = "extra_map_width"
        const val EXTRA_MAP_HEIGHT = "extra_map_height"
        private const val DEFAULT_MAP_SIZE = 30
    }

    private lateinit var hexMapView: HexMapView
    private lateinit var infoPanelView: InfoPanelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建主布局
        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 创建地图视图
        hexMapView = HexMapView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 创建信息面板
        infoPanelView = InfoPanelView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        rootLayout.addView(hexMapView)
        rootLayout.addView(infoPanelView)
        setContentView(rootLayout)

        // 获取地图大小参数
        val mapWidth = intent.getIntExtra(EXTRA_MAP_WIDTH, DEFAULT_MAP_SIZE)
            .coerceIn(20, 40)
        val mapHeight = intent.getIntExtra(EXTRA_MAP_HEIGHT, DEFAULT_MAP_SIZE)
            .coerceIn(20, 40)

        // 生成并显示地图
        val hexMap = MapGenerator.generateMap(width = mapWidth, height = mapHeight)
        hexMapView.setMap(hexMap)

        // 设置地图点击监听
        hexMapView.setOnCellClickListener { cell ->
            showCellInfo(cell)
        }

        // 设置信息面板方向按钮点击
        infoPanelView.setOnDirectionClickListener { direction ->
            showEdgeInfo(direction)
        }

        // 设置面板关闭监听
        infoPanelView.setOnPanelDismissListener {
            hexMapView.clearSelection()
        }

        // 设置触摸事件分发（点击空白区域关闭面板）
        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 如果面板可见且点击不在面板内，关闭面板
                if (infoPanelView.panelIsVisible && !infoPanelView.isPointInPanel(event.x, event.y)) {
                    // 检查是否点击了方向按钮
                    val dir = infoPanelView.getDirectionAt(event.x, event.y)
                    if (dir == -1) {
                        infoPanelView.dismiss()
                    }
                }
            }
            false
        }
    }

    /**
     * 显示格子信息
     */
    private fun showCellInfo(cell: HexCell) {
        infoPanelView.showCellInfo(cell)
    }

    /**
     * 显示边缘信息
     */
    private fun showEdgeInfo(direction: Int) {
        val cell = hexMapView.currentSelectedCell ?: return
        val edge = hexMapView.getMap()?.getEdge(cell.x, cell.y, direction) ?: return
        infoPanelView.showEdgeInfo(cell, direction, edge)
    }
}