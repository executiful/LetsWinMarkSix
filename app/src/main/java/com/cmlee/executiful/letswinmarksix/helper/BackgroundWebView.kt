package com.cmlee.executiful.letswinmarksix.helper

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BackgroundWebView(context: Context) {
    private val webView: WebView
    private var resultCallback: ((String) -> Unit)? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Initialize WebView
        webView = WebView(context).apply {
            // Basic WebView settings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true

            // Add JavaScript interface for communication
            addJavascriptInterface(WebAppInterface { result ->
                // Handle the retrieved value
                resultCallback?.invoke(result)
            }, "AndroidInterface")

            // Set WebViewClient to handle page loading
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    // Inject JavaScript to retrieve desired value (e.g., page title)
                    view?.evaluateJavascript(
                        "(function() { return document.title; })();"
                    ) { value ->
                        // Remove quotes from the result
                        val cleanValue = value?.replace("\"", "") ?: ""
                        resultCallback?.invoke(cleanValue)
                    }
                }
            }
        }
    }

    // Interface to communicate between JavaScript and Kotlin
    inner class WebAppInterface(private val callback: (String) -> Unit) {
        @JavascriptInterface
        fun sendData(data: String) {
            // Handle data received from JavaScript
            Handler(Looper.getMainLooper()).post {
                callback(data)
            }
        }
    }

    // Function to load URL and set callback for results
    @SuppressLint("SetJavaScriptEnabled")
    fun loadUrl(url: String, callback: (String) -> Unit) {
        this.resultCallback = callback

        // Run WebView operations on IO thread
        coroutineScope.launch {
            webView.loadUrl(url)
        }
    }

    // Example: Inject custom JavaScript to retrieve specific element
    fun getElementById(elementId: String, callback: (String) -> Unit) {
        this.resultCallback = callback
        webView.evaluateJavascript(
            "(function() { return document.getElementById('$elementId').innerText; })();"
        ) { value ->
            val cleanValue = value?.replace("\"", "") ?: ""
            callback(cleanValue)
        }
    }

    // Clean up WebView and coroutine scope
    fun destroy() {
        coroutineScope.cancel() // Cancel all coroutines
        webView.destroy() // Destroy WebView
    }
}
//
//// Usage example in an Activity or Fragment
//fun useWebView(context: Context) {
//    val webView = BackgroundWebView(context)
//
//    // Load a website and get its title
//    webView.loadUrl("https://example.com") { result ->
//        println("Page title: $result")
//    }
//
//    // Get specific element by ID
//    webView.getElementById("myElementId") { content ->
//        println("Element content: $content")
//    }
//
//    // Clean up when done (e.g., in onDestroy of Activity/Fragment)
//    webView.destroy()
//}