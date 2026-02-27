package com.example.vcstudyassistant.error

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ErrorWordManager(private val context: Context) {
    private val PREF_NAME = "error_words_prefs"
    private val KEY_ERROR_WORDS = "error_words"
    
    private val MAX_ERROR_COUNT = 3 // 最大错误次数
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // 错题数据类
    data class ErrorWord(
        val word: String, // 单词
        val meaning: String, // 释义
        val errorCount: Int = 0, // 错误次数
        val type: String // 题型
    )
    
    // 添加错题
    fun addErrorWord(word: String, meaning: String, type: String) {
        val errorWords = getErrorWords()
        
        // 查找是否已存在该错题
        val existingIndex = errorWords.indexOfFirst { it.word == word }
        
        if (existingIndex != -1) {
            // 更新错误次数
            val updatedWord = errorWords[existingIndex].copy(errorCount = errorWords[existingIndex].errorCount + 1)
            errorWords[existingIndex] = updatedWord
        } else {
            // 添加新错题
            val newErrorWord = ErrorWord(word, meaning, 1, type)
            errorWords.add(newErrorWord)
        }
        
        // 保存更新后的错题列表
        saveErrorWords(errorWords)
    }
    
    // 获取所有错题
    fun getErrorWords(): MutableList<ErrorWord> {
        val json = prefs.getString(KEY_ERROR_WORDS, "[]")
        val type = object : TypeToken<MutableList<ErrorWord>>() {}.type
        return gson.fromJson(json, type)
    }
    
    // 移除错题
    fun removeErrorWord(word: String) {
        val errorWords = getErrorWords()
        errorWords.removeAll { it.word == word }
        saveErrorWords(errorWords)
    }
    
    // 清空所有错题
    fun clearErrorWords() {
        prefs.edit().remove(KEY_ERROR_WORDS).apply()
    }
    
    // 检查是否需要手动订正
    fun needManualRevision(word: String): Boolean {
        val errorWords = getErrorWords()
        val errorWord = errorWords.find { it.word == word }
        return errorWord?.errorCount ?: 0 >= MAX_ERROR_COUNT
    }
    
    // 保存错题列表
    private fun saveErrorWords(errorWords: MutableList<ErrorWord>) {
        val json = gson.toJson(errorWords)
        prefs.edit().putString(KEY_ERROR_WORDS, json).apply()
    }
    
    // 获取错题数量
    fun getErrorWordCount(): Int {
        return getErrorWords().size
    }
}