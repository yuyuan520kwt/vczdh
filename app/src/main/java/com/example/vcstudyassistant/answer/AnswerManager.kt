package com.example.vcstudyassistant.answer

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.vcstudyassistant.accessibility.MyAccessibilityService
import com.example.vcstudyassistant.audio.AudioManager
import com.example.vcstudyassistant.error.ErrorWordManager
import com.example.vcstudyassistant.error.ErrorWordDialogManager
import com.example.vcstudyassistant.util.NetworkUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnswerManager(private val context: Context) {
    private lateinit var timer: Handler
    private var timerTask: Runnable? = null
    private var isRunning = false
    private var totalTime = 0L // 总运行时间（秒）
    private var interval = 2L // 答题间隔（秒）
    private var currentTime = 0L
    
    private val errorWordManager = ErrorWordManager(context)
    private val errorWordDialogManager = ErrorWordDialogManager(context)
    
    // 题型枚举
    enum class QuestionType {
        WORD_MATCHING, // 英文语句词义匹配题
        WORD_SPELLING, // 单词拼写题
        WORD_LISTENING, // 单词听力题
        LISTENING_TEST // 听力测试题
    }

    init {
        timer = Handler(Looper.getMainLooper())
    }

    // 设置定时时间
    fun setTimer(seconds: Long) {
        totalTime = seconds
    }

    // 设置答题间隔
    fun setInterval(seconds: Long) {
        interval = seconds
    }

    // 启动答题流程
    fun startAnswerProcess() {
        if (isRunning) return
        
        isRunning = true
        currentTime = 0
        
        // 启动定时任务
        timerTask = Runnable {
            currentTime++
            if (currentTime >= totalTime && totalTime > 0) {
                stopAnswerProcess()
                return@Runnable
            }
            
            // 执行答题逻辑
            performAnswerLogic()
            
            // 间隔时间后再次执行
            timer.postDelayed(this, interval * 1000)
        }
        
        timer.postDelayed(timerTask!!, 0)
    }

    // 停止答题流程
    fun stopAnswerProcess() {
        isRunning = false
        if (timerTask != null) {
            timer.removeCallbacks(timerTask!!)
            timerTask = null
        }
        
        // 显示错题提示框
        showErrorWordsDialog()
    }
    
    /**
     * 记录错题
     */
    fun recordErrorWord(word: String, meaning: String, type: String) {
        errorWordManager.addErrorWord(word, meaning, type)
        
        // 检查是否需要手动订正
        if (errorWordManager.needManualRevision(word)) {
            showManualRevisionDialog()
        }
    }
    
    /**
     * 显示错题提示框
     */
    private fun showErrorWordsDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            errorWordDialogManager.showErrorWordsDialog(
                onReviseClicked = { startRevisionProcess() },
                onCloseClicked = { checkRewardCondition() }
            )
        }
    }
    
    /**
     * 显示手动订正提示框
     */
    private fun showManualRevisionDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            errorWordDialogManager.showManualRevisionDialog {
                // 用户确认后，暂停程序运行
                stopAnswerProcess()
            }
        }
    }
    
    /**
     * 开始订正流程
     */
    private fun startRevisionProcess() {
        // TODO: 实现错题订正流程
        // 这里可以重新启动答题流程，专注于错题
    }
    
    /**
     * 检查奖励条件
     */
    private fun checkRewardCondition() {
        // 当所有错题都已订正完成时，显示奖励提示
        val errorWords = errorWordManager.getErrorWords()
        if (errorWords.isEmpty()) {
            showRewardDialog()
        }
    }
    
    /**
     * 显示奖励提示框
     */
    private fun showRewardDialog() {
        CoroutineScope(Dispatchers.Main).launch {
            errorWordDialogManager.showRewardDialog {
                // 用户关闭奖励提示后，自动关闭弹窗并提交
                autoCloseAndSubmit()
            }
        }
    }
    
    /**
     * 自动关闭奖励弹窗并提交
     */
    private fun autoCloseAndSubmit() {
        // 自动识别并点击关闭按钮
        MyAccessibilityService.findAndClickButton(context.getString(R.string.btn_close))
        
        // 自动识别并点击提交按钮
        MyAccessibilityService.findAndClickButton(context.getString(R.string.btn_submit))
    }

    // 执行答题逻辑
    private fun performAnswerLogic() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 检查学习时间提示框
                MyAccessibilityService.handleStudyTimeDialogIfExists()
                
                // 前置操作流程
                performPreOperation()
                
                // 识别当前题型
                val questionType = identifyQuestionType()
                
                // 根据题型执行作答逻辑
                when (questionType) {
                    QuestionType.WORD_MATCHING -> handleWordMatching()
                    QuestionType.WORD_SPELLING -> handleWordSpelling()
                    QuestionType.WORD_LISTENING -> handleWordListening()
                    QuestionType.LISTENING_TEST -> handleListeningTest()
                    else -> handleUnknownQuestion()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // 处理异常
                }
            }
        }
    }

    // 前置操作流程
    private suspend fun performPreOperation() {
        // 1. 识别并点击"下一个"按钮
        var hasNextButton = true
        while (hasNextButton) {
            hasNextButton = MyAccessibilityService.findAndClickButton(context.getString(R.string.btn_next))
            if (!hasNextButton) break
            
            // 2. 检查是否弹出"温馨提示"弹窗，点击"暂不跟读"
            val hasWarmPrompt = MyAccessibilityService.findAndClickButton(context.getString(R.string.btn_not_follow))
            
            // 如果没有温馨提示，继续寻找下一个按钮
            if (!hasWarmPrompt) {
                // 短暂延迟后再次检查是否有下一个按钮
                Thread.sleep(500)
            }
        }
    }

    // 识别当前题型
    private suspend fun identifyQuestionType(): QuestionType {
        // TODO: 实现屏幕识别逻辑，判断当前题型
        // 这里暂时返回一个默认值，实际需要根据屏幕内容识别
        return QuestionType.WORD_MATCHING
    }

    // 处理英文语句词义匹配题
    private suspend fun handleWordMatching() {
        // 1. 识别屏幕中部英文语句，区分标黑常规字体与绿色待作答字体
        val sentence = MyAccessibilityService.identifyEnglishSentence()
        val targetWord = MyAccessibilityService.identifyGreenWord()
        
        // 2. 联网搜索绿色字体单词在当前语句语境下的准确含义
        val wordMeaning = NetworkUtil.searchWordMeaning(context, targetWord, sentence)
        
        // 3. 识别语句下方的选项列表，匹配正确答案并自动执行点击操作
        val options = MyAccessibilityService.getAnswerOptions()
        val correctOption = matchCorrectOption(wordMeaning, options)
        if (correctOption != null) {
            MyAccessibilityService.clickOption(correctOption)
        }
    }

    // 处理单词拼写题
    private suspend fun handleWordSpelling() {
        // 1. 识别屏幕中展示的目标单词汉语释义
        val chineseMeaning = MyAccessibilityService.identifyChineseMeaning()
        
        // 2. 联网检索对应汉语释义的英文单词标准拼写
        val wordSpelling = NetworkUtil.searchWordSpelling(context, chineseMeaning)
        
        // 3. 识别屏幕下方虚拟键盘，按单词字母顺序依次自动点击输入
        MyAccessibilityService.typeWord(wordSpelling)
    }

    // 处理单词听力题
    private suspend fun handleWordListening() {
        // 1. 音频采集：通过麦克风实时获取维词APP发出的音频信号
        val audioData = AudioManager.recordAudio()
        
        // 2. 语音识别：将采集到的音频转换为对应的英语单词文本
        val recognizedWord = AudioManager.recognizeAudio(audioData)
        
        // 3. 答案匹配：对比屏幕下方选项列表，自动选择正确答案并点击
        val options = MyAccessibilityService.getAnswerOptions()
        val correctOption = matchCorrectOption(recognizedWord, options)
        if (correctOption != null) {
            MyAccessibilityService.clickOption(correctOption)
        }
    }

    // 处理听力测试题
    private suspend fun handleListeningTest() {
        // 1. 音频录制与处理
        val audioData = AudioManager.recordAudio()
        val translatedContent = AudioManager.translateAudio(audioData)
        
        // 2. 问题识别
        val question = MyAccessibilityService.identifyQuestion()
        
        // 3. 答案分析
        val options = MyAccessibilityService.getAnswerOptions()
        val correctOption = analyzeAnswer(question, translatedContent, options)
        if (correctOption != null) {
            MyAccessibilityService.clickOption(correctOption)
        }
    }

    // 处理未知题型
    private suspend fun handleUnknownQuestion() {
        // 记录日志或给出提示
    }

    // 匹配正确答案
    private fun matchCorrectOption(keyword: String, options: List<String>): String? {
        // TODO: 实现答案匹配逻辑
        // 这里暂时返回第一个选项，实际需要根据关键词匹配
        return options.firstOrNull()
    }

    // 分析答案
    private fun analyzeAnswer(question: String, content: String, options: List<String>): String? {
        // TODO: 实现答案分析逻辑
        // 这里暂时返回第一个选项，实际需要根据问题和内容分析
        return options.firstOrNull()
    }
}
