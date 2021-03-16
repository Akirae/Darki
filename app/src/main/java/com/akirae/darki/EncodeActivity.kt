package com.akirae.darki

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.akirae.darki.databinding.ActivityEncodeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncodeActivity : AppCompatActivity() {
    private val REQUEST_LOAD_FROM_GALLERY: Int = 1

    private lateinit var binding: ActivityEncodeBinding
    private val model: EncodeModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.add.setOnClickListener { loadFromGallery() }
        binding.encode.setOnClickListener {
            binding.encode.isEnabled = false
            binding.loading.visibility = VISIBLE
            encode(model.uri.value!!)
        }
        model.uri.observe(this, { uri ->
            binding.add.isEnabled = false
            binding.encode.isEnabled = true
            binding.save.isEnabled = false
        })
        model.encoding.observe(this, { str ->
            binding.add.isEnabled = false
            binding.encode.isEnabled = false
            binding.loading.visibility = GONE
            binding.save.isEnabled = true
        })
        model.file.observe(this, { file ->
            binding.add.isEnabled = false
            binding.encode.isEnabled = false
            binding.save.isEnabled = true
        })
    }

    private fun loadFromGallery() {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            startActivityForResult(
                intent,
                REQUEST_LOAD_FROM_GALLERY
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_LOAD_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                model.uri.value = uri
            }
        }
    }

    private fun encode(uri: Uri) {
        val bm = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        val height =bm.height
        val width = bm.width

        val bitmap = bm.copy(Bitmap.Config.ARGB_8888, true)
        bitmap.reconfigure(width / 5, height / 5, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        model.encrypt(pixels)
    }

    

}