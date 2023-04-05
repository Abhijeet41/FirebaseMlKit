package com.abhi41.firebasemlkit.screen

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.abhi41.firebasemlkit.databinding.ActivityFaceDetectionScreenBinding
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FaceDetectionScreen : AppCompatActivity() {
    private lateinit var currentPhotoName: String
    private lateinit var currentPhotoPath: String
    private val TAG = "FaceDetectionScreen"
    lateinit var binding: ActivityFaceDetectionScreenBinding

    var cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        feceDetectionProcess(it)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListener()
    }

    private fun clickListener() {
        binding.btnCamera.setOnClickListener {
            //takePicture.launch()
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
                null
            }
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    applicationContext,
                    applicationContext.getPackageName() + ".provider",
                    photoFile
                )
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoURI
                );
                cameraActivityResultLauncher.launch(cameraIntent)
            }

        }
    }

    private fun feceDetectionProcess(result: ActivityResult?) {
        if (result?.getResultCode() == Activity.RESULT_OK) {
            val image_uri = FileProvider.getUriForFile(
                applicationContext,
                applicationContext.getPackageName() + ".provider",
                File(currentPhotoPath)
            ) //You wll get the proper image uri here.


            //val image_uri: Uri? = result.data?.data
            if (image_uri != null) {
                val bitmap = getBitmapFromUri(image_uri)
                binding.txtHead.text = "Processing Image..."
                val builder = StringBuilder()
                val drawable = binding.imgFaced.drawable
                binding.imgFaced.setImageBitmap(bitmap)
                if (bitmap != null) {
                    val inputImage = InputImage.fromBitmap(bitmap, 0)
                    val highAccuracyOpt = FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        //.setMinFaceSize(1.0f)
                        .setClassificationMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .enableTracking().build()

                    val detector = FaceDetection.getClient(highAccuracyOpt)

                    val result: Task<List<Face>> = detector.process(inputImage)
                    result.addOnSuccessListener { faces ->

                        if (faces.size != 0) {
                            if (faces.size == 1) {
                                builder.append("${faces.size} Face Detected \n\n")
                            } else if (faces.size > 1) {
                                builder.append("${faces.size} Faces Detected \n\n")
                            }
                        }
                        for (face in faces) {
                            val id = face.trackingId
                            val rotY = face.headEulerAngleY
                            val rotZ = face.headEulerAngleZ

                            builder.append("1. Face Tracking ID [" + id + "]\n")
                            builder.append(
                                "2. Head Rotation to Right[" + String.format(
                                    "%.2f",
                                    rotY
                                ) + "deg. ]\n"
                            )
                            builder.append(
                                "3. Head Titled Sideways[" + String.format(
                                    "%.2f",
                                    rotZ
                                ) + "deg. ]\n"
                            )
                            showDetection("Face Detection", builder, true)
                        }

                    }.addOnFailureListener {
                        val strBuilder = StringBuilder()
                        strBuilder.append("Sorry!! There is an error!")
                        showDetection("Face Detection", builder, false)
                    }
                }
            } else {
                Log.d(TAG, "uri null: ")
            }
        }
    }

    private fun showDetection(title: String, builder: StringBuilder, isSuccess: Boolean) {
        if (isSuccess == true) {
            binding.txtHead.text = null
            binding.txtHead.movementMethod.apply {
                if (builder.length != 0) {
                    binding.txtHead.append(builder)
                    if (title.substring(0, title.indexOf(' ')).equals("OCR")) {
                        binding.txtHead.append("\n (Hold the text to copy it!)")
                    } else {
                        binding.txtHead.append("(Hold the text to copy it!)")
                    }

                    binding.txtHead.setOnLongClickListener {
                        val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE)
                        val clip = ClipData.newPlainText(title, builder)
                        clipBoard.apply { clip }
                        true
                    }
                } else {
                    binding.txtHead.append(title.substring(0, title.indexOf(' ')) + "Failed`!")
                }
            }
        } else if (isSuccess == false) {
            binding.txtHead.text = null
            binding.txtHead.movementMethod.apply {
                binding.txtHead.append(builder)
            }
        }
    }


    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val parcelFileDescriptor = applicationContext.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return bitmap
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat(
            "yy-MM-dd-HH-mm-ss-SS",
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "FNL_${timeStamp}_",
            ".jpg",
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            currentPhotoName = name
        }
    }
}