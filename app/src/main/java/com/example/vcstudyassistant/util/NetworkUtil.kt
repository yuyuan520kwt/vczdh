package com.example.vcstudyassistant.util

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

class NetworkUtil {
    companion object {
        private const val NOTICE_URL = "https://sharechain.qq.com/a5fd16d4d7e3e57ffa9b137e63ed8211?qq_aio_chat_type=2"
        private const val VERIFICATION_URL = "https://sharechain.qq.com/f0be92806c338acb700719e6bb25c298?qq_aio_chat_type=2"
        private const val TIME_URL = "https://www.baidu.com"
        private const val BAIDU_SEARCH_URL = "https://www.baidu.com/s?wd="
        private const val YOUDAO_URL = "https://dict.youdao.com/w/" 
        
        private val client = OkHttpClient()

        // 获取公告内容
    @Throws(IOException::class)
    fun fetchNotice(context: Context): String {
        // 验证链接
        val linkValidator = com.example.vcstudyassistant.link.LinkValidator.getInstance(context)
        if (!linkValidator.validateLink(NOTICE_URL)) {
            throw IOException("Invalid link: $NOTICE_URL")
        }
        
        val request = Request.Builder()
            .url(NOTICE_URL)
            .build()

        client.newCall(request).execute().use { response ->
            return if (response.isSuccessful) {
                response.body?.string() ?: ""
            } else {
                throw IOException("Unexpected code $response")
            }
        }
    }

        // 验证支付状态
        @Throws(IOException::class)
        fun verifyPayment(context: Context, randomString: String): Boolean {
            // 验证链接
            val linkValidator = com.example.vcstudyassistant.link.LinkValidator.getInstance(context)
            if (!linkValidator.validateLink(VERIFICATION_URL)) {
                throw IOException("Invalid link: $VERIFICATION_URL")
            }
            
            val request = Request.Builder()
                .url(VERIFICATION_URL)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val content = response.body?.string() ?: ""
                    return content.contains(randomString)
                } else {
                    throw IOException("Unexpected code $response")
                }
            }
        }

        // 获取当前时间（从网络）
        @Throws(IOException::class)
        fun getCurrentTime(context: Context): Long {
            // 验证链接
            val linkValidator = com.example.vcstudyassistant.link.LinkValidator.getInstance(context)
            if (!linkValidator.validateLink(TIME_URL)) {
                throw IOException("Invalid link: $TIME_URL")
            }
            
            val request = Request.Builder()
                .url(TIME_URL)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return System.currentTimeMillis()
                } else {
                    throw IOException("Unexpected code $response")
                }
            }
        }

        // 检查链接是否合法
        fun isLinkValid(url: String): Boolean {
            return url == NOTICE_URL || url == VERIFICATION_URL || url == TIME_URL || url.startsWith(BAIDU_SEARCH_URL) || url.startsWith(YOUDAO_URL)
        }
        
        /**
         * 搜索单词在特定语句语境下的含义
         */
        @Throws(IOException::class)
        fun searchWordMeaning(context: Context, word: String, sentence: String): String {
            // 构建搜索URL
            val searchUrl = BAIDU_SEARCH_URL + "\"${word}\" " + sentence
            
            // 验证链接
            val linkValidator = com.example.vcstudyassistant.link.LinkValidator.getInstance(context)
            if (!linkValidator.validateLink(searchUrl)) {
                throw IOException("Invalid link: $searchUrl")
            }
            
            // 发送请求
            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val htmlContent = response.body?.string() ?: ""
                    // 使用Jsoup解析HTML内容，提取单词含义
                    return parseWordMeaningFromHtml(htmlContent, word)
                } else {
                    throw IOException("Unexpected code $response")
                }
            }
        }
        
        /**
         * 搜索汉语释义对应的英文单词标准拼写
         */
        @Throws(IOException::class)
        fun searchWordSpelling(context: Context, chineseMeaning: String): String {
            // 使用有道词典查询
            val searchUrl = YOUDAO_URL + chineseMeaning
            
            // 验证链接
            val linkValidator = com.example.vcstudyassistant.link.LinkValidator.getInstance(context)
            if (!linkValidator.validateLink(searchUrl)) {
                throw IOException("Invalid link: $searchUrl")
            }
            
            val request = Request.Builder()
                .url(searchUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val htmlContent = response.body?.string() ?: ""
                    // 解析HTML内容，提取英文单词
                    return parseWordSpellingFromHtml(htmlContent)
                } else {
                    throw IOException("Unexpected code $response")
                }
            }
        }
        
        /**
         * 从HTML内容中解析单词含义
         */
        private fun parseWordMeaningFromHtml(htmlContent: String, word: String): String {
            try {
                val doc: Document = Jsoup.parse(htmlContent)
                // 这里简化处理，实际需要根据具体的HTML结构进行解析
                // 查找包含单词含义的元素
                val meaningElements = doc.select(".c-abstract, .content, .meaning")
                
                for (element in meaningElements) {
                    val text = element.text()
                    if (text.contains(word)) {
                        return text
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }
        
        /**
         * 从HTML内容中解析英文单词拼写
         */
        private fun parseWordSpellingFromHtml(htmlContent: String): String {
            try {
                val doc: Document = Jsoup.parse(htmlContent)
                // 这里简化处理，实际需要根据有道词典的HTML结构进行解析
                val wordElements = doc.select(".keyword, .word, .phonetic")
                
                for (element in wordElements) {
                    val text = element.text()
                    if (text.matches(Regex("^[a-zA-Z]+$"))) {
                        return text
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }
    }
}