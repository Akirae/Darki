package com.akirae.darki.ui.encrypt

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.akirae.darki.databinding.FragmentEncryptBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EncryptFragment : Fragment() {
    companion object {
        const val COVER = 1
        const val SECRET = 2
    }

    private val model: EncryptViewModel by activityViewModels()
    private var _binding: FragmentEncryptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEncryptBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.selectCoverButton.setOnClickListener { getPhoto(COVER) }
        binding.selectSecretButton.setOnClickListener { getPhoto(SECRET) }
        binding.encrypt.setOnClickListener { encrypt() }
        binding.save.setOnClickListener { save() }

        model.uriCover.observe(viewLifecycleOwner, {
            if (it == null) {
                binding.selectCoverButton.visibility = VISIBLE
                binding.coverIsSelectedGroup.visibility = GONE
            } else {
                binding.coverImageView.clipToOutline = true
                binding.selectCoverButton.visibility = GONE
                binding.coverIsSelectedGroup.visibility = VISIBLE

                binding.removeCoverPhoto.setOnClickListener {
                    model.uriCover.value = null
                }
                binding.coverImageView.setImageURI(it)
            }

        })
        model.uriSecret.observe(viewLifecycleOwner, {
            if (it == null) {
                binding.selectSecretButton.visibility = VISIBLE
                binding.secretIsSelectedGroup.visibility = GONE
            } else {
                binding.secretImageView.clipToOutline = true
                binding.selectSecretButton.visibility = GONE
                binding.secretIsSelectedGroup.visibility = VISIBLE
                binding.removeSecretPhoto.setOnClickListener {
                    model.uriSecret.value = null
                }
                binding.secretImageView.setImageURI(it)
            }
        })
        model.resultAllocation.observe(viewLifecycleOwner, {
            val bm =
                BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(model.uriSecret.value!!))
            it.copyTo(bm)
            binding.resultImageView.setImageBitmap(bm)
            binding.stateImageIsNotReadyGroup.visibility = GONE
            binding.stateImageIsReadyGroup.visibility = VISIBLE
            binding.loader.visibility = GONE
            //  bm.recycle()
        })
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getPhoto(photoType: Int) {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            startActivityForResult(intent, photoType)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SECRET && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriSecret.value = uri }
        }
        if (requestCode == COVER && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriCover.value = uri }
        }
    }

    private fun getBitmapByUri(uri: Uri): Bitmap =
        BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))

    private fun encrypt() {
        binding.loader.visibility = VISIBLE
        if (model.uriSecret.value != null && model.uriCover.value != null) {
            val bitmapCover = getBitmapByUri(model.uriCover.value!!)

            val bitmapSecret =
                getBitmapByUri(model.uriSecret.value!!)
            model.encryptWithRenderscript(bitmapCover, bitmapSecret)

            //bitmapSecret.recycle()
            //bitmapCover.recycle()
        }
    }

    private fun save() {
        try {
            val bitmap = getBitmapByUri(model.uriCover.value!!)
            model.resultAllocation.value?.copyTo(bitmap)
            try {
                val timeStamp: String =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val file = File(storageDir, "JPEG_${timeStamp}_.jpg")
                val out = FileOutputStream(file)
                bitmap.compress(CompressFormat.PNG, 0, out)
                out.close()
                out.flush()
                Toast.makeText(activity, "Сохранено!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    activity, "Ошибка при сохранении. Повторите попытку.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            bitmap.recycle()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

    }


}