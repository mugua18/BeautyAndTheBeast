package com.example.beautyandthebeast

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.beautyandthebeast.R
import kotlin.concurrent.thread
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var petals: List<ImageView>
    private lateinit var btnStart: Button
    private lateinit var btnGiveFlower: Button
    private lateinit var btnDinner: Button
    private lateinit var btnChat: Button
    private lateinit var btnDance: Button

    private var remainingPetals = 5
    private var isGameActive = false
    private var isDialogShowing = false
    private var hasMagicBroken = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化UI元件
        initializeViews()
        setupButtonListeners()
        resetGameState()
    }

    private fun initializeViews() {
        petals = listOf(
            findViewById(R.id.petal1),
            findViewById(R.id.petal2),
            findViewById(R.id.petal3),
            findViewById(R.id.petal4),
            findViewById(R.id.petal5)
        )

        btnStart = findViewById(R.id.btnStart)
        btnGiveFlower = findViewById(R.id.btnGiveFlower)
        btnDinner = findViewById(R.id.btnDinner)
        btnChat = findViewById(R.id.btnChat)
        btnDance = findViewById(R.id.btnDance)
    }

    private fun setupButtonListeners() {
        btnStart.setOnClickListener {
            if (!isGameActive) {
                startGame()
            }
        }

        val interactionButtons = listOf(btnGiveFlower, btnDinner, btnChat, btnDance)
        interactionButtons.forEach { button ->
            button.setOnClickListener {
                if (!isGameActive || isDialogShowing) return@setOnClickListener

                val agreed = Random.nextInt(4) == 0 // 1/4 機率
                if (agreed) {
                    handleMagicSuccess()
                } else {
                    Toast.makeText(this, "貝兒還是拒絕...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startGame() {
        resetGameState()
        isGameActive = true
        btnStart.isEnabled = false
        setInteractionButtonsEnabled(true)
        startPetalFallSequence()
    }

    private fun resetGameState() {
        remainingPetals = 5
        isGameActive = false
        hasMagicBroken = false
        isDialogShowing = false

        // 重置花瓣狀態
        petals.forEach { petal ->
            petal.clearAnimation()
            petal.alpha = 1f
            petal.translationY = 0f
        }

        // 重置按鈕狀態
        btnStart.isEnabled = true
        setInteractionButtonsEnabled(false)
    }

    private fun startPetalFallSequence() {
        thread {
            try {
                for (i in petals.indices) {
                    if (!isGameActive || isDialogShowing) break
                    Thread.sleep(Random.nextLong(1000, 3000))
                    handler.post {
                        if (!isFinishing && isGameActive && !isDialogShowing) {
                            animatePetalFall(petals[i])
                        }
                    }
                }
            } finally {
                // 移除這行，讓花瓣掉落完成後再設置為false
                // isGameActive = false
            }
        }
    }

    private fun animatePetalFall(petal: ImageView) {
        val animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f
        )
        animation.duration = 1000
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                handler.post {
                    remainingPetals--
                    if (remainingPetals == 0) {
                        isGameActive = false  // 在這裡設置遊戲結束
                        showResultDialog("魔法仍未解除")  // 直接在這裡顯示對話框
                    }
                }
            }
        })
        petal.startAnimation(animation)
    }

    private fun handleMagicSuccess() {
        if (!isGameActive || isDialogShowing) return
        
        Toast.makeText(this, "貝兒答應了！", Toast.LENGTH_SHORT).show()
        hasMagicBroken = true
        isGameActive = false
        showResultDialog("魔法成功解除")
    }


    private fun showResultDialog(message: String) {
        if (isFinishing || isDestroyed) return
        
        runOnUiThread {
            isDialogShowing = true
            AlertDialog.Builder(this)
                .setTitle("結局")
                .setMessage(message)
                .setPositiveButton("重新開始") { dialog, _ ->
                    dialog.dismiss()
                    isDialogShowing = false
                    resetGameState()
                }
                .setCancelable(false)
                .setOnDismissListener {
                    isDialogShowing = false
                }
                .show()
        }
    }

    private fun setInteractionButtonsEnabled(enabled: Boolean) {
        runOnUiThread {
            btnGiveFlower.isEnabled = enabled
            btnDinner.isEnabled = enabled
            btnChat.isEnabled = enabled
            btnDance.isEnabled = enabled
            
            val color = if (enabled) "#FFD700" else "#CCCCCC"
            val tint = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(color))
            btnGiveFlower.backgroundTintList = tint
            btnDinner.backgroundTintList = tint
            btnChat.backgroundTintList = tint
            btnDance.backgroundTintList = tint
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isGameActive = false
        handler.removeCallbacksAndMessages(null)
    }
}
