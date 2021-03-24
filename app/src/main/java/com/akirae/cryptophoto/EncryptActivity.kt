package com.akirae.cryptophoto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.akirae.cryptophoto.databinding.ActivityEncryptBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class EncryptActivity : AppCompatActivity() {
    companion object {
        const val cover = 1
        const val secret = 2
    }

    private val model: EncryptModel by viewModels()
    private lateinit var binding: ActivityEncryptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncryptBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        model.uriCover.observe(this, {
            binding.coverPreview.visibility = VISIBLE
            binding.coverPreview.setImageURI(it)
        })
        model.uriSecret.observe(this, {
            binding.secretPreview.visibility = VISIBLE
            binding.secretPreview.setImageURI(it)
        })

        model.encryptedPixels.observe(this, {
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
            binding.encrypt.visibility = GONE
            binding.loader.visibility = GONE
            binding.save.visibility = VISIBLE
        })

    }

    fun choosePhotoCover(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            startActivityForResult(intent, cover)
        }
    }

    fun choosePhotoSecret(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            startActivityForResult(intent, secret)
        }
    }

    fun encrypt(view: View) {
        binding.loader.visibility = VISIBLE
        if (model.uriSecret.value != null) {
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
            bitmapCover.recycle()

            val bitmapSecret =
                BitmapFactory.decodeStream(contentResolver.openInputStream(model.uriSecret.value!!))
            val pixelsSecret = IntArray(bitmapSecret.width * bitmapSecret.height)
            bitmapSecret.getPixels(
                pixelsSecret,
                0,
                bitmapSecret.width,
                0,
                0,
                bitmapSecret.width,
                bitmapSecret.height
            )
            bitmapSecret.recycle()
            if (pixelsCover.size == pixelsSecret.size) model.encrypt(pixelsCover, pixelsSecret)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == secret && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriSecret.value = uri }
        }
        if (requestCode == cover && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriCover.value = uri }
        }
    }

    fun save(view: View) {
        val resolver = applicationContext.contentResolver
        try {
            val resourceUri = model.uriSecret.value!!
            val bitmap = BitmapFactory.decodeStream(resolver.openInputStream(resourceUri))
            val b = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            b.setPixels(
                model.encryptedPixels.value,
                0,
                bitmap.width,
                0,
                0,
                bitmap.width,
                bitmap.height
            )
            try {
                val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(Date())
                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val file = File(storageDir, "JPEG_${timeStamp}_.jpg")
                val out = FileOutputStream(file)
                b.compress(CompressFormat.PNG, 0, out)
                out.close()
                out.flush()
                Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show()
            } catch (e: java.lang.Exception) {
                Toast.makeText(
                    this, "Ошибка при сохранении. Повторите попытку.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            //MediaStore.Images.Media.insertImage(resolver, b, "image", "img")
            b.recycle()
            bitmap.recycle()
        } catch (exception: Exception) {
            exception.printStackTrace()

        }

    }

}