package com.akirae.darki.ui.decrypt

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.akirae.darki.databinding.FragmentDecryptBinding
import com.akirae.darki.ui.encrypt.EncryptFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DecryptFragment : Fragment() {
    private val model: DecryptViewModel by activityViewModels()
    private var _binding: FragmentDecryptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecryptBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.selectCoverButton.setOnClickListener { getPhoto(EncryptFragment.COVER) }
        binding.decrypt.setOnClickListener { decrypt() }
        binding.save.setOnClickListener { save() }

        model.uriCover.observe(viewLifecycleOwner, {
            if (it == null) {
                binding.selectCoverButton.visibility = View.VISIBLE
                binding.coverIsSelectedGroup.visibility = View.GONE
            } else {
                binding.coverImageView.clipToOutline = true
                binding.selectCoverButton.visibility = View.GONE
                binding.coverIsSelectedGroup.visibility = View.VISIBLE

                binding.removeCoverPhoto.setOnClickListener {
                    model.uriCover.value = null
                }
                binding.coverImageView.setImageURI(it)
            }

        })
        model.resultAllocation.observe(viewLifecycleOwner, {
            val bm =
                BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(model.uriCover.value!!))
            it.copyTo(bm)
            binding.coverImageView.setImageBitmap(bm)
            binding.loader.visibility = View.GONE
            binding.save.visibility = View.VISIBLE
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
        if (requestCode == EncryptFragment.COVER && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri -> model.uriCover.value = uri }
        }
    }

    private fun getBitmapByUri(uri: Uri): Bitmap =
        BitmapFactory.decodeStream(activity?.contentResolver?.openInputStream(uri))

    private fun decrypt() {
        binding.loader.visibility = View.VISIBLE
        binding.decrypt.visibility = View.GONE
        if (model.uriCover.value != null) {
            val bitmapCover = getBitmapByUri(model.uriCover.value!!)
            model.decryptWithRenderscript(bitmapCover)
            bitmapCover.recycle()
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
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, out)
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