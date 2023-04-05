package com.abhi41.firebasemlkit.util

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.abhi41.firebasemlkit.databinding.BottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.concurrent.Executors

class BottomDialog : BottomSheetDialogFragment() {
    private lateinit var binding: BottomDialogBinding
    lateinit var fetchUrl: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtHead.setText(fetchUrl)

        binding.txtlink.setOnClickListener {
            val i = Intent("android.intent.action.VIEW").apply {
                setData(Uri.parse(fetchUrl))
            }
            startActivity(i)
        }

        binding.imgClose.setOnClickListener {
            dismiss()
        }

    }

    fun fetchUrl(url: String?) {
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        url?.let {
            executorService.execute {
                fetchUrl = url
            }
        }
    }
}