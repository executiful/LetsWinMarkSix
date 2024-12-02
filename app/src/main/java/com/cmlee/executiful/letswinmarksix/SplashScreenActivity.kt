package com.cmlee.executiful.letswinmarksix

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    var delay = 100L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState==null) delay=500
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onResume() {
        super.onResume()
        if (BuildConfig.DEBUG) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Handler(mainLooper).also {
                it.postDelayed({
                    it.postDelayed({ finish() }, delay)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, 100)
            }
        }
    }
}