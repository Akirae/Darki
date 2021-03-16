package com.akirae.darki

import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncodeModel @ViewModelInject constructor() : ViewModel() {

    val uri by lazy { MutableLiveData<Uri>() }
    val encoding by lazy { MutableLiveData<String>() }
    val file by lazy { MutableLiveData<File>() }

    fun encrypt(data: IntArray) {
        viewModelScope.launch {
            try {
                encoding.value = encryptAsync(data)
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    }

    private suspend fun encryptAsync(data: IntArray): String {
        return withContext(Dispatchers.IO) {
            var enc = ""
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            val iv = IvParameterSpec("ZzhTiR64TGrFexfT".toByteArray(Charsets.UTF_8))
            val keySpec = SecretKeySpec("FlUyfMAtHI10dkms".toByteArray(Charsets.UTF_8), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)

            data.forEach {
                val encoded = cipher.doFinal(it.toString().toByteArray(Charsets.UTF_8))
                enc += "${Base64.encodeToString(encoded, Base64.DEFAULT)}."
                Log.i("enc", "encrypt: $enc")
            }
            return@withContext enc
        }
    }

    fun decrypt(str: String) {
        viewModelScope.launch {
            try {
                var decrypted = ""

                val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
                val iv = IvParameterSpec("ZzhTiR64TGrFexfT".toByteArray(Charsets.UTF_8))
                val keySpec = SecretKeySpec("FlUyfMAtHI10dkms".toByteArray(Charsets.UTF_8), "AES")
                cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)

                str.split(".").forEach {
                    if (it != "") {
                        val raw = Base64.decode(it, Base64.DEFAULT)
                        val dec = cipher.doFinal(raw)
                        decrypted += String(dec, Charsets.UTF_8)
                    }
                }

            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }

        }

    }
}
