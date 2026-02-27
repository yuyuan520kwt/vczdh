package com.example.vcstudyassistant.payment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vcstudyassistant.MainActivity
import com.example.vcstudyassistant.R
import com.example.vcstudyassistant.util.NetworkUtil
import com.example.vcstudyassistant.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentActivity : AppCompatActivity() {
    private lateinit var tvRandomString: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnVerify: Button
    
    private var randomString: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // 初始化UI组件
        initUI()
        
        // 生成随机字符串
        generateRandomString()
        
        // 设置按钮点击事件
        setButtonListeners()
    }

    private fun initUI() {
        tvRandomString = findViewById(R.id.tv_random_string)
        btnCopy = findViewById(R.id.btn_copy)
        btnVerify = findViewById(R.id.btn_verify)
    }

    private fun generateRandomString() {
        randomString = PreferenceUtil.getRandomString(this)
        tvRandomString.text = randomString
    }

    private fun setButtonListeners() {
        // 复制按钮点击事件
        btnCopy.setOnClickListener {
            copyRandomString()
        }

        // 验证按钮点击事件
        btnVerify.setOnClickListener {
            verifyPayment()
        }
    }

    private fun copyRandomString() {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("random_string", randomString)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, getString(R.string.msg_copy_success), Toast.LENGTH_SHORT).show()
    }

    private fun verifyPayment() {
        btnVerify.isEnabled = false
        btnVerify.text = "验证中..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 验证支付状态
                val isVerified = NetworkUtil.verifyPayment(this@PaymentActivity, randomString)
                
                withContext(Dispatchers.Main) {
                    btnVerify.isEnabled = true
                    btnVerify.text = getString(R.string.btn_verify)
                    
                    if (isVerified) {
                        // 验证成功
                        handleVerificationSuccess()
                    } else {
                        // 验证失败
                        Toast.makeText(this@PaymentActivity, getString(R.string.msg_verify_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    btnVerify.isEnabled = true
                    btnVerify.text = getString(R.string.btn_verify)
                    Toast.makeText(this@PaymentActivity, getString(R.string.msg_verify_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleVerificationSuccess() {
        // 获取当前时间
        val currentTime = NetworkUtil.getCurrentTime(this)
        
        // 设置过期时间（假设有效期为2小时）
        val expiryTime = currentTime + (2 * 60 * 60 * 1000)
        
        // 保存验证状态和过期时间
        PreferenceUtil.setPaymentVerified(this, true)
        PreferenceUtil.setExpiryTime(this, expiryTime)
        
        // 显示成功提示
        Toast.makeText(this, getString(R.string.msg_verify_success), Toast.LENGTH_SHORT).show()
        
        // 启动主界面并关闭当前界面
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // 禁止返回按钮，用户必须完成验证
        showExitConfirmation()
    }

    private fun showExitConfirmation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.payment_title))
        builder.setMessage("未完成验证，退出后应用将无法使用。是否确定退出？")
        builder.setPositiveButton(getString(R.string.btn_ok)) {
            dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            finishAffinity() // 退出应用
        }
        builder.setNegativeButton(getString(R.string.btn_cancel)) {
            dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onResume() {
        super.onResume()
        
        // 检查是否已经验证
        if (PreferenceUtil.isPaymentVerified(this)) {
            // 检查是否已过期
            val expiryTime = PreferenceUtil.getExpiryTime(this)
            val currentTime = System.currentTimeMillis()
            
            if (currentTime > expiryTime) {
                // 已过期，重新生成随机字符串
                randomString = PreferenceUtil.updateRandomString(this)
                tvRandomString.text = randomString
                PreferenceUtil.setPaymentVerified(this, false)
            } else {
                // 未过期，进入主界面
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}