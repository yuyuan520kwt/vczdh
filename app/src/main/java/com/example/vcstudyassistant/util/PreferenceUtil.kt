package com.example.vcstudyassistant.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil {
    companion object {
        private const val PREF_NAME = "vc_study_assistant_prefs"
        private const val KEY_TIMER_SETTING = "timer_setting"
        private const val KEY_INTERVAL_SETTING = "interval_setting"
        private const val KEY_PAYMENT_VERIFIED = "payment_verified"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_RANDOM_STRING = "random_string"
        private const val KEY_EXPIRY_TIME = "expiry_time"
        private const val KEY_LAST_NOTICE_CHECK = "last_notice_check"
        
        private const val DEFAULT_INTERVAL = 2L // 默认间隔时间（秒）

        private fun getPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        // 定时运行设置
        fun getTimerSetting(context: Context): Long {
            return getPreferences(context).getLong(KEY_TIMER_SETTING, 0)
        }

        fun setTimerSetting(context: Context, timer: Long) {
            getPreferences(context).edit().putLong(KEY_TIMER_SETTING, timer).apply()
        }

        // 答题间隔设置
        fun getIntervalSetting(context: Context): Long {
            return getPreferences(context).getLong(KEY_INTERVAL_SETTING, DEFAULT_INTERVAL)
        }

        fun setIntervalSetting(context: Context, interval: Long) {
            getPreferences(context).edit().putLong(KEY_INTERVAL_SETTING, interval).apply()
        }

        // 付费验证状态
        fun isPaymentVerified(context: Context): Boolean {
            return getPreferences(context).getBoolean(KEY_PAYMENT_VERIFIED, false)
        }

        fun setPaymentVerified(context: Context, verified: Boolean) {
            getPreferences(context).edit().putBoolean(KEY_PAYMENT_VERIFIED, verified).apply()
        }

        // 设备唯一标识
        fun getDeviceId(context: Context): String {
            val prefs = getPreferences(context)
            var deviceId = prefs.getString(KEY_DEVICE_ID, null)
            if (deviceId == null) {
                deviceId = generateDeviceId()
                prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
            }
            return deviceId
        }

        // 随机字符串
        fun getRandomString(context: Context): String {
            val prefs = getPreferences(context)
            var randomString = prefs.getString(KEY_RANDOM_STRING, null)
            if (randomString == null) {
                randomString = generateRandomString()
                prefs.edit().putString(KEY_RANDOM_STRING, randomString).apply()
            }
            return randomString
        }

        fun updateRandomString(context: Context): String {
            val randomString = generateRandomString()
            getPreferences(context).edit().putString(KEY_RANDOM_STRING, randomString).apply()
            return randomString
        }

        // 过期时间
        fun getExpiryTime(context: Context): Long {
            return getPreferences(context).getLong(KEY_EXPIRY_TIME, 0)
        }

        fun setExpiryTime(context: Context, expiryTime: Long) {
            getPreferences(context).edit().putLong(KEY_EXPIRY_TIME, expiryTime).apply()
        }

        // 最后检查公告时间
        fun getLastNoticeCheck(context: Context): Long {
            return getPreferences(context).getLong(KEY_LAST_NOTICE_CHECK, 0)
        }

        fun setLastNoticeCheck(context: Context, time: Long) {
            getPreferences(context).edit().putLong(KEY_LAST_NOTICE_CHECK, time).apply()
        }

        // 生成设备唯一标识
        private fun generateDeviceId(): String {
            return java.util.UUID.randomUUID().toString()
        }

        // 生成14位随机字符串（大写字母+小写字母+数字）
        private fun generateRandomString(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            val sb = StringBuilder(14)
            val random = java.util.Random()
            for (i in 0 until 14) {
                sb.append(chars[random.nextInt(chars.length)])
            }
            return sb.toString()
        }
    }
}