package com.cmlee.executiful.letswinmarksix.helper

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebViewWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    val TAG = "Web123"
    val javascript = """
        new Promise((resolve) => {
            // Wait for specific elements to be available
            function checkElements() {
                const elements = document.querySelectorAll('.next-draw-table-header .next-draw-table-item, .jackpot-row, .estdiv-row');
                if (elements.length > 0) {
                    const data = Array.from(elements).map(el => ({
                        content: el.textContent,
                        attributes: Array.from(el.attributes).reduce((acc, attr) => {
                            acc[attr.name] = attr.value;
                            return acc;
                        }, {})
                    }));
                    resolve(JSON.stringify({ success: true, data: data }));
                } else {
                    // Retry after a short delay
                    setTimeout(checkElements, 100);
                }
            }
            checkElements();
        });
    """.trimIndent()
    val jsClick = """
            javascript:(function(){
                var button = document.querySelector('#subTypeMobileN');
                if(button){
                    button.click();
                    return document.querySelector('.blockNextDrawBody').className
                } else {
                    console.log('subTypeMobileN not found');
                    return "--"
                }
                    return document.readyState;
            })();
            """.trimIndent()
    val jsData = """
            javascript:(function() {
                var resultElement = document.querySelector('.blockNextDrawBody') 
                return (resultElement ? resultElement.innerText || resultElement.innerHTML : "No data")+document.readyState;
            })();
            """.trimIndent()
    var isFinished = false
//    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun extractDataFromWebPage(url: String): String {
        return withContext(Dispatchers.Main) {
            // Note: WebView must run on main thread
            val webView = WebView(applicationContext)
            var result = ""
            val latch = CountDownLatch(1)
            Log.d(TAG, "fun $url")
            var isFinished = false
            webView.settings.javaScriptEnabled = true
            webView.settings.loadsImagesAutomatically = false
            webView.settings.domStorageEnabled = true
//            webView.settings.useWideViewPort = true
//            webView.addJavascriptInterface(this, "Android")
            webView.webViewClient = object : WebViewClient() {
                var count = 15
                val aa1:(Boolean)->Unit = {str->
                    Thread.sleep(100)
                    if(str) {
                        webView.evaluateJavascript(jsData, cb2)
                    } else if(count>0){
//                     Thread.sleep(20)
                        count--
                        webView.evaluateJavascript(jsClick, cb1)
                    }
                }
                val cb2 = ValueCallback<String> { str->
                    Log.d(TAG, "$str:Result")
                    latch.countDown()
                    webView.destroy()
                }
                val cb1 = ValueCallback<String> { str->
                    if(true){ Log.d(TAG, "click done $str") }
                    aa1(str.contains("blockNextDrawBody"))
                }
                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "onPageFinished $url")
                    if(!isFinished){
                        isFinished=true
                        aa1(false )
                    }
                }
            }

            webView.loadUrl(url)
            latch.await(100000, TimeUnit.MILLISECONDS) // Timeout after 30 seconds
//            webView.destroy()
            result
        }
    }
//    @JavascriptInterface
//    fun onDataReceived(data:String) {
//        Log.d(TAG, "received:$data")
//    }

    private fun saveResult(result: String) {
        // Save to SharedPreferences, database, or file
        val sharedPref = applicationContext.getSharedPreferences("web_data", Context.MODE_PRIVATE)
        sharedPref.edit().putString("last_scraped_data", result).apply()
    }
    override suspend fun doWork(): Result {
        return try {
            val url = inputData.getString("url") ?: return Result.failure()
            val result = extractDataFromWebPage(url)
            Log.d(TAG, "fun2 $url")

            // Save or process the result
            saveResult(result)
            Log.d(TAG, "save $url")

            Result.success()
        } catch (e: Exception) {
            Log.d(TAG, "exception ${e.message}")
            Result.failure()
        }
    }
}