package com.xfbgy.hexmap.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * HexMapActivity - 地图生成页面
 *
 * 输入地图大小和河流数量，点击"生成地图"后跳转到游戏页面(GameActivity)。
 */
class HexMapActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MAP_WIDTH = "extra_map_width"
        const val EXTRA_MAP_HEIGHT = "extra_map_height"
    }

    private lateinit var mapSizeInput: EditText
    private lateinit var riverCountInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = createLayout()
        setContentView(rootView)
    }

    private fun createLayout(): ScrollView {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(0xFF1A1A2E.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
        }

        // 标题
        val titleText = TextView(this).apply {
            text = "地图生成"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dpToPx(16))
        }
        rootLayout.addView(titleText)

        // 副标题
        val subtitleText = TextView(this).apply {
            text = "输入大小生成n×n六角格地图，随机地形/工事/河流"
            textSize = 13f
            setTextColor(0xFF80FFFFFF.toInt())
            setPadding(0, 0, 0, dpToPx(8))
        }
        rootLayout.addView(subtitleText)

        // 输入行：地图大小
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val inputLabel = TextView(this).apply {
            text = "地图大小："
            textSize = 15f
            setTextColor(Color.WHITE)
        }
        inputLayout.addView(inputLabel)

        val defaultSize = intent.getIntExtra(EXTRA_MAP_WIDTH, 5).coerceIn(5, 50)
        mapSizeInput = EditText(this).apply {
            hint = "5~50"
            textSize = 15f
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF80FFFFFF.toInt())
            setBackgroundColor(0xFF37474F.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(dpToPx(80), dpToPx(44))
        }
        mapSizeInput.setText(defaultSize.toString())
        inputLayout.addView(mapSizeInput)

        val sizeSuffix = TextView(this).apply {
            text = " × n"
            textSize = 15f
            setTextColor(0xFFB0FFFFFF.toInt())
        }
        inputLayout.addView(sizeSuffix)

        rootLayout.addView(inputLayout)

        // 河流数量输入行
        val riverInputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val riverLabel = TextView(this).apply {
            text = "河流数量："
            textSize = 15f
            setTextColor(Color.WHITE)
        }
        riverInputLayout.addView(riverLabel)

        riverCountInput = EditText(this).apply {
            hint = "1~10"
            textSize = 15f
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.WHITE)
            setHintTextColor(0xFF80FFFFFF.toInt())
            setBackgroundColor(0xFF37474F.toInt())
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            layoutParams = LinearLayout.LayoutParams(dpToPx(80), dpToPx(44))
        }
        riverCountInput.setText("1")
        riverInputLayout.addView(riverCountInput)

        val riverHint = TextView(this).apply {
            text = " 条（横向/纵向各50%）"
            textSize = 13f
            setTextColor(0xFFB0FFFFFF.toInt())
        }
        riverInputLayout.addView(riverHint)

        val generateBtn = Button(this).apply {
            text = "生成地图"
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF1E90FF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(44)
            ).apply {
                marginStart = dpToPx(12)
            }
            setOnClickListener { generateMap() }
        }
        riverInputLayout.addView(generateBtn)

        rootLayout.addView(riverInputLayout)

        // 提示
        val hint = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFF80FFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(16), 0, 0)
            text = "点击\"生成地图\"后进入游戏页面"
        }
        rootLayout.addView(hint)

        scrollView.addView(rootLayout)
        return scrollView
    }

    private fun generateMap() {
        val sizeStr = mapSizeInput.text.toString()
        val size = sizeStr.toIntOrNull()

        if (size == null || size !in 5..50) {
            Toast.makeText(this, "请输入5~50之间的数字", Toast.LENGTH_SHORT).show()
            return
        }

        val riverCountStr = riverCountInput.text.toString()
        val riverCount = riverCountStr.toIntOrNull()
        if (riverCount == null || riverCount !in 1..10) {
            Toast.makeText(this, "河流数量请输入1~10之间的数字", Toast.LENGTH_SHORT).show()
            return
        }

        // 跳转到游戏页面
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_MAP_SIZE, size)
            putExtra(GameActivity.EXTRA_RIVER_COUNT, riverCount)
        }
        startActivity(intent)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
