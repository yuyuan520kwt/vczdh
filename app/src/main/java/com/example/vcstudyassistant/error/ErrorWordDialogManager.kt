package com.example.vcstudyassistant.error

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.vcstudyassistant.R

class ErrorWordDialogManager(private val context: Context) {
    private val errorWordManager = ErrorWordManager(context)
    
    // 显示错题提示框
    fun showErrorWordsDialog(onReviseClicked: () -> Unit, onCloseClicked: () -> Unit) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_error_words, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        
        // 获取UI组件
        val tvErrorCount = view.findViewById<TextView>(R.id.tv_error_count)
        val llErrorWords = view.findViewById<LinearLayout>(R.id.ll_error_words)
        val btnRevise = view.findViewById<Button>(R.id.btn_revise)
        val btnClose = view.findViewById<Button>(R.id.btn_close)
        
        // 获取错题列表
        val errorWords = errorWordManager.getErrorWords()
        
        // 设置错误词汇数量
        tvErrorCount.text = "共有${errorWords.size}个错误词汇"
        
        // 动态添加错误词汇
        for (errorWord in errorWords) {
            val wordView = createErrorWordView(errorWord)
            llErrorWords.addView(wordView)
        }
        
        // 设置按钮点击事件
        btnRevise.setOnClickListener {
            dialog.dismiss()
            onReviseClicked()
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
            onCloseClicked()
        }
        
        // 显示对话框
        dialog.show()
    }
    
    // 创建错误词汇视图
    private fun createErrorWordView(errorWord: ErrorWordManager.ErrorWord): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_error_word, null)
        
        val tvWord = view.findViewById<TextView>(R.id.tv_word)
        val tvMeaning = view.findViewById<TextView>(R.id.tv_meaning)
        val tvErrorCount = view.findViewById<TextView>(R.id.tv_error_count)
        
        tvWord.text = errorWord.word
        tvMeaning.text = errorWord.meaning
        tvErrorCount.text = "错误次数: ${errorWord.errorCount}"
        
        return view
    }
    
    // 显示需要手动订正的提示框
    fun showManualRevisionDialog(onOkClicked: () -> Unit) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_manual_revision, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        
        val btnOk = view.findViewById<Button>(R.id.btn_ok)
        
        btnOk.setOnClickListener {
            dialog.dismiss()
            onOkClicked()
        }
        
        dialog.show()
    }
    
    // 显示奖励提示框
    fun showRewardDialog(onCloseClicked: () -> Unit) {
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_reward, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        
        val btnClose = view.findViewById<Button>(R.id.btn_close)
        
        btnClose.setOnClickListener {
            dialog.dismiss()
            onCloseClicked()
        }
        
        dialog.show()
    }
}