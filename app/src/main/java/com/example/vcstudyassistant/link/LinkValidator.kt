package com.example.vcstudyassistant.link

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.example.vcstudyassistant.R
import com.example.vcstudyassistant.util.NetworkUtil

class LinkValidator(private val context: Context) {
    companion object {
        // 定义支持正版的提示消息
        private const val SUPPORT_ORIGINAL_MSG = "支持正版，请勿使用盗版软件！"
        
        // 单例模式
        @Volatile
        private var instance: LinkValidator? = null
        
        fun getInstance(context: Context): LinkValidator {
            if (instance == null) {
                synchronized(LinkValidator::class.java) {
                    if (instance == null) {
                        instance = LinkValidator(context)
                    }
                }
            }
            return instance!!
        }
    }
    
    /**
     * 验证链接是否合法
     * @param url 要验证的链接
     * @return 如果链接合法返回true，否则返回false
     */
    fun validateLink(url: String): Boolean {
        // 使用NetworkUtil中定义的isLinkValid方法检查链接是否合法
        val isLinkValid = NetworkUtil.isLinkValid(url)
        
        if (!isLinkValid) {
            // 检测到非法链接，显示提示并终止应用
            showSupportOriginalDialog()
            return false
        }
        
        return true
    }
    
    /**
     * 显示支持正版的提示对话框
     */
    private fun showSupportOriginalDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("提示")
        builder.setMessage(SUPPORT_ORIGINAL_MSG)
        builder.setPositiveButton("确定") { _: DialogInterface, _: Int ->
            // 终止应用
            terminateApp()
        }
        builder.setCancelable(false)
        
        // 在主线程中显示对话框
        ContextHelper.runOnUiThread(context) {
            val dialog = builder.create()
            dialog.show()
        }
    }
    
    /**
     * 终止应用运行
     */
    private fun terminateApp() {
        // 终止所有Activity并退出应用
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        
        // 强制终止应用进程
        System.exit(0)
    }
}
