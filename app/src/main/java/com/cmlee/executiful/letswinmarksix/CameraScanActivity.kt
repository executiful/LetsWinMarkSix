package com.cmlee.executiful.letswinmarksix

//import androidx.camera.lifecycle.ProcessCameraProvider
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_banker
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_num
import com.cmlee.executiful.letswinmarksix.databinding.ActivityCameraScanBinding
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.NegativeButton
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.PositiveButton
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraScanActivity : AppCompatActivity() {
    private var interstitialAd: InterstitialAd? = null
    private lateinit var viewBinding: ActivityCameraScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private var outputString = StringBuilder()
    private var largeBannerLoaded = false
    private var bpcb = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    private lateinit var dlgConfirm :AlertDialog

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityCameraScanBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
//        setContentView(R.layout.activity_camera_scan)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        viewBinding.adView.adListener = object : AdListener(){
            override fun onAdLoaded() {
                super.onAdLoaded()
                largeBannerLoaded = true
            }
        }
        viewBinding.adView.loadAd(adRequest)
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCameraWhile()
        } else {
            requestPermissions()
        }

        // Set up the listeners for take photo and video capture buttons

        cameraExecutor = Executors.newSingleThreadExecutor()
//        adContainerView = viewBinding.adViewContainer



        onBackPressedDispatcher.addCallback(bpcb)
    }
    private fun initDlg(){
//        if(dlgConfirm!=null) return
        dlgConfirm = AlertDialog.Builder(this).setCancelable(false)
            .setTitle(R.string.title_scanticket)
            .setNeutralButton("退出") { d, _ ->
                Handler(mainLooper).postDelayed({
                    d.dismiss()
                    finish()
                }, if(interstitialAd==null) 2000 else 0)
            }.setNegativeButton(android.R.string.copy) { _, _ ->
            }.setPositiveButton("掃瞄") { d, _ ->
                d.dismiss()
                goToNextLevel()
//                if (interstitialAd == null) {
//                    finish()
//                } else {
                Handler(mainLooper).postDelayed({
                    startCamera()
                }, 1000)
//                }
            }.create()
    }

    private fun startCameraWhile(){
        initDlg()
        lifecycleScope.launch {

            var untilTime = 10000
            while( untilTime>0 && !largeBannerLoaded){
                kotlinx.coroutines.delay(1000)
                untilTime-=1000
                runOnUiThread{
                    viewBinding.temp.text = "${untilTime / 1000}"
                }
            }
            startCamera()
        }
    }

    val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                startCameraWhile()
            }
        }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        viewBinding.adView.destroy()
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onPause() {
        super.onPause()
        viewBinding.adView.pause()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.adView.resume()
    }

    private fun startCamera() {
//        MobileAds.initialize(
//            this
//        ) { }
//        // Load the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        goToNextLevel()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
// When using Chinese script library
            val recognizer =
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            val showListener : (Boolean) ->DialogInterface.OnShowListener ={suc->
                DialogInterface.OnShowListener {
                    if(interstitialAd == null/*&& BuildConfig.DEBUG.not()*/){
                        dlgConfirm.PositiveButton.visibility = View.GONE
                    }
                    cameraProvider.unbindAll()
                    dlgConfirm.NegativeButton.isVisible = suc
                    if(suc) {
                        dlgConfirm.NegativeButton.setOnClickListener {

                            if(outputString.isNotEmpty()) {
                                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                val clip =
                                    ClipData.newPlainText(getString(R.string.chprize_1st), outputString)
                                clipboard.setPrimaryClip(clip)
                            }
                            it.isVisible = false
                        }
                    }
                    if (suc)
                        showInterstitial()
                }
            }
            viewBinding.temp.text=""
            // Set up the image analysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            val startAt = System.currentTimeMillis() + 20000// 55000

            imageAnalysis.setAnalyzer(
                cameraExecutor,
                ImageAnalysis.Analyzer { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        // Pass image to an ML Kit Vision API
                        // ...
                        recognizer.process(image)
                            .addOnSuccessListener { iof ->
                                val sb = StringBuilder()
                                val ii = mutableListOf<String>()

                                var (m6str, dyr, dym, dno) = listOf<String?>(null, null, null, null)
                                var (pn, pv, dc) = listOf<String?>(null, null, null)
                                var (ttl, uni) = listOf(0f, 0f)
                                var drawcount = 1
                                val info = mutableListOf<String>()
                                val updateInfo:(str:String, line: Text.Line)->Unit={ t, _->
                                        info.add(t)
                                    runOnUiThread {
                                        viewBinding.temp.text = info.joinToString("\n")
                                    }
                                }
                                Log.d(TAG, "==============\n${iof.text}")
                                iof.textBlocks.sortedWith(compareBy(
                                    { b -> b.boundingBox?.top },
                                    { b -> b.boundingBox?.left })).forEach { firstline ->
                                    if (!firstline.text.contains(anyJockey)) {
                                        Log.d(TAG, ">${firstline.text}<${firstline.boundingBox}")
                                        firstline.lines.sortedWith(compareBy(
                                            { b -> b.boundingBox?.top },
                                            { b -> b.boundingBox?.left }))//.filterNot { it.boundingBox==null }.map{ it.text to it.boundingBox!!}
                                            .forEach {line ->
//                                            val test = it.text//.replace("\\s".toRegex(), "")
                                            if(line.boundingBox!=null) {
                                                val test = line.text
Log.d(TAG, "?????$test????${line.boundingBox}")
                                                when {
                                                    test.contains(m6_sep_num) || N49.contains(test) -> {
                                                        if(!test.startsWith("/"))
                                                            sb.append("&")
                                                        sb.append(test.replace("\\s".toRegex(), ""))
                                                    }

                                                    dyr == null && (test.contains(anyDrawDate)||test.contains(regexDrawDate)) -> {
                                                        regexDrawDate.find(test)?.groupValues?.let{f->
                                                            dym=f[1]
                                                            dyr = "(\\d{2})$".toRegex().find(f[1])?.groupValues?.get(1)
                                                            updateInfo(dym!!, line)
                                                        }
                                                        if(dyr==null)
                                                            "(\\d{2})年(\\d{1,2})月(\\d{1,2})日".toRegex().find(test)?.groupValues?.let { f->
                                                            val now = Calendar.getInstance()
                                                            now.set(Calendar.YEAR, f[1].toInt())
                                                            now.set(Calendar.MONTH, f[2].toInt()-1)
                                                            now.set(Calendar.DAY_OF_MONTH, f[3].toInt())
                                                            dym=dfm.format(now.time).uppercase()
                                                            dyr = "(\\d{2})$".toRegex().find(f[1])?.groupValues?.get(1)
                                                            updateInfo("$dym", line)
                                                        }
                                                    }

                                                    dno == null && test.contains(anyDrawNo) -> {
                                                        regexDrawNo.find(test)?.groupValues?.let {f->
                                                            dno = f[1]
                                                            updateInfo(f[1], line)
                                                        }
                                                    }

                                                    dc == null && test.contains("期|Draw[s]?".toRegex()) -> {
                                                        "^(5|10|20|30)".toRegex()
                                                            .find(test)?.groupValues?.let{f->
                                                                dc = f[1]
                                                                updateInfo(f[1], line)
                                                            }
                                                    }

                                                    m6str == null && test.contains(anyM6) -> {
                                                        m6str = test
                                                        updateInfo("六合彩", line)
                                                    }

                                                    "^[0-9A-F]{5}\\s[0-9A-F]{5}\\s[0-9A-F]{5}".toRegex().find(test)!=null-> {
                                            //                                                    return@forEach
                                                    }
                                                    else -> {
                                                        if (pn == null)
                                                            pn = getDollarType(test)
                                                        if (pv == null && test.contains("$"))
                                                            pv = getDollar(test)
                                                        if (pn != null) {
                                                            Log.d(TAG, " $pn ")
                                                            pv?.toFloatOrNull()?.also {f->
                                                                if (pn == "T")
                                                                    ttl = f
                                                                else if (pn == "@")
                                                                    uni = f
//                                                                if(ii.none { it.startsWith(pn!!) }) {
                                                                    ii.add("$pn:$pv")
                                                                    updateInfo("${pn!!}:$${pv!!}", line)
//                                                                }
                                                                pv = null
                                                                pn = null
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Log.d(TAG, "$dyr $dno $ttl")
                                if (!dyr.isNullOrBlank() && !dno.isNullOrBlank() && ttl >= 10) {
                                    dc?.let { d -> drawcount = d.toInt() }
                                    Log.d(TAG, sb.toString())
                                    val nums = sb.toString().getDrawNumbers()//
                                    if(ttl<uni) {
                                        val value = ttl
                                        ttl =uni
                                        uni = value
                                    }
                                    val valid =
                                        nums.map { (legs, bans) -> validateNumbers(legs, bans) }

                                    Log.d(TAG, "$ii,$nums $uni * ${valid.sum() * drawcount}==$ttl")
                                    if (valid.all { it > 0 } && drawcount * uni * valid.sum() == ttl) {
//                                        val res =
//                                            nums.joinToString("${System.lineSeparator()}/ ") { (legs, bans) ->
//                                                listOf(
//                                                    bans.joinToString(m6_sep_num),
//                                                    legs.joinToString(m6_sep_num)
//                                                )
//                                                    .filter { it.isNotEmpty() }
//                                                    .joinToString(m6_sep_banker)
//                                            }
//                                        val bde = Bundle().also {
//                                            it.putString(TICKETRESULT, res)
//                                            it.putString(
//                                                TICKETSTRING,
//                                                "$dyr#$dno#$ttl#$uni#$drawcount#$$ttl=$$uni*${valid.sum()}${if (drawcount > 1) "*$drawcount" else ""}#$dym"
//                                            )
//                                        }
                                        outputString.clear().appendLine("期數:").appendLine("$dyr/$dno ${if(drawcount>1) "(${drawcount}期)" else ""} $dym")
                                            .appendLine("注項:")
                                            .appendLine(nums.joinToString("${System.lineSeparator()}/ ") { (legs, bans) ->
                                                listOf(
                                                    bans.joinToString(m6_sep_num),
                                                    legs.joinToString(m6_sep_num)
                                                )
                                                    .filter { it.isNotEmpty() }
                                                    .joinToString(m6_sep_banker)
                                            })
                                            .appendLine()
                                            .appendLine("$$ttl = $$uni * ${valid.sum()}${if (drawcount > 1) " * $drawcount" else ""}")
                                        with(dlgConfirm){
                                            if (isShowing) dismiss()
                                            setTitle("掃瞄結果")
                                            setOnShowListener(showListener(true))

                                            setMessage(outputString)
                                            show()
                                        }
                                    }
                                } else {
                                    val timeLeft = System.currentTimeMillis() - startAt

                                    if (timeLeft > 0) {
                                        with(dlgConfirm){
                                            if (isShowing) dismiss()
                                            setTitle("掃瞄逾時")
                                            setOnShowListener(showListener(false))
                                            setMessage("找不到彩票")
                                            show()
                                        }
                                    } else {
                                        "time left \n${timeLeft / -1000}".let {
//                                            viewBinding.imageCaptureButton.text = it
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener {

                            }
                    }

                    // Close the ImageProxy
                    imageProxy.close()
                })

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun getDollar(str: String): String? {
        "[(][$](\\d*)[)]|[$](\\d{2,}[.]\\d{2})".toRegex().find(str)?.groupValues?.let { f ->
            Log.d(TAG, "<$str>$f")

            return f[1].ifEmpty { f[2] }
        }
//        "[(][$](\\d*)[)]$".toRegex().find(str)?.let { f ->
//            return f.groups[1]?.value
//        }
        return null
    }

    private fun getDollarType(str: String): String? {
        return if (str.contains(anyTotalPrice)) "T" else if (str.contains(anyUnitPrice)) "@" else null
    }

    private fun validateNumbers(legs: List<String>, bans: List<String>): Int {
        if (bans.size > 5) {
            println("falses ban>5,${bans.size}")
            return 0
        }
        if (legs.size + bans.size < 7)
            if (legs.size == 6)
                return 1
            else {
                println("falses ${legs.size}, ${bans.size}")
                return 0
            }
        if (legs.isSort() && bans.isSort() && legs.intersect(bans.toSet()).isEmpty()) {
            val x = 6 - bans.size
            val temp = arrayOf(x, legs.size - x)
            temp.sort()
            val rem = (temp[1] + 1..legs.size).toMutableList()
            var divider = 1
            (1..temp[0]).forEach { item -> // this is avoid conversion from Int to Long and return Int, cause the result of leg!, factorial, maybe Long
                val idx = rem.indexOfFirst { it % item == 0 }
                if (idx == -1)
                    divider *= item
                else
                    rem[idx] /= item
            }
            val draw = rem.fold(1) { acc, i -> i * acc }.div(divider)
            Log.d(TAG, "noof draw:$draw")
            return draw
        } else
            return 0
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this, getString(R.string.interstitial_ad_unit_id), adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    // The interstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = ad
//                    nextLevelButton.setEnabled(true)
                    Toast.makeText(this@CameraScanActivity, "onAdLoaded()", Toast.LENGTH_SHORT)
                        .show()
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Called when fullscreen content is dismissed.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            interstitialAd = null
                            Log.d(TAG, "The ad was dismissed.")
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when fullscreen content failed to show.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            interstitialAd = null
                            Log.d(TAG, "The ad failed to show.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when fullscreen content is shown.
                            Log.d(TAG, "The ad was shown.")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.i(TAG, loadAdError.message)
                    interstitialAd = null
//                    nextLevelButton.setEnabled(true)
                    val error: String = String.format(
                        Locale.ENGLISH,
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain,
                        loadAdError.code,
                        loadAdError.message
                    )
                    Log.d(TAG, "onAdFailedToLoad() with error: $error")
                    Toast.makeText(
                        this@CameraScanActivity,
                        "onAdFailedToLoad() with error: $error", Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
    }

    private fun showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (interstitialAd != null) {
            interstitialAd!!.show(this)
        } else {
            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
            goToNextLevel()
        }
    }
    private fun goToNextLevel() {
        // Show the next level and reload the ad to prepare for the level after.
//        levelTextView.text = "Level " + (++currentLevel)
        if (interstitialAd == null) {
            loadInterstitialAd()
        }
    }
    companion object {
        val dfm = SimpleDateFormat("ddMMMyy", Locale.ENGLISH)
        val anyM6 = "[六合彩]|Mark|Six".toRegex(RegexOption.IGNORE_CASE)
        val anyDrawNo = "[期數]|Draw|No".toRegex(RegexOption.IGNORE_CASE)
        val anyUnitPrice = "[注]|Unit|bet".toRegex(RegexOption.IGNORE_CASE)
        val anyTotalPrice = "[總額]|Total".toRegex(RegexOption.IGNORE_CASE)
        val anyDrawDate = "[年月日]".toRegex(RegexOption.IGNORE_CASE)
        val anyJockey = "JOCKEY|CLUB|HONG|KONG".toRegex(RegexOption.IGNORE_CASE)
        val barcode = "[A-F0-9]{7}\\s[A-F0-9]{17}"

        val regexDrawDate = "\\s(\\d{2}[A-Z]{3}\\d{2})$".toRegex()
        val regexDrawNo = "\\s(\\d{3}|[A-Z]{3})$".toRegex()

        val regRight = Regex("JOCKEY|CLUB|HONG|KONG")
        val sam = "香港馬會奬券有限公司".toCharArray().toSet()
        val sam2 = "六合彩".toCharArray().toSet()
        private val N49 = (1..49).map { it.toString() }
        private fun List<String>.isSort(): Boolean {
            if (this.size < 2) return true
            val toint = this.map { N49.indexOf(it) }
//            var idx = -1
//            for (s in this) {
//                val idxof = N49.indexOf(s)
//                if(idxof == -1 || idxof<idx)
//                    return false
//                idx=idxof
//            }
//            return true
            return toint.zipWithNext().all { (a, b) -> a != -1 && a < b }
        }

        private fun String.getDrawNumbers(): List<Pair<List<String>, List<String>>> {
            return this.removePrefix("&")
                    //1+2|/2+3|+3+4+|5+6+7|8
                    //1+2&12+3&+3+4+&5+6+7&8
                .replace("&[+]|[+]&".toRegex(), "")
                .replace("&1","/").replace("[^0-9>+/]".toRegex(), "").split('/').map {//"banker>leg"
                //or "leg"
                val bl = it.split(m6_sep_banker).map { it.split(m6_sep_num) }
                if (bl.size == 2) bl[1] to bl[0] else bl[0] to listOf() // legs to banker
            }
        }

        private const val TAG = "CameraXApp"
    }
}