package com.example.vcstudyassistant

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vcstudyassistant.floatingwindow.FloatingWindowService
import com.example.vcstudyassistant.payment.PaymentActivity
import com.example.vcstudyassistant.permission.PermissionManager
import com.example.vcstudyassistant.util.NetworkUtil
import com.example.vcstudyassistant.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var tvNoticeContent: TextView
    private lateinit var etTimerSetting: EditText
    private lateinit var etIntervalSetting: EditText
    
    // 权限请求码使用PermissionManager中定义的常量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化UI组件
        initUI()
        
        // 加载设置
        loadSettings()
        
        // 获取公告
        fetchNotice()
        
        // 检查付费状态
        checkPaymentStatus()
        
        // 设置按钮点击事件
        setButtonListeners()
    }

    private fun initUI() {
        tvNoticeContent = findViewById(R.id.tv_notice_content)
        etTimerSetting = findViewById(R.id.et_timer_setting)
        etIntervalSetting = findViewById(R.id.et_interval_setting)
    }

    private fun loadSettings() {
        val timer = PreferenceUtil.getTimerSetting(this)
        if (timer > 0) {
            etTimerSetting.setText(timer.toString())
        }
        
        val interval = PreferenceUtil.getIntervalSetting(this)
        if (interval > 0) {
            etIntervalSetting.setText(interval.toString())
        }
    }

    private fun fetchNotice() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notice = NetworkUtil.fetchNotice(this@MainActivity)
                withContext(Dispatchers.Main) {
                    tvNoticeContent.text = notice
                    
                    // 设置滚动效果
                    setNoticeScrollEffect()
                    
                    // 保存最后检查时间
                    PreferenceUtil.setLastNoticeCheck(this@MainActivity, System.currentTimeMillis())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 设置公告滚动效果
     * 仅当公告文本长度超过显示容器容量时，触发横向动态滚动效果
     */
    private fun setNoticeScrollEffect() {
        tvNoticeContent.post({
            // 获取TextView的实际宽度和内容宽度
            val viewWidth = tvNoticeContent.width
            val contentWidth = tvNoticeContent.paint.measureText(tvNoticeContent.text.toString())
            
            if (contentWidth > viewWidth) {
                // 文本长度超过容器，启用滚动效果
                tvNoticeContent.isSelected = true
            } else {
                // 文本长度未超过容器，禁用滚动效果
                tvNoticeContent.isSelected = false
            }
        })
    }

    private fun checkPaymentStatus() {
        if (!PreferenceUtil.isPaymentVerified(this)) {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setButtonListeners() {
        // 定时设置确认按钮
        findViewById<View>(R.id.btn_timer_confirm).setOnClickListener {
            val timerText = etTimerSetting.text.toString()
            if (timerText.isNotEmpty()) {
                val timer = timerText.toLong()
                PreferenceUtil.setTimerSetting(this, timer)
                Toast.makeText(this, getString(R.string.msg_timer_set), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.error_invalid_input), Toast.LENGTH_SHORT).show()
            }
        }
        
        // 间隔设置确认按钮
        findViewById<View>(R.id.btn_interval_confirm).setOnClickListener {
            val intervalText = etIntervalSetting.text.toString()
            if (intervalText.isNotEmpty()) {
                val interval = intervalText.toLong()
                PreferenceUtil.setIntervalSetting(this, interval)
                Toast.makeText(this, getString(R.string.msg_interval_set), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.error_invalid_input), Toast.LENGTH_SHORT).show()
            }
        }
        
        // 开始运行按钮
        findViewById<View>(R.id.btn_start_running).setOnClickListener {
            if (checkAllPermissions()) {
                startFloatingWindowService()
            } else {
                requestAllPermissions()
            }
        }
    }

    private fun checkAllPermissions(): Boolean {
        return PermissionManager.hasAllPermissions(this)
    }

    private fun requestAllPermissions() {
        PermissionManager.requestSelfPermissions(this)
    }

    private fun requestAccessibilityPermission() {
        PermissionManager.requestAccessibilityPermission(this)
    }

    private fun requestOverdrawPermission() {
        PermissionManager.requestOverdrawPermission(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.REQUEST_PERMISSIONS) {
            if (PermissionManager.hasSelfPermissions(this)) {
                if (!PermissionManager.hasAccessibilityPermission(this)) {
                    requestAccessibilityPermission()
                } else if (!PermissionManager.hasOverdrawPermission(this)) {
                    requestOverdrawPermission()
                } else {
                    startFloatingWindowService()
                }
            } else {
                Toast.makeText(this, getString(R.string.msg_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionManager.REQUEST_ACCESSIBILITY -> {
                if (PermissionManager.hasAccessibilityPermission(this)) {
                    if (!PermissionManager.hasOverdrawPermission(this)) {
                        requestOverdrawPermission()
                    } else {
                        startFloatingWindowService()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.msg_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
            PermissionManager.REQUEST_OVERDRAW -> {
                if (PermissionManager.hasOverdrawPermission(this)) {
                    startFloatingWindowService()
                } else {
                    Toast.makeText(this, getString(R.string.msg_permission_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startFloatingWindowService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        startService(intent)
    }
}
