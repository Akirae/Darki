package com.akirae.cryptophoto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.toColor
import com.akirae.cryptophoto.databinding.ActivityCryptoBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CryptoActivity : AppCompatActivity() {
    companion object {
        const val cover = 1
        const val secret = 2
    }


    private val model: CryptoModel by viewModels()
    private lateinit var binding: ActivityCryptoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCryptoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        model.uriCover.observe(this, {
            binding.cipherLayout.visibility = VISIBLE
            binding.selectLayout.visibility = GONE
            binding.photo.setImageURI(it)
        })
    }

    fun choosePhoto(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            model.photoFile.value = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            model.photoFile.value?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.akirae.cryptophoto.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, cover)
            }
        }
    }

    fun choosePhotoSecret(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            model.photoFile.value = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            model.photoFile.value?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.akirae.cryptophoto.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, secret)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun encrypt(view: View) {
        if (model.uriSecret != null) {
            val bitmapCover =
                BitmapFactory.decodeStream(contentResolver.openInputStream(model.uriCover.value!!))
            val pixelsCover = IntArray(bitmapCover.width * bitmapCover.height)
            bitmapCover.getPixels(
                pixelsCover,
                0,
                bitmapCover.width,
                0,
                0,
                bitmapCover.height,
                bitmapCover.width
            )

            val bitmapSecret =
                BitmapFactory.decodeStream(contentResolver.openInputStream(model.uriSecret!!))
            val pixelsSecret = IntArray(bitmapSecret.width * bitmapSecret.height)
            bitmapSecret.getPixels(
                pixelsSecret,
                0,
                bitmapSecret.width,
                0,
                0,
                bitmapSecret.height,
                bitmapSecret.width
            )

            if (bitmapCover.width == bitmapSecret.width && bitmapCover.height == bitmapSecret.height)

                pixelsCover.forEachIndexed { index, pixel ->

                    pixelsSecret[index].toColor().also { secret ->

                        Log.i("secret", secret.toString())
                        pixel.toColor().also { cover ->
                            Log.i("cover", Companion.cover.toString())
                            val resR = cover.red().toString()
                                .substring(0, cover.red().toString().length - 3) + secret.red()
                                .toString().substring(0, 2)
                            val resG = cover.green().toString()
                                .substring(0, cover.green().toString().length - 3) + secret.green()
                                .toString().substring(0, 2)
                            val resB = cover.blue().toString()
                                .substring(0, cover.blue().toString().length - 3) + secret.blue()
                                .toString().substring(0, 2)

                            val result = Color.rgb(resR.toInt(), resG.toInt(), resB.toInt())

                            Log.i("result", result.toString())

                        }

                    }
                }

            bitmapCover.also { bitmap ->
                val out = FileOutputStream(model.photoFile.value)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.close()
                bitmap.recycle()
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == secret && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriSecret = uri }
        }
        if (requestCode == cover && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriCover.value = uri }
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(storageDir, "img_${timeStamp}.jpg").apply { createNewFile() }
    }

}