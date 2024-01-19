package com.cmlee.executiful.letswinmarksix

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onResume() {
        super.onResume()
        Handler(mainLooper).also{
            it.postDelayed({
                it.postDelayed({finish()}, 500)
                startActivity(Intent(this, MainActivity::class.java))
           }, 100)
        }
    }
}