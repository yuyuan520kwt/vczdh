package com.example.vcstudyassistant.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.vcstudyassistant.answer.AnswerManager

class MyAccessibilityService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"
    
    companion object {
        private var instance: MyAccessibilityService? = null
        
        // 获取服务实例
        fun getInstance(): MyAccessibilityService? {
            return instance
        }
        
        // 查找并点击按钮
        fun findAndClickButton(buttonText: String): Boolean {
            val service = getInstance() ?: return false
            return service.findAndClickButtonInternal(buttonText)
        }
        
        // 识别英文语句
        fun identifyEnglishSentence(): String {
            val service = getInstance() ?: return ""
            return service.identifyEnglishSentenceInternal()
        }
        
        // 识别绿色待作答字体
        fun identifyGreenWord(): String {
            val service = getInstance() ?: return ""
            return service.identifyGreenWordInternal()
        }
        
        // 获取答案选项
        fun getAnswerOptions(): List<String> {
            val service = getInstance() ?: return emptyList()
            return service.getAnswerOptionsInternal()
        }
        
        // 点击选项
        fun clickOption(option: String): Boolean {
            val service = getInstance() ?: return false
            return service.clickOptionInternal(option)
        }
        
        // 识别汉语释义
        fun identifyChineseMeaning(): String {
            val service = getInstance() ?: return ""
            return service.identifyChineseMeaningInternal()
        }
        
        // 输入单词
        fun typeWord(word: String): Boolean {
            val service = getInstance() ?: return false
            return service.typeWordInternal(word)
        }
        
        // 识别问题
        fun identifyQuestion(): String {
            val service = getInstance() ?: return ""
            return service.identifyQuestionInternal()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "无障碍服务已连接")
        
        // 配置无障碍服务
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        info.packageNames = arrayOf("com.voiceofenglish", packageName)
        
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 处理无障碍事件
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // 窗口状态改变
                Log.d(TAG, "窗口状态改变: ${event.packageName}, ${event.className}")
                
                // 检查是否弹出学习时间提示框
                handleStudyTimeDialog()
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                // 视图被点击
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                // 文本改变
            }
        }
    }
    
    /**
     * 处理学习时间提示框
     * 当屏幕弹出含"你已学习30分钟"字样的提示框时，自动点击"不再提醒"按钮
     */
    private fun handleStudyTimeDialog() {
        val rootNode = rootInActiveWindow ?: return
        
        // 查找包含"你已学习30分钟"字样的提示框
        val dialogNodes = rootNode.findAccessibilityNodeInfosByText("你已学习30分钟")
        if (dialogNodes.isNotEmpty()) {
            // 查找"不再提醒"按钮
            val noRemindButton = findNodeByText(rootNode, "不再提醒")
            if (noRemindButton != null) {
                // 点击"不再提醒"按钮
                performClick(noRemindButton)
                Log.d(TAG, "已点击'不再提醒'按钮")
            }
        }
    }
    
    // 添加到伴生对象，使外部可以调用
    companion object {
        // ... 现有的伴生对象方法 ...
        
        // 处理学习时间提示框
        fun handleStudyTimeDialogIfExists() {
            val service = getInstance() ?: return
            service.handleStudyTimeDialog()
        }
    }

    override fun onInterrupt() {
        // 服务被中断
        Log.d(TAG, "无障碍服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "无障碍服务已销毁")
    }

    // 查找并点击按钮（内部方法）
    private fun findAndClickButtonInternal(buttonText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val buttonNode = findNodeByText(rootNode, buttonText)
        if (buttonNode != null) {
            return performClick(buttonNode)
        }
        return false
    }

    // 识别英文语句（内部方法）
    private fun identifyEnglishSentenceInternal(): String {
        // TODO: 实现英文语句识别逻辑
        return "This is an example sentence with the target word."
    }

    // 识别绿色待作答字体（内部方法）
    private fun identifyGreenWordInternal(): String {
        // TODO: 实现绿色单词识别逻辑
        return "example"
    }

    // 获取答案选项（内部方法）
    private fun getAnswerOptionsInternal(): List<String> {
        // TODO: 实现答案选项获取逻辑
        return listOf("选项A", "选项B", "选项C", "选项D")
    }

    // 点击选项（内部方法）
    private fun clickOptionInternal(option: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val optionNode = findNodeByText(rootNode, option)
        if (optionNode != null) {
            return performClick(optionNode)
        }
        return false
    }

    // 识别汉语释义（内部方法）
    private fun identifyChineseMeaningInternal(): String {
        // TODO: 实现汉语释义识别逻辑
        return "示例"
    }

    // 输入单词（内部方法）
    private fun typeWordInternal(word: String): Boolean {
        // TODO: 实现单词输入逻辑
        return true
    }

    // 识别问题（内部方法）
    private fun identifyQuestionInternal(): String {
        // TODO: 实现问题识别逻辑
        return "What is the meaning of this word?"
    }

    // 根据文本查找节点
    private fun findNodeByText(rootNode: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        if (nodes.isNotEmpty()) {
            return nodes[0]
        }
        return null
    }

    // 执行点击操作
    private fun performClick(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            // 如果节点不可点击，尝试查找其父节点
            val parent = node.parent
            if (parent != null) {
                val result = performClick(parent)
                parent.recycle()
                return result
            }
        }
        return false
    }
}