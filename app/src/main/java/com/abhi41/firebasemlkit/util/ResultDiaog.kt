package com.abhi41.firebasemlkit.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.abhi41.firebasemlkit.databinding.FragmentResultDialogBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector

class ResultDiaog : DialogFragment() {
    lateinit var binding: FragmentResultDialogBinding
    lateinit var text: String
    lateinit var firebaseVision: InputImage
    lateinit var visiondetectore:FaceDetector

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultDialogBinding.inflate(inflater, container, false)

        val bundle: Bundle? = arguments
        text = bundle?.getString("RESULT_TEXT").toString()
        binding.txtTitle.text = text


        binding.btnOk.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

}