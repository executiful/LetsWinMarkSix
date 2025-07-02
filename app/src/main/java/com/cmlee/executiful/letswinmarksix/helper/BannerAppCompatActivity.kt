package com.cmlee.executiful.letswinmarksix.helper

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration


abstract class BannerAppCompatActivity : AppCompatActivity() {
    lateinit var adContainerView: FrameLayout
    private lateinit var adView: AdView
    override fun onPostCreate(savedInstanceState: Bundle?) {
        init_banner()
//        window.statusBarColor = ContextCompat.getColor(baseContext, R.color.colorPrimaryDark)
        super.onPostCreate(savedInstanceState)
    }

    val adUnitId get() = getString(adUnitStringId)
    protected abstract val adUnitStringId:Int
    private fun loadBanner() {
        // Create an ad request.
        adView = AdView(this)
        adView.adUnitId = adUnitId
        adContainerView.removeAllViews()
        adContainerView.addView(adView)
        val adSize :AdSize = adSize
        adView.setAdSize(adSize)
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                this@BannerAppCompatActivity.onAdLoaded()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                // Gets the domain from which the error came.
                val errorDomain = error.domain
                // Gets the error code. See
                // https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constant-summary
                // for a list of possible codes.
                val errorCode = error.code
                // Gets an error message.
                // For example "Account not approved yet". See
                // https://support.google.com/admob/answer/9905175 for explanations of
                // common errors.
                val errorMessage = error.message
                // Gets additional response information about the request. See
                // https://developers.google.com/admob/android/response-info for more
                // information.
                val responseInfo = error.responseInfo
                // Gets the cause of the error, if available.
                val cause = error.cause
                // All of this information is available via the error's toString() method.
                Log.d("Ads", error.toString())
//                super.onAdFailedToLoad(p0)
            }
        }
    }

    protected abstract fun onAdLoaded()
    private val adSize: AdSize
        get() {
            val windowWidth = resources.configuration.screenWidthDp
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, windowWidth)
        }
//    protected val listDivider: Drawable?
//        get() {
//            val typedValue = TypedValue()
//            theme?.resolveAttribute(android.R.attr.listDivider, typedValue, true)
//            return AppCompatResources.getDrawable(this, typedValue.resourceId)
//        }
    //    private AdSize getAdSize() {
    //        // Determine the screen width (less decorations) to use for the ad width.
    //        Display display = getWindowManager().getDefaultDisplay();
    //        DisplayMetrics outMetrics = new DisplayMetrics();
    //        display.getMetrics(outMetrics);
    //
    //        float density = outMetrics.density;
    //
    //        float adWidthPixels = adContainerView.getWidth();
    //
    //        // If the ad hasn't been laid out, default to the full screen width.
    //        if (adWidthPixels == 0) {
    //            adWidthPixels = outMetrics.widthPixels;
    //        }
    //
    //        int adWidth = (int) (adWidthPixels / density);
    //
    ////        return AdSize.getCurrentOrientationBannerAdSizeWithWidth(this, adWidth);
    //        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    //    }
    private fun init_banner() {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this) { }

        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("ABCDEF012345")).build())
//        adContainerView = findViewById(R.id.ad_view_container)

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView.post{ loadBanner() }
    }

    companion object {
        const val AD_UNIT_ID_SAMPLE = "ca-app-pub-3940256099942544/6300978111"
    }
}