package com.abhi41.firebasemlkit.screen

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.abhi41.firebasemlkit.databinding.ActivityOcrBinding
import com.abhi41.firebasemlkit.util.Constants.PICK_IMAGE
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale.ENGLISH

class OcrScreen : AppCompatActivity() {
    lateinit var binding: ActivityOcrBinding

    lateinit var textRecognizer: TextRecognizer
    lateinit var textToSpeech: TextToSpeech
    lateinit var inputImage: InputImage


    val pickImage = registerForActivityResult(ActivityResultContracts.GetContent().apply {
        Intent.ACTION_PICK
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    ) { uri: Uri? ->
        uri?.let {
            try {
                inputImage = InputImage.fromFilePath(this, uri)
                val resultUri: Bitmap? = inputImage.bitmapInternal

                Glide.with(applicationContext)
                    .load(resultUri)
                    .into(binding.imgResult)

                // Process the Image
                val result = textRecognizer.process(inputImage).addOnSuccessListener { text ->
                    ProcessTextBlock(text)
                }.addOnFailureListener {
                    Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        clickListener()

    }

    private fun clickListener() {

        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { i ->
            if (i != TextToSpeech.ERROR) {
                textToSpeech.language = ENGLISH
            }
        })

        binding.btnReadtext.setOnClickListener {
            textToSpeech.speak(binding.txtReadText.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
        }

        binding.btnChooseImg.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        /* val chooserIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
         startActivityForResult(chooserIntent, PICK_IMAGE)*/
        pickImage.launch("image/*")
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE) {

            if (data != null) {
                val byteArry = arrayOf(0)
                val filePath: String?

                try {
                    inputImage = InputImage.fromFilePath(this, data.data!!)
                    val resultUri: Bitmap? = inputImage.bitmapInternal

                    Glide.with(applicationContext)
                        .load(resultUri)
                        .into(binding.imgResult)

                    // Process the Image
                    val result = textRecognizer.process(inputImage).addOnSuccessListener { text ->
                        ProcessTextBlock(text)
                    }.addOnFailureListener {
                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }

    }*/

    private fun ProcessTextBlock(text: Text?) {
        binding.txtReadText.setText("")
        text?.let {
            for (block in text.textBlocks) {
                binding.txtReadText.append("\n")
                for (line in block.lines) {
                    for (element in line.elements) {
                        binding.txtReadText.append("")
                        val elementText = element.text
                        binding.txtReadText.append(elementText)
                    }
                }
            }
        }
    }

    override fun onPause() {
        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        super.onPause()
    }

}