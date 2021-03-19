package com.akirae.cryptophoto

import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class DecryptModel : ViewModel() {
    private val significantBitsCount = 4

    val uriCover by lazy { MutableLiveData<Uri>() }

    val decryptedPixels by lazy { MutableLiveData<IntArray>() }

    fun decrypt(encryptedPixels: IntArray) {
        viewModelScope.launch(Dispatchers.Default) {
            encryptedPixels
                .map {
                    async {
                        shuffle(it)
                    }
                }
                .awaitAll()
                .also {
                    withContext(Dispatchers.Main) {
                        decryptedPixels.value = it.toIntArray()
                    }
                }
        }
    }

    private fun shuffle(cover: Int): Int {
        val resR: Int = Color.red(cover).shl(8 - significantBitsCount) and 255
        val resG: Int = Color.green(cover).shl(8 - significantBitsCount) and 255
        val resB: Int = Color.blue(cover).shl(8 - significantBitsCount) and 255
        return Color.argb(255, resR, resG, resB)
    }

}
