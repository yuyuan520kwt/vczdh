package com.example.vcstudyassistant.link

import android.content.Context
import android.os.Handler
import android.os.Looper

object ContextHelper {
    /**
     * 在UI线程中运行代码
     */
    fun runOnUiThread(context: Context, action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 当前已经在UI线程
            action()
        } else {
            // 在非UI线程，使用Handler切换到UI线程
            Handler(Looper.getMainLooper()).post(action)
        }
    }
}