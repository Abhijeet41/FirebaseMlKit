package com.abhi41.firebasemlkit.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.abhi41.firebasemlkit.databinding.ActivityQrCodeScannerBinding
import com.abhi41.firebasemlkit.util.BottomDialog
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRCodeScannerScreen : AppCompatActivity() {
    private lateinit var binding: ActivityQrCodeScannerBinding
    private lateinit var cameraProviderFuature: ListenableFuture<ProcessCameraProvider>
    lateinit var cameraExecutor: ExecutorService
    lateinit var analyzer: MyAnalyzer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window.setFlags(1024, 1024)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuature = ProcessCameraProvider.getInstance(this)

        analyzer = MyAnalyzer(supportFragmentManager, applicationContext)

        // Camera Provider Future

        cameraProviderFuature.addListener(Runnable {
            try {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    )
                    != (PackageManager.PERMISSION_GRANTED)
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA), 101
                    )
                } else {
                    val processCameraProvider = cameraProviderFuature.get()
                    bindPreview(processCameraProvider)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(applicationContext))

    }

    private fun bindPreview(processCameraProvider: ProcessCameraProvider?) {
        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder().requireLensFacing(
            LENS_FACING_BACK
        ).build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val imageCapture = ImageCapture.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer)

        processCameraProvider?.unbindAll()
        processCameraProvider?.bindToLifecycle(
            this, cameraSelector, preview,
            imageCapture, imageAnalysis
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var processCameraProvider: ProcessCameraProvider? = null
        if (requestCode == 101 && grantResults.size > 0) {
            try {
                processCameraProvider = cameraProviderFuature.get()
                bindPreview(processCameraProvider)
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}

class MyAnalyzer(
    supportFragmentManager: FragmentManager,
    applicationContext: Context
) : ImageAnalysis.Analyzer {
    private val TAG = "MyAnalyzer"

    private var fragmentManager: FragmentManager
    private var bottomDialog: BottomDialog
    private var context: Context

    init {
        this.fragmentManager = supportFragmentManager
        this.bottomDialog = BottomDialog()
        this.context = applicationContext
    }

    override fun analyze(image: ImageProxy) {
        scanBarCode(image)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun scanBarCode(image: ImageProxy) {
        val tempImg = image.image
        tempImg?.let {
            val inputImage = InputImage.fromMediaImage(tempImg, image.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC
                )
                .build()

            val scanner = BarcodeScanning.getClient(options)
            val result = scanner.process(inputImage)
                .addOnSuccessListener { barcodes: List<Barcode> ->
                    readerBarCodeData(barcodes)
                }.addOnFailureListener {
                    //failed to read qr code
                    Toast.makeText(context, "Failed to read code", Toast.LENGTH_SHORT).show()
                }.addOnCompleteListener {
                    image.close()
                }
        }
    }

    private fun readerBarCodeData(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            val bounds = barcode.boundingBox
            val corners = arrayOf(barcode.cornerPoints)
            val rawValue = barcode.rawValue

            val valueType = barcode.valueType

            when (valueType) {
                Barcode.TYPE_WIFI -> {
                    val ssid = barcode.wifi?.ssid
                    val password = barcode.wifi?.password
                    val type = barcode.wifi?.encryptionType
                }

                Barcode.TYPE_URL -> {
                    if (!bottomDialog.isAdded) {
                        bottomDialog.show(fragmentManager, "")
                    }
                    bottomDialog.fetchUrl(barcode.url?.url)
                    val title = barcode.url?.title
                    val url = barcode.url?.url

                }
            }
        }
    }

}
