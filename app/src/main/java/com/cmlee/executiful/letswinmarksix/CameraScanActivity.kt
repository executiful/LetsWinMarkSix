package com.cmlee.executiful.letswinmarksix

//import androidx.camera.lifecycle.ProcessCameraProvider
import android.R
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


class CameraScanActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCameraScanBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_camera_scan)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        viewBinding = ActivityCameraScanBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
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
//                            print(it.text)
//                                val numbers = mutableListOf<MutableList<String>>()
                                val sb = StringBuilder()
//                                var current = mutableListOf<String>()
//                                numbers.add(current)
                                val sorted = it.textBlocks.sortedWith(compareBy ({it.boundingBox?.top}, {it.boundingBox?.left} ))
                                sorted.forEach {
                                    it.lines.sortedWith(compareBy ({it.boundingBox?.top}, {it.boundingBox?.left} )).forEach{
                                        var test = it.text.replace ( "\\s".toRegex() , "")
                                        if(test.contains('+')||test.isDigitsOnly()) {
                                            sb.append(test)
/*                                            val nl = test.startsWith('/')
                                            val line = test.trimStart('/')
                                            if (nl) current = mutableListOf()
                                            val digits = line.trim('+').split('+')
                                                    .toMutableList()
                                            println("line of ... ${it.text}")
                                            if (digits.isNotEmpty() && digits.all { it.isDigitsOnly() }) {
                                                if (nl) {
                                                    current = mutableListOf()
                                                    numbers.add(current)
                                                }
                                                current.addAll(digits)
                                                println("line of ... yes")
                                            } else println("line of ... no")*/
                                        }
                                    }

                                }
//                                if(numbers.all{it.isSort()})
                                val nums = sb.toString().split('/').map{
                                    val bl = it.split('<')
                                     when(bl.count()){
                                        2->bl[0] to bl[1]
                                        1->bl[0] to null
                                        else-> null
                                    }
                                }
                                Toast.makeText(this,
                                    sb, Toast.LENGTH_SHORT ).show()
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
    companion object {
        private val N49 = (1..49).map{it.toString()}
        private fun List<String>.isSort():Boolean{
            if(this.count()<6) return false
            val toint = this.map{ N49.indexOf(it) }

            return toint.none{it==-1}&& toint.zipWithNext().all {(a,b)-> a<b}
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