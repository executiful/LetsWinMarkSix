package com.cmlee.executiful.letswinmarksix.helper

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SimpleWebViewHelper {

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun extractData(
        context: Context,
        url: String,
        javascriptCode: String
    ): String = suspendCancellableCoroutine { continuation ->
        val webView = WebView(context).apply {
            // Minimal required settings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled=true
            settings.loadsImagesAutomatically = false
//            settings.loadWithOverviewMode = true
//            settings.useWideViewPort = true
//            settings.setSupportZoom(false)
//            settings.builtInZoomControls = false
//            settings.displayZoomControls = false
//            settings.javaScriptCanOpenWindowsAutomatically=false
            webChromeClient = WebChromeClient()
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, pageUrl: String?) {
                webView.evaluateJavascript(javascriptCode) { result ->
                    val cleanResult = result?.removeSurrounding("\"") ?: ""
                    continuation.resume(cleanResult)
                    webView.destroy()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                continuation.resumeWithException(
                    RuntimeException("Failed to load page: $description")
                )
                webView.destroy()
            }
        }

        // Load URL
        webView.loadUrl(url)

        // Cleanup on cancellation
        continuation.invokeOnCancellation {
            webView.destroy()
        }
    }
}