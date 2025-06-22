package com.cmlee.executiful.letswinmarksix

//import androidx.camera.lifecycle.ProcessCameraProvider
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_banker
import com.cmlee.executiful.letswinmarksix.MainActivity.Companion.m6_sep_num
import com.cmlee.executiful.letswinmarksix.databinding.ActivityCameraScanBinding
import com.cmlee.executiful.letswinmarksix.helper.BannerAppCompatActivity
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.TICKETRESULT
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.TICKETSTRING
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraScanActivity : BannerAppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraScanBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    override val adUnitStringId: Int = R.string.admob_m6_lottery
    override fun onAdLoaded() {
//        TODO("Not yet implemented")
    }

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


        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()
        adContainerView = viewBinding.adViewContainer
    }

//    override fun onPause() {
//        super.onPause()
//        finish()
//    }
    private fun takePhoto() {}

    private fun captureVideo() {}

    //    private fun startCamera() {}
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
//                finish()
            } else {
                startCamera()
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
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
//        getSharedPreferences("TESTNUM", MODE_PRIVATE).all.clear()
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


            // Set up the image analysis use case
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            val startAt = System.currentTimeMillis() + 55000

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
                                val intent = Intent()
                                val sb = StringBuilder()
                                val ii = mutableListOf<String>()
                                var (m6str, dyr, dym, dno) = listOf<String?>(null, null, null, null)
                                var (pn, pv, dc) = listOf<String?>(null, null, null)
                                var (ttl, uni) = listOf(0f, 0f)
                                var drawcount = 1
                                var info = StringBuilder()
                                var updateinfo:(str:String)->Unit={t->
//                                    if(t!=null) {
                                        info.appendLine(t)
                                        viewBinding.videoCaptureButton.text = info.toString()
//                                    }
                                }
                                viewBinding.videoCaptureButton.text = "?"
                                iof.textBlocks/*.sortedWith(compareBy(
                                    { b -> b.boundingBox?.top },
                                    { b -> b.boundingBox?.left }))*/.forEach { firstline ->
                                    if (!firstline.text.contains(anyJockey)) {
                                        Log.d(TAG, ">${firstline.text}<")
                                        firstline.lines.sortedWith(compareBy(
                                            { b -> b.boundingBox?.top },
                                            { b -> b.boundingBox?.left })).forEach {
                                            val test = it.text//.replace("\\s".toRegex(), "")

                                            when {
                                                test.contains(m6_sep_num) || test.isDigitsOnly() -> {
                                                    if(!test.startsWith("/"))
                                                        sb.append("&")
                                                    sb.append(test.replace("\\s".toRegex(), ""))
                                                }

                                                dyr == null && (test.contains(anyDrawDate)||test.contains(regexDrawDate)) -> {
                                                    dym =
                                                        regexDrawDate.find(test)?.groupValues?.get(1)
                                                    dym?.let { it1 ->
                                                        dyr = "(\\d{2})$".toRegex()
                                                            .find(it1)?.groupValues?.get(1)
                                                        updateinfo(dym!!)
                                                    }
                                                }

                                                dno == null && test.contains(anyDrawNo) -> {
                                                    dno =
                                                        regexDrawNo.find(test)?.groupValues?.get(1)
                                                    updateinfo(dno!!)
                                                }

                                                dc == null && test.contains("期|Draw[s]?".toRegex()) -> {
                                                    dc = "^(5|10|20|30)".toRegex()
                                                        .find(test)?.groupValues?.get(1)
                                                    updateinfo(dc!!)
                                                }

                                                m6str == null && test.contains(anyM6) -> {
                                                    m6str = it.text
                                                    updateinfo("六合彩")
                                                }
                                                "^[0-9A-F]{5}\\s[0-9A-F]{5}\\s[0-9A-F]{5}".toRegex().find(it.text)!=null->
                                                    return@forEach
                                                else -> {
                                                    if (pn == null)
                                                        pn = getDollarType(test)
                                                    if (pv == null && test.contains("$"))
                                                        pv = getDollar(test)
                                                }
                                            }
                                        }
                                        if (pn != null) {
                                            pv?.toFloatOrNull()?.also {
                                                if (pn == "T")
                                                    ttl = it
                                                else if (pn == "@")
                                                    uni = it
                                                ii.add("$pn:$pv")
                                                updateinfo("${pn!!}:$${pv!!}")
                                                pv = null
                                                pn = null
                                            }
                                        }
                                    }
                                }
                                dc?.let { d -> drawcount = d.toInt() }
                                Log.d(TAG, "$dyr $dno $ttl * $drawcount")
                                if (!dyr.isNullOrBlank() && !dno.isNullOrBlank() && ttl >= 10) {
                                    Log.d(TAG, sb.toString())
                                    val nums = sb.toString().removePrefix("&").replace("&[+]|[+]&".toRegex(), "").replace("&1","/").getDrawNumbers()//

                                    val valid =
                                        nums.map { (legs, bans) -> validateNumbers(legs, bans) }

                                    Log.d(TAG, "$ii,$nums $uni * ${valid.sum() * drawcount}==$ttl")
                                    if (valid.all { it > 0 } && drawcount * uni * valid.sum() == ttl) {
                                        val res =
                                            nums.joinToString("${System.lineSeparator()}/ ") { (legs, bans) ->
                                                listOf(
                                                    bans.joinToString(m6_sep_num),
                                                    legs.joinToString(m6_sep_num)
                                                )
                                                    .filter { it.isNotEmpty() }
                                                    .joinToString(m6_sep_banker)
                                            }
                                        val bde = Bundle().also {
                                            it.putString(TICKETRESULT, res)
                                            it.putString(
                                                TICKETSTRING,
                                                "$dyr#$dno#$ttl#$uni#$drawcount#$$ttl=$$uni*${valid.sum()}${if (drawcount > 1) "*$drawcount" else ""}#$dym"
                                            )
                                        }
                                        intent.putExtras(bde)
                                        setResult(RESULT_OK, intent)
                                        finish() //onBackPressed()
                                    }
                                } else {
                                    val timeLeft = System.currentTimeMillis() - startAt
                                    "time left \n${timeLeft / -1000}".let {
                                        viewBinding.imageCaptureButton.text = it
                                    }
                                    if (timeLeft > 0) {
                                        intent.putExtras(Bundle().also{it.putString(TICKETRESULT,"掃瞄逾時")})
                                        setResult(RESULT_CANCELED, intent)
                                        finish()
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
        Log.d(TAG, "<$str>")
        "[$](\\d*[.]+\\d*)$".toRegex().find(str)?.let { f ->
            return f.groups[1]?.value
        }
        "[(][$](\\d*)[)]$".toRegex().find(str)?.let { f ->
            return f.groups[1]?.value
        }
        return null
    }

    private fun getDollarType(str: String): String? {
        return if (str.contains(anyTotalPrice)) "T" else if (str.contains(anyUnitPrice)) "@" else null
    }

    private fun getFuns(): MutableList<(String) -> String> {
        return mutableListOf<(String) -> String>(
            {
                if (it.contains(anyM6)) it else ""
            }, {
                "(\\d{3}|[A-Z]{3})$".toRegex().find(it)?.let { f -> f.groups[0]?.value } ?: ""
            }/*,{
                if(it.contains(unitprice)){
                    dollaru.find(it)?.let { f ->
//                        sb.appendLine(f.groups[1]?.value)
                        return@mutableListOf true
                    }
//                    sbu.appendLine("??")
                }
                false
            },{
                if(it.contains(totalprice)){
                    dollarp.find(it)?.let {f->
//                        sb.appendLine(f.groups[1]?.value)
                        return@mutableListOf true
                    }
//                    sbu.appendLine("??")
                }
                false
            }*/, {
                "(\\d{2})年".toRegex().find(it)?.let { f -> f.groups[1]?.value } ?: ""
            }, {
                if (it.contains(anyJockey)) it else ""
            }, {
                if (it.contains(barcode)) it else ""
            })
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

    companion object {
        val anyM6 = "[六合彩]|Mark|Six".toRegex(RegexOption.IGNORE_CASE)
        val anyDrawNo = "[期數]|Draw|No".toRegex(RegexOption.IGNORE_CASE)
        val anyUnitPrice = "[注]|Unit|bet".toRegex(RegexOption.IGNORE_CASE)
        val anyTotalPrice = "[總額]|Total".toRegex(RegexOption.IGNORE_CASE)
        val anyDrawDate = "[年月日]".toRegex(RegexOption.IGNORE_CASE)
        val anyJockey = "JOCKEY|CLUB|HONG|KONG".toRegex(RegexOption.IGNORE_CASE)
        val barcode = "[A-F0-9]{7}\\s[A-F0-9]{17}"

        val regexDrawDate = "(\\d{2}[A-Z]{3}\\d{2})".toRegex()
        val regexDrawNo = "(\\d{3}|[A-Z]{3})$".toRegex()

        val dollarp = "(\\$\\d*[.]+\\d+)".toRegex()
        val dollaru = "[(](\\$\\d*)[)]".toRegex()
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
            return this.replace("[^0-9>+/]".toRegex(), "").split('/').map {//"banker>leg"
                //or "leg"
                val bl = it.split(m6_sep_banker).map { it.split(m6_sep_num) }
                if (bl.size == 2) bl[1] to bl[0] else bl[0] to listOf() // legs to banker
            }
        }

        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}