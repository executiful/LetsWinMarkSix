package com.cmlee.executiful.letswinmarksix.helper

import android.content.Context
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SafeWebViewExtractor(private val context: Context) {

    companion object {
        private const val TAG = "WebViewExtractor"
    }

    private var webView: WebView? = null

    private fun initializeWebView(): WebView {
        return try {
            WebView(context).apply {
                // Basic settings
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false

                    // Cache settings - using modern approach
                    cacheMode = WebSettings.LOAD_DEFAULT

                    // Allow file access if needed
                    allowFileAccess = false
                    allowContentAccess = false

                    // Mixed content support for HTTPS sites with HTTP resources
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//                    }

                    // Safe browsing (API 26+)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        safeBrowsingEnabled = true
                    }
                }

                // Important: Set WebChromeClient to prevent crashes
                webChromeClient = WebChromeClient()

                Log.d(TAG, "WebView initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing WebView: ${e.message}")
            throw RuntimeException("Failed to initialize WebView", e)
        }
    }

    suspend fun extractDataFromUrl(
        url: String,
        javascriptCode: String,
        timeoutMillis: Long = 30000L
    ): String = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            try {
                // Initialize WebView if not already done
                if (webView == null) {
                    webView = initializeWebView()
                }

                val currentWebView = webView ?: throw IllegalStateException("WebView not initialized")

                var isCompleted = false
                val timeoutRunnable = Runnable {
                    if (!isCompleted) {
                        continuation.resumeWithException(
                            RuntimeException("Operation timed out after $timeoutMillis ms")
                        )
                        cleanup()
                    }
                }

                // Set timeout
                val handler = android.os.Handler()
                handler.postDelayed(timeoutRunnable, timeoutMillis)

                currentWebView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)

                        Log.d(TAG, "Page finished loading: $pageUrl")

                        // Execute JavaScript to extract data
                        currentWebView.evaluateJavascript(javascriptCode) { result ->
                            try {
                                if (!isCompleted) {
                                    isCompleted = true
                                    handler.removeCallbacks(timeoutRunnable)

                                    val cleanedResult = result?.removeSurrounding("\"") ?: ""
                                    Log.d(TAG, "JavaScript execution result: $cleanedResult")
                                    continuation.resume(cleanedResult)
                                    cleanup()
                                }
                            } catch (e: Exception) {
                                if (!isCompleted) {
                                    isCompleted = true
                                    handler.removeCallbacks(timeoutRunnable)
                                    continuation.resumeWithException(e)
                                    cleanup()
                                }
                            }
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)

                        if (!isCompleted) {
                            isCompleted = true
                            handler.removeCallbacks(timeoutRunnable)
                            continuation.resumeWithException(
                                RuntimeException("WebView error: $description (code: $errorCode)")
                            )
                            cleanup()
                        }
                    }
                }

                // Load the URL
                Log.d(TAG, "Loading URL: $url")
                currentWebView.loadUrl(url)

            } catch (e: Exception) {
                Log.e(TAG, "Error in extractDataFromUrl: ${e.message}")
                continuation.resumeWithException(e)
                cleanup()
            }

            // Cleanup on cancellation
            continuation.invokeOnCancellation {
                Log.d(TAG, "Operation cancelled")
                cleanup()
            }
        }
    }

    private fun cleanup() {
        try {
            webView?.stopLoading()
            webView?.destroy()
            webView = null
            Log.d(TAG, "WebView cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }

    fun destroy() {
        cleanup()
    }
}