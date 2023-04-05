package com.abhi41.firebasemlkit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abhi41.firebasemlkit.databinding.ActivityMainBinding
import com.abhi41.firebasemlkit.screen.FaceDetectionScreen
import com.abhi41.firebasemlkit.screen.OcrScreen
import com.abhi41.firebasemlkit.screen.QRCodeScannerScreen
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(applicationContext)

        binding.btnOcrTextToSpeeach.setOnClickListener {
            val intent = Intent(this, OcrScreen::class.java)
            startActivity(intent)
        }

        binding.btnQrCode.setOnClickListener {
            val intent = Intent(this, QRCodeScannerScreen::class.java)
            startActivity(intent)
        }

        binding.btnFaceDetection.setOnClickListener {
            val intent = Intent(this, FaceDetectionScreen::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()

    }
}
