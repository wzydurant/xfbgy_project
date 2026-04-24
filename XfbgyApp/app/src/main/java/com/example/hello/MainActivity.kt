package com.example.hello

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xfbgy.hexmap.ui.HexMapActivity

/**
 * 游戏启动页面
 * 展示游戏标题、副标题和开始按钮
 */
class MainActivity : AppCompatActivity() {

    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var startButton: Button
    private lateinit var widthInput: EditText
    private lateinit var heightInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.hello.R.layout.activity_splash)

        // 隐藏系统UI，全屏显示
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        initViews()
        setupAnimations()
        setupClickListeners()
    }

    private fun initViews() {
        titleText = findViewById(R.id.titleText)
        subtitleText = findViewById(R.id.subtitleText)
        startButton = findViewById(R.id.startButton)
        widthInput = findViewById(R.id.widthInput)
        heightInput = findViewById(R.id.heightInput)
    }

    /**
     * 设置按钮动画效果
     */
    private fun setupAnimations() {
        // 标题淡入动画
        titleText.alpha = 0f
        titleText.animate()
            .alpha(1f)
            .setDuration(1500)
            .setStartDelay(300)
            .start()

        // 副标题淡入动画
        subtitleText.alpha = 0f
        subtitleText.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(800)
            .start()

        // 设置容器淡入动画
        val settingsContainer = findViewById<View>(R.id.settingsContainer)
        settingsContainer.alpha = 0f
        settingsContainer.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(1000)
            .start()

        // 按钮缩放入场动画
        startButton.scaleX = 0f
        startButton.scaleY = 0f
        startButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(1200)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()
    }

    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        startButton.setOnClickListener {
            // 获取用户输入的地图大小
            val widthStr = widthInput.text.toString()
            val heightStr = heightInput.text.toString()

            // 验证输入
            if (widthStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, "请输入地图大小", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val width = widthStr.toIntOrNull()
            val height = heightStr.toIntOrNull()

            if (width == null || height == null) {
                Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (width !in 20..40 || height !in 20..40) {
                Toast.makeText(this, "地图大小必须在20-40之间", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 跳转到游戏地图页面，传递地图大小
            val intent = Intent(this, HexMapActivity::class.java).apply {
                putExtra(HexMapActivity.EXTRA_MAP_WIDTH, width)
                putExtra(HexMapActivity.EXTRA_MAP_HEIGHT, height)
            }
            startActivity(intent)
            // 添加切换动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
