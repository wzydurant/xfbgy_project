package com.xfbgy.hexmap.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.xfbgy.hexmap.data.EquipmentType
import com.xfbgy.hexmap.data.ResourceKind

/**
 * 资源条View - 展示玩家当前资源数量
 *
 * 显示：粮食(全局池产出/消耗)、人力、牛、马、装备点 + 装备详情
 * 独立View类，不直接写在Activity中
 */
class ResourceBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val resourceTextViews = mutableMapOf<ResourceKind, TextView>()
    private val equipDetailText: TextView
    private val foodDetailText: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))

        // 粮食（特殊展示：产出/消耗）
        val foodLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        val foodIcon = TextView(context).apply {
            text = ResourceKind.FOOD.icon
            textSize = 12f
        }
        foodLayout.addView(foodIcon)

        foodDetailText = TextView(context).apply {
            text = "0"
            textSize = 12f
            setTextColor(0xFF808080.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dpToPx(2), 0, dpToPx(4), 0)
        }
        foodLayout.addView(foodDetailText)
        addView(foodLayout)

        addView(createSeparator())

        // 其他资源种类
        val displayKinds = listOf(
            ResourceKind.MANPOWER,
            ResourceKind.CATTLE,
            ResourceKind.HORSE,
            ResourceKind.EQUIPMENT
        )

        for ((index, kind) in displayKinds.withIndex()) {
            if (index > 0) {
                addView(createSeparator())
            }

            val itemLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }

            // 图标
            val iconText = TextView(context).apply {
                text = kind.icon
                textSize = 12f
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }
            itemLayout.addView(iconText)

            // 数值
            val valueText = TextView(context).apply {
                text = "0"
                textSize = 12f
                setTextColor(0xFFE0E0E0.toInt())
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(dpToPx(2), 0, dpToPx(4), 0)
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            }
            itemLayout.addView(valueText)
            resourceTextViews[kind] = valueText

            addView(itemLayout)
        }

        // 分隔符
        addView(createSeparator())

        // 装备详情
        equipDetailText = TextView(context).apply {
            text = ""
            textSize = 11f
            setTextColor(0xFFB0B0B0.toInt())
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            maxLines = 1
        }
        addView(equipDetailText)
    }

    /**
     * 更新资源显示
     *
     * @param resourceTotals 资源种类 -> 总数
     * @param equipTotals 装备类型 -> 总数
     * @param foodSummary 上回合粮食统计 (产出, 消耗, 结余)，null表示无数据
     */
    fun updateResources(
        resourceTotals: Map<ResourceKind, Int>,
        equipTotals: Map<EquipmentType, Int>,
        foodSummary: Triple<Int, Int, Int>? = null
    ) {
        // 粮食显示：产出/消耗
        if (foodSummary != null && (foodSummary.first > 0 || foodSummary.second > 0)) {
            val (produced, consumed, remaining) = foodSummary
            foodDetailText.text = "+$produced/-$consumed"
            foodDetailText.setTextColor(
                if (remaining > 0) 0xFF4CAF50.toInt()
                else if (produced > 0) 0xFFFF9800.toInt()
                else 0xFF808080.toInt()
            )
        } else {
            foodDetailText.text = "0"
            foodDetailText.setTextColor(0xFF808080.toInt())
        }

        for ((kind, textView) in resourceTextViews) {
            val value = resourceTotals[kind] ?: 0
            textView.text = value.toString()
            // 数值大于0时高亮
            textView.setTextColor(if (value > 0) 0xFFFFFFFF.toInt() else 0xFF808080.toInt())
        }

        // 装备详情
        val equipParts = equipTotals.filter { it.value > 0 }.map { "${it.key.chineseName}×${it.value}" }
        equipDetailText.text = if (equipParts.isEmpty()) "无装备" else equipParts.joinToString(" ")
    }

    private fun createSeparator(): TextView {
        return TextView(context).apply {
            text = "│"
            textSize = 12f
            setTextColor(0xFF606060.toInt())
            setPadding(dpToPx(3), 0, dpToPx(3), 0)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
