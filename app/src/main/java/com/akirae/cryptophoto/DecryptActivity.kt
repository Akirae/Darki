package com.akirae.cryptophoto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.akirae.cryptophoto.databinding.ActivityDecryptBinding

class DecryptActivity : AppCompatActivity() {
    private val model: DecryptModel by viewModels()
    private lateinit var binding: ActivityDecryptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecryptBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }


        model.uriCover.observe(this, {
            binding.result.setImageURI(it)
        })
        model.decryptedPixels.observe(this, {
            binding.loader.visibility = View.GONE

            val bitmapCover =
                BitmapFactory.decodeStream(contentResolver.openInputStream(model.uriCover.value!!))

            val b = bitmapCover.copy(Bitmap.Config.ARGB_8888, true)
            b.setPixels(
                it,
                0,
                bitmapCover.width,
                0,
                0,
                bitmapCover.width,
                bitmapCover.height
            )
            binding.result.setImageBitmap(b)
        })
    }
    fun choosePhotoCover(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            startActivityForResult(intent, EncryptActivity.cover)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EncryptActivity.cover && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriCover.value = uri }
        }
    }

    fun decrypt(view: View) {
        val bitmapCover =
            BitmapFactory.decodeStream(contentResolver.openInputStream(model.uriCover.value!!))
        val pixelsCover = IntArray(bitmapCover.width * bitmapCover.height)
        bitmapCover.getPixels(
            pixelsCover,
            0,
            bitmapCover.width,
            0,
            0,
            bitmapCover.width,
            bitmapCover.height
        )
        model.decrypt(pixelsCover)
        bitmapCover.recycle()
        binding.loader.visibility = View.VISIBLE
    }
}