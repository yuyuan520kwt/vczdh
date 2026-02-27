package com.example.vcstudyassistant.floatingwindow

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.example.vcstudyassistant.R
import com.example.vcstudyassistant.answer.AnswerManager
import com.example.vcstudyassistant.util.PreferenceUtil

class FloatingWindowService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var dragHandle: View
    
    private var params: WindowManager.LayoutParams? = null
    private var isFloatingWindowVisible = false
    private var isDragging = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0.0f
    private var initialTouchY = 0.0f

    private lateinit var answerManager: AnswerManager

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        answerManager = AnswerManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isFloatingWindowVisible) {
            showFloatingWindow()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
        } else {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
        }

        params?.gravity = Gravity.TOP or Gravity.START
        params?.x = 100
        params?.y = 200

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window, null)

        btnStart = floatingView.findViewById(R.id.btn_start)
        btnStop = floatingView.findViewById(R.id.btn_stop)
        dragHandle = floatingView.findViewById(R.id.drag_handle)

        // 设置按钮点击事件
        btnStart.setOnClickListener {
            startAnswerProcess()
        }

        btnStop.setOnClickListener {
            stopAnswerProcess()
        }

        // 设置拖动手柄触摸事件
        dragHandle.setOnTouchListener {
                _: View, event: MotionEvent -> handleTouchEvent(event)
        }

        // 添加悬浮窗到窗口管理器
        windowManager.addView(floatingView, params)
        isFloatingWindowVisible = true
    }

    private fun handleTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params?.x ?: 0
                initialY = params?.y ?: 0
                initialTouchX = event.rawX.toDouble()
                initialTouchY = event.rawY.toDouble()
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val newX = (initialX + (event.rawX.toDouble() - initialTouchX)).toInt()
                    val newY = (initialY + (event.rawY.toDouble() - initialTouchY)).toInt()
                    params?.x = newX
                    params?.y = newY
                    windowManager.updateViewLayout(floatingView, params)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return true
    }

    private fun startAnswerProcess() {
        // 获取用户设置的定时时间
        val timer = PreferenceUtil.getTimerSetting(this)
        if (timer > 0) {
            answerManager.setTimer(timer)
        }
        
        // 获取用户设置的间隔时间
        val interval = PreferenceUtil.getIntervalSetting(this)
        answerManager.setInterval(interval)
        
        // 启动答题流程
        answerManager.startAnswerProcess()
        Toast.makeText(this, "答题流程已启动", Toast.LENGTH_SHORT).show()
    }

    private fun stopAnswerProcess() {
        // 停止答题流程
        answerManager.stopAnswerProcess()
        Toast.makeText(this, "答题流程已停止", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFloatingWindowVisible) {
            windowManager.removeView(floatingView)
            isFloatingWindowVisible = false
        }
        answerManager.stopAnswerProcess()
    }
}
