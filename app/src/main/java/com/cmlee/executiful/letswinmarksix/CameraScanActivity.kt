package com.cmlee.executiful.letswinmarksix

//import androidx.camera.lifecycle.ProcessCameraProvider
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.collection.mutableFloatListOf
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_banker
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_num
import com.cmlee.executiful.letswinmarksix.databinding.ActivityCameraScanBinding
import com.cmlee.executiful.letswinmarksix.databinding.MyAdDialogBinding
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.NegativeButton
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.NeutralButton
import com.cmlee.executiful.letswinmarksix.helper.AlertDialogHelper.PositiveButton
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.bitmapToBase64
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.getHKInstance
import com.cmlee.executiful.letswinmarksix.model.tickets.Ticket
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random


class CameraScanActivity : AppCompatActivity() {
    private var interstitialAd: InterstitialAd? = null
    private lateinit var viewBinding: ActivityCameraScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var adView: AdView
    private var outputString = StringBuilder()
    private var largeBannerLoaded = false
    private var bpcb = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    private lateinit var dlgConfirm: AlertDialog
    var untilTime: Long = 10000
    private val KEY_UNTIL = "UNTIL"
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_UNTIL, untilTime)
    }
    @SuppressLint("SuspiciousIndentation", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestedOrientation= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        viewBinding = ActivityCameraScanBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
//        setContentView(R.layout.activity_camera_scan)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        viewBinding.temp.setTextColor(
            AppCompatResources.getColorStateList(
                this, when (Random.nextInt(3)) {
                    0 -> R.color.hkjc_red
                    1 -> R.color.hkjc_green
                    else -> R.color.hkjc_blue
                }
            )
        )
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adView = AdView(this)
        adView.adUnitId =
            getString(if (BuildConfig.DEBUG) R.string.banner_ad_unit_id else R.string.admob_m6_lottery)
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE)
        viewBinding.adViewContainer.removeAllViews()
        viewBinding.adViewContainer.addView(adView)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                largeBannerLoaded = true
            }
        }
        adView.loadAd(adRequest)
        savedInstanceState?.let { state->
            untilTime = state.getLong(KEY_UNTIL, 10000)
        }
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

    private fun initDlg() {
//        if(dlgConfirm!=null) return
        dlgConfirm = MaterialAlertDialogBuilder(this, R.style.OCR_Dialog).setCancelable(false)
            .setTitle(R.string.title_scanticket)
            .setNeutralButton("退出") { d, _ ->
                Handler(mainLooper).postDelayed({
                    d.dismiss()
                    finish()
                }, if (interstitialAd == null) 2000 else 0)
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

    private fun startCameraWhile() {
        initDlg()
        lifecycleScope.launch {

            val indicate = "0123456789"
            while (untilTime > 0 && !largeBannerLoaded) {
                kotlinx.coroutines.delay(1000)
                untilTime -= 1000
                runOnUiThread {
                    viewBinding.temp.text = indicate.substring(0, (untilTime / 1000).toInt())
                }
            }
            viewBinding.idMyAppImage.isVisible = false
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
        adView.destroy()
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
//        MobileAds.initialize(
//            this
//        ) { }
//        // Load the InterstitialAd and set the adUnitId (defined in values/strings.xml).
        goToNextLevel()
        val gson = GsonBuilder().create()

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
                    it.surfaceProvider = viewBinding.viewFinder.surfaceProvider
                }

            val timeoutRef = System.currentTimeMillis() + 30000// 55000

            val showListener: (Boolean) -> DialogInterface.OnShowListener = { suc ->
                DialogInterface.OnShowListener {
                    if (interstitialAd == null/*&& BuildConfig.DEBUG.not()*/) {
                        dlgConfirm.PositiveButton.isVisible = false
                    }
                    cameraProvider.unbindAll()
//                    dlgConfirm.NegativeButton.isVisible = suc
                    dlgConfirm.NegativeButton.isEnabled = suc
                    if (suc) {
                        val temproot = MyAdDialogBinding.inflate(layoutInflater)
                        val butlist = listOf(temproot.button2, temproot.button3)
                        butlist.forEach { b -> b.isVisible = false }
                        val tempdialog =
                            MaterialAlertDialogBuilder(this, R.style.TEMP_Ads_Dialog).setView(
                                temproot.root
                            ).setCancelable(false).show()
                        dlgConfirm.NeutralButton.isVisible = false
                        lifecycleScope.launch {
                            var time = 15
                            val no_interstitial = (interstitialAd == null)
                            while (time-- > 0) {
                                kotlinx.coroutines.delay(if (no_interstitial) 800 else 350)
                            }
                            runOnUiThread {
                                dlgConfirm.NeutralButton.isVisible = true

                                butlist[if (Random.nextInt() % 2 == 0) 0 else 1].apply {
                                    isVisible = true
                                    bringToFront()
                                    setOnClickListener { tempdialog.dismiss() }
                                }
                                if (no_interstitial)
                                    tempdialog.setCancelable(true)
                                else
                                    tempdialog.dismiss()
                            }
                        }

                        lifecycleScope.launch {
                            val timeDiff = timeoutRef - System.currentTimeMillis()
                            if (timeDiff > 0)
                                kotlinx.coroutines.delay(timeDiff.coerceAtMost(2000))
                            showInterstitial()
                        }
                        dlgConfirm.NegativeButton.setOnClickListener {

                            if (outputString.isNotEmpty()) {
                                val clipboard =
                                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                val clip =
                                    ClipData.newPlainText(
                                        getString(R.string.chprize_1st),
                                        outputString
                                    )
                                clipboard.setPrimaryClip(clip)
                            }
                            it.isEnabled = false
                        }
                    }
                }
            }
            viewBinding.temp.text = ""
            // Set up the image analysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                cameraExecutor,
                ImageAnalysis.Analyzer { imageProxy ->
//                    lifecycleScope.launch{
//                        cacheDir.listFiles { ff->ff.name.startsWith("bm") }?.first()?.delete()
//                        createTempFile("bm").writeText(imageString)
//                    }
                    val mediaImage = imageProxy.image

                    if (mediaImage == null) {
                        Log.d(TAG, "mediaImage is null?")
                    } else {
                        val imageString = imageProxy.toBitmap()

                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                        // Pass image to an ML Kit Vision API
                        // ...
                        recognizer.process(image)
                            .addOnSuccessListener { iof ->
                                val sb = StringBuilder()
                                val ii = mutableListOf<String>()

                                var (m6str, dyr, dym, dno) = listOf<String?>(null, null, null, null)
                                var (pn, pv, dc) = listOf<String?>(null, null, null)
                                var (uni, ttl) = listOf(0f, 0f)
                                val dollars = mutableFloatListOf()
                                var drawcount = 1
                                val info = mutableListOf<String>()
//                                val updateInfo:(str:String, line: Text.Line)->Unit={ t, _->
//                                        info.add(t)
//                                }
                                Log.d(TAG, "==============\n${iof.text}")
                                iof.textBlocks.sortedWith(
                                    compareBy(
                                        { b -> b.boundingBox?.top },
                                        { b -> b.boundingBox?.left })
                                ).forEach { firstline ->
                                    if (!firstline.text.contains(anyJockey)) {
                                        Log.d(TAG2, ">${firstline.text}<${firstline.boundingBox}")
                                        firstline.lines.sortedWith(
                                            compareBy(
                                                { b -> b.boundingBox?.top },
                                                { b -> b.boundingBox?.left })
                                        )//.filterNot { it.boundingBox==null }.map{ it.text to it.boundingBox!!}
                                            .forEach { line ->
                                                if (line.boundingBox != null) {
//                                                    Log.d(TAG2, "/${line.text}/")
                                                    val test = line.text
                                                    when {
                                                        test.contains(m6_sep_num) || N49.contains(
                                                            test
                                                        ) -> {
                                                            if (!test.startsWith("/"))
                                                                sb.append("&")
                                                            sb.append(
                                                                test.replace(
                                                                    "\\s".toRegex(),
                                                                    ""
                                                                )
                                                            )
                                                        }

                                                        dyr == null && (test.contains(anyDrawDate) || test.contains(
                                                            regexDrawDate
                                                        )) -> {
                                                            regexDrawDate.find(test)?.groupValues?.let { f ->
                                                                dym = f[1]
                                                                dyr = f[1].takeLast(2)
                                                                info.add(f[1])
                                                            }
                                                            if (dyr == null)
                                                                "(\\d{2})年(\\d{1,2})月(\\d{1,2})日".toRegex()
                                                                    .find(test)?.groupValues?.let { f ->
                                                                    val now = getHKInstance()
                                                                    now.set(
                                                                        Calendar.YEAR,
                                                                        f[1].toInt()
                                                                    )
                                                                    now.set(
                                                                        Calendar.MONTH,
                                                                        f[2].toInt() - 1
                                                                    )
                                                                    now.set(
                                                                        Calendar.DAY_OF_MONTH,
                                                                        f[3].toInt()
                                                                    )
                                                                    dym = dfm.format(now.time)
                                                                        .uppercase()
                                                                    dyr = f[1]
                                                                    info.add(
                                                                        dfm.format(now.time)
                                                                            .uppercase()
                                                                    )
                                                                }
                                                        }

                                                        dno == null && test.contains(anyDrawNo) &&test.contains(anyDrawCount).not() -> {
                                                            regexDrawNo.find(test)?.groupValues?.let { f ->
                                                                dno = f[1]
                                                                info.add(f[1])
                                                            }
                                                        }

                                                        dc == null && test.contains("期|Draw[s]?".toRegex())&&test.contains(anyDrawCount) -> {
                                                            anyDrawCount.find(test)?.groupValues?.let { f ->
                                                                    dc = f[1]
                                                                    info.add(f[1])
                                                                }
                                                        }

                                                        m6str == null && test.contains(anyM6) -> {
                                                            m6str = test
                                                            info.add("六合彩")
                                                        }

                                                        "^[0-9A-F]{5}\\s[0-9A-F]{5}\\s".toRegex()
                                                            .find(test) != null -> {
                                                            //                                                    return@forEach
                                                        }
                                                        test.contains("$") ->{
                                                            getDollar(test)?.toFloatOrNull()?.let{f->
                                                                if(f>0&&dollars.contains(f).not()){
                                                                    info.add(test)
                                                                    dollars.add(f)
                                                                    dollars.sort()
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                                runOnUiThread {
                                    viewBinding.temp.text = info.joinToString("\n")
                                }
                                Log.d(TAG, "year:$dyr, id: $dno, total: ${dollars.joinToString()}, count$dc")
                                if (!dyr.isNullOrBlank() && !dno.isNullOrBlank() &&dollars.any{a->a>=10}) {
                                    dc?.toIntOrNull()?.let { d -> drawcount = d }
                                    uni = dollars[0]
                                    ttl = if(dollars.size==1) uni else dollars[1]
                                    Log.d(TAG, sb.toString())
                                    val nums = sb.toString().getDrawNumbers()

                                    val valid = nums.map { (legs, bans) -> validateNumbers(legs, bans) }

                                    Log.d(TAG, "$ii,$nums $uni * ${valid.sum() * drawcount}==$ttl")
                                    if (valid.all { it > 0 } && drawcount * uni * valid.sum() == ttl) {
                                        val mtx=Matrix()
                                        mtx.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat(), imageString.width/2f, imageString.height/2f)
                                        val img = bitmapToBase64(Bitmap.createBitmap(imageString, 0,0, imageString.width, imageString.height, mtx, true))
                                        val ticket = Ticket(drawYear = dyr, buyDate = dym?:"", drawNo = dno, drawTotal = ttl, drawUnit = uni,
                                            drawItemNumbers = nums, ocr = img, draws = drawcount)
                                        outputString.clear().appendLine("期數:")
                                            .appendLine("${ticket.drawID} ${if (ticket.draws > 1) "(${ticket.draws}期)" else ""} $dym")
                                            .appendLine("注項:")
                                            .appendLine(ticket.numbersString)
                                            .appendLine()
                                            .appendLine("$${ticket.drawTotal} = $${ticket.drawUnit} * ${valid.sum()}${if (drawcount > 1) " * $drawcount" else ""}")
//                                        with(getSharedPreferences(OCR_TICKETS, MODE_PRIVATE)) {
//                                            try {
//                                                if (all.isEmpty()||!all.entries.parallelStream().anyMatch { (k,v)->
//                                                        val r = gson.fromJson(
//                                                            v as String,
//                                                            Ticket::class.java
//                                                        )
//                                                        (r.drawID == ticket.drawID && r.drawItemNumbers == ticket.drawItemNumbers&&r.draws==ticket.draws).also{
//                                                            if(it){outputString.appendLine("(已有紀錄:${
//                                                                Date(
//                                                                    k.toLong()
//                                                                )
//                                                            })")}
//                                                        }
//                                                    }) {
//                                                    edit {
//                                                        putString(
//                                                            getCurrentTimeInTimezone(),
//                                                            gson.toJson(ticket)
//                                                        )
//                                                    }
//                                                }
//                                            } catch (e: Exception) {
//                                                Log.d(TAG, "ticket exception ${e.message?:e.stackTraceToString()}")
//                                            }
//                                        }
                                        with(dlgConfirm) {
                                            if (isShowing) dismiss()
                                            setTitle("掃瞄結果")
                                            setOnShowListener(showListener(true))

                                            setMessage(outputString)
                                            show()
                                        }
                                    }
                                } else {
                                    val timeLeft = System.currentTimeMillis() - timeoutRef

                                    if (timeLeft > 0) {
                                        with(dlgConfirm) {
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_0 -> {
                largeBannerLoaded = true
                untilTime = 10
                true
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun getDollar(str: String): String? {
        "[(][$](\\d*)[)]|[$](\\d*[.]\\d{2})".toRegex().find(str)?.groupValues?.let { f ->
            Log.d(TAG, "<$str>$f")

            return f[1].ifEmpty { f[2] }
        }
        return null
    }

    private fun getDollarType(str: String): String? {
        return if (str.contains(anyTotalPrice)) "T" else if (str.contains(anyUnitPrice)) "@" else null
    }

    private fun validateNumbers(legs: List<String>, bans: List<String>): Int {
        if (bans.size > 5 || !(bans.isSort())) {
            println("falses ban>5,${bans.size}")
            return 0
        }
        if(!legs.isSort())
            return 0
        if (legs.size + bans.size < 7)
            if (legs.size == 6)
                return 1
            else {
                println("falses ${legs.size}, ${bans.size}")
                return 0
            }
        if (legs.intersect(bans.toSet()).isEmpty()) {
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
            this,
            getString(if (BuildConfig.DEBUG) R.string.interstitial_ad_unit_id else R.string.admob_m6_ocr),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    // The interstitialAd reference will be null until
                    // an ad is loaded.
                    interstitialAd = ad
//                    nextLevelButton.setEnabled(true)
//                    Toast.makeText(this@CameraScanActivity, "onAdLoaded()", Toast.LENGTH_SHORT)
//                        .show()
                    Log.d(TAG, "onAdLoaded() interstitial")
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
//                    Toast.makeText(
//                        this@CameraScanActivity,
//                        "onAdFailedToLoad() with error: $error", Toast.LENGTH_SHORT
//                    )
//                        .show()
                }
            })
    }

    private fun showInterstitial() {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (interstitialAd != null) {
            interstitialAd!!.show(this)
        } else {
//            Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "interstitial Ad did not load")
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
        const val OCR_TICKETS = "OCR Readings"
        val dfm = SimpleDateFormat("ddMMMyy", Locale.ENGLISH)
        val anyM6 = "[六合彩]|Mark|Six".toRegex(RegexOption.IGNORE_CASE)
        val anyDrawNo = "[期數]|Draw|No".toRegex(RegexOption.IGNORE_CASE)
        val anyUnitPrice = "注|Unit|bet".toRegex(RegexOption.IGNORE_CASE)
        val anyTotalPrice = "[總額]|Total".toRegex(RegexOption.IGNORE_CASE)
        val anyDrawDate = "[年月日]".toRegex(RegexOption.IGNORE_CASE)
        val anyDrawCount = "^([5123][0]{0,1})[^0-9]".toRegex()
        val anyJockey = "JOCKEY|CLUB|HONG|KONG".toRegex(RegexOption.IGNORE_CASE)
        val barcode = "[A-F0-9]{7}\\s[A-F0-9]{17}"

        val regexDrawDate = "\\s(\\d{2}[A-Z]{3}\\d{2})$".toRegex()
        val regexDrawNo = "\\s(\\d{3}|[A-Z]{3})$".toRegex()

        val regRight = Regex("JOCKEY|CLUB|HONG|KONG")
        val sam = "香港馬會奬券有限公司".toCharArray().toSet()
        val sam2 = "六合彩".toCharArray().toSet()
        private val N49 = (1..49).map { it.toString() }
        private fun List<String>.isSort(): Boolean {
            if(isEmpty()) return true
            val ids = map{N49.indexOf(it)}
            if(ids.any{it==-1}) return false
            if(size<2) return true
            val ret = !ids.zipWithNext().any { (a, b) -> a >= b}
            Log.d(TAG, "ret :$ret ${first()}")
            return ret
        }

        private fun String.getDrawNumbers(): List<Pair<List<String>, List<String>>> {
            return this.removePrefix("&")
                //1+2|/2+3|+3+4+|5+6+7|8
                //1+2&12+3&+3+4+&5+6+7&8
                .replace("\\s".toRegex(), "")
                .replace("&[+]|[+]&".toRegex(), "")
                .replace("&1", "/").replace("[^0-9>+/]".toRegex(), "").split('/')
                .map {//"banker>leg"
                    //or "leg"
                    val bl = it.split(m6_sep_banker).map { r->r.split(m6_sep_num) }
                    if (bl.size == 2) bl[1] to bl[0] else bl[0] to listOf() // legs to banker
                }
        }

        private const val TAG = "CameraXApp"
        private const val TAG2 = "CameraXKApp"
    }
}