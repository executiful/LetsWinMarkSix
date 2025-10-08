package com.cmlee.executiful.letswinmarksix.helper

import android.content.Context
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.jsc_marksix
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.url_marksix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebDataRepository(private val context: Context) {

    private val webViewHelper = SimpleWebViewHelper()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    interface WebDataCallback {
        fun onSuccess(data: String)
        fun onError(error: String)
    }

    fun getNextDrawData(url: String = url_marksix, callback: WebDataCallback){
        coroutineScope.launch {
            try {
                val result = webViewHelper.extractData(context, url, jsc_marksix.trimIndent())
                callback.onSuccess(result)
            } catch (e: Exception) {
                callback.onError("Failed to extract element: ${e.message}")
            }
        }
    }

    // Simple method to get website title
    fun getWebsiteTitle(url: String, callback: WebDataCallback) {
        coroutineScope.launch {
            try {
                val title = webViewHelper.extractData(context, url, "document.title")
                callback.onSuccess(title)
            } catch (e: Exception) {
                callback.onError("Failed to get title: ${e.message}")
            }
        }
    }

    // Method to extract text from CSS selector
    fun getElementText(url: String, cssSelector: String, callback: WebDataCallback) {
        val javascript = """
            (function() {
                const element = document.querySelector('$cssSelector');
                return element ? element.textContent.trim() : 'Element not found';
            })()
        """.trimIndent()

        coroutineScope.launch {
            try {
                val result = webViewHelper.extractData(context, url, javascript)
                callback.onSuccess(result)
            } catch (e: Exception) {
                callback.onError("Failed to extract element: ${e.message}")
            }
        }
    }

    // Method to extract multiple elements
    fun getMultipleElements(url: String, selectors: Map<String, String>, callback: WebDataCallback) {
        val javascript = buildString {
            append("(function() {")
            append("const result = {};")
            selectors.forEach { (key, selector) ->
                append("""
                    try {
                        const element = document.querySelector('$selector');
                        result['$key'] = element ? element.textContent.trim() : null;
                    } catch(e) { result['$key'] = 'Error: ' + e.message; }
                """.trimIndent())
            }
            append("return JSON.stringify(result);")
            append("})()")
        }

        coroutineScope.launch {
            try {
                val result = webViewHelper.extractData(context, url, javascript)
                callback.onSuccess(result)
            } catch (e: Exception) {
                callback.onError("Failed to extract data: ${e.message}")
            }
        }
    }

    // Method to get all links
    fun getAllLinks(url: String, callback: WebDataCallback) {
        val javascript = """
            (function() {
                const links = Array.from(document.getElementsByTagName('a'));
                return JSON.stringify(links.map(link => ({
                    text: link.textContent.trim(),
                    href: link.href
                })));
            })()
        """.trimIndent()

        coroutineScope.launch {
            try {
                val result = webViewHelper.extractData(context, url, javascript)
                callback.onSuccess(result)
            } catch (e: Exception) {
                callback.onError("Failed to get links: ${e.message}")
            }
        }
    }
}