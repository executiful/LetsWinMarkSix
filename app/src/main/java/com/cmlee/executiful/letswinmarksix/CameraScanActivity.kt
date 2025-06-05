package com.cmlee.executiful.letswinmarksix

//import androidx.camera.lifecycle.ProcessCameraProvider
import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.cmlee.executiful.letswinmarksix.databinding.ActivityCameraScanBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.core.content.edit
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.TICKETRESULT
import com.cmlee.executiful.letswinmarksix.helper.CommonObject.TICKETSTRING


class CameraScanActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraScanBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
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
    }
    private fun takePhoto() {}

    private fun captureVideo() {}

//    private fun startCamera() {}
    val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
                finish()
            } else {
                startCamera()
            }
        }
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
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
            val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
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

            imageAnalysis.setAnalyzer(
                cameraExecutor,
                ImageAnalysis.Analyzer { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        // Pass image to an ML Kit Vision API
                        // ...
                        recognizer.process(image)
                            .addOnSuccessListener {
                                val sb = StringBuilder()
                                val chkfor = mutableListOf(m6, totalprice, unitprice)
                                val dd = StringBuilder()
                                val ii = StringBuilder()
                                var dyr :String? = null
                                var dno :String? = null
//                                val funs = getFuns()
                                val sorted = it.textBlocks.sortedWith(compareBy ({b->b.boundingBox?.top}, {b->b.boundingBox?.left} ))
                                sorted.forEach { firstline->
                                    firstline.lines.sortedWith(compareBy ({b->b.boundingBox?.top}, {b->b.boundingBox?.left} )).forEach{
                                        val test = it.text.replace ( "\\s".toRegex() , "")
                                        if(test.contains('+')||test.isDigitsOnly()) {
                                            sb.append(test)
                                        } else if (test.contains("$")) {
                                            dd.append(test)
                                        } else if (dyr==null &&test.contains(drawdate)) {
                                            "(\\d{2})年".toRegex().find(test)?.let {f->
                                                dyr = f.groups[1]?.value
                                            }
                                        } else if (dno==null &&test.contains(drawno)){
                                            "(\\d{3}|[A-Z]{3})$".toRegex().find( test)?.let{f->
                                                dno = f.groups[1]?.value
                                            }
                                        } else {
                                            for (s in chkfor) {
                                                if (test.contains(s)&&chkfor.remove(s)) {
                                                    ii.appendLine(test)
                                                    break
                                                }
                                            }
                                        }
                                    }

                                }
//                                if(numbers.all{it.isSort()})
                                val nums = sb.toString().getDrawNumbers().filter {(f,s)-> validateNumbers(f,s) }
//                                if( nums.any{ (f,s)->
//                                    !validateNumbers(f,s)
//
//                                    }){
//                                    getSharedPreferences("TESTNUM", MODE_PRIVATE).edit {
//                                        putString(
//                                            System.currentTimeMillis().toString(),
//                                            sb.toString()
//                                        )
//                                    }
//                                } else
//                                    getSharedPreferences("SUCCESSNUM", MODE_PRIVATE).edit{
//                                        putString(System.currentTimeMillis().toString(), sb.toString())
//                                    }

                                if(nums.isNotEmpty()&&!dyr.isNullOrBlank()&&!dno.isNullOrBlank()){
                                    val res =nums.joinToString { (f, s) ->
                                        f.joinToString("_") + if (s.isNotEmpty()) ">" + s.joinToString("+") else ""
                                    }
                                    val intent = Intent()
                                    val bde = Bundle().also {
                                        it.putString(TICKETRESULT, res)
                                        it.putString(TICKETSTRING, "$dyr#$dno")
                                    }
                                    intent.putExtras(bde)
                                    setResult(RESULT_OK, intent)

                                    finish() //onBackPressed()
//                                    viewBinding.imageCaptureButton.text = dd
//                                    viewBinding.videoCaptureButton.text = ii
//                                    Toast.makeText(
//                                        this,
//                                        res, Toast.LENGTH_SHORT
//                                    ).show()
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
                    this, cameraSelector, preview, imageAnalysis)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private fun getFuns(): MutableList<(String) -> Boolean>{
        return mutableListOf<(String)->Boolean>(
            {
                if (it.contains(m6)){

                    true
                }else
                    false
            },{
                if(it.contains(drawno)){
                    drawnnn.find(it)?.let {f->
                        return@mutableListOf true
                    }
//                    sbu.appendLine("??")
                }
                false
            },{
                if(it.contains(number)){
                    true
                } else {
                    false
                }
            },{
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
            },{
                it.contains(drawdate)
            },{
                it.contains(jockey)
                false
            },{
                it.contains(barcode)
                false
            })
    }
    private fun validateNumbers(legs:List<String>, bans:List<String>):Boolean{
        if (bans.size > 5) {
            println("falses ban>5,${bans.size}")
            return false
        }
        if (legs.size + bans.size < 7 &&legs.size != 6) {
            println("falses ${legs.size}, ${bans.size}")
            return false
        }
        return (legs.isSort() && bans.isSort() && legs.intersect(bans.toSet()).isEmpty())
    }
    companion object {
        val m6 = "[六合彩]|Mark|Six".toRegex(RegexOption.IGNORE_CASE)
        val drawno = "[期數]|Draw|No".toRegex(RegexOption.IGNORE_CASE)
        val number = "[+/]".toRegex()
        val unitprice = "[注]|Unit|bet".toRegex(RegexOption.IGNORE_CASE)
        val totalprice = "[總額]|Total".toRegex(RegexOption.IGNORE_CASE)
        val drawdate = "[年月日]".toRegex(RegexOption.IGNORE_CASE)
        val jockey = "JOCKEY|CLUB|HONG|KONG".toRegex(RegexOption.IGNORE_CASE)
        val barcode = "[A-F0-9]{7}\\s[A-F0-9]{17}"

        val dollarp = "(\\$\\d*[.]+\\d+)".toRegex()
        val dollaru = "[(](\\$\\d*)[)]".toRegex()
        val drawnnn = "(\\d{3})".toRegex()
        val regRight = Regex("JOCKEY|CLUB|HONG|KONG")
        val sam = "香港馬會奬券有限公司".toCharArray().toSet()
        val sam2 = "六合彩".toCharArray().toSet()
        private val N49 = (1..49).map{it.toString()}
        private fun List<String>.isSort():Boolean{
            if(this.size<2) return true
            val toint = this.map{ N49.indexOf(it) }
//            var idx = -1
//            for (s in this) {
//                val idxof = N49.indexOf(s)
//                if(idxof == -1 || idxof<idx)
//                    return false
//                idx=idxof
//            }
//            return true
            return toint.zipWithNext().all {(a,b)-> a!=-1&& a<b}
        }
        private fun String.toNumbers():List<Int>{
            return this.split('+').map { N49.indexOf(it) }.filterNot{it==-1}
        }
        private fun String.getDrawNumbers():List<Pair<List<String>, List<String>>>{
           return this.split('/').map {
                val bl = it.split('>')
                when (bl.count()) {
                    2 -> bl[0] to bl[1]
                    1 -> bl[0] to ""
                    else -> "" to ""
                }
            }.map{(f,s)-> f.split('+') to s.split('+')}
        }
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}