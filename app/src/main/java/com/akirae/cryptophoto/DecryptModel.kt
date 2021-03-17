package com.akirae.cryptophoto

import android.graphics.Color
import android.net.Uri
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DecryptModel: ViewModel() {
    private val significantBitsCount = 4
    val uriCover by lazy { MutableLiveData<Uri>() }

    val decryptedPixels by lazy { MutableLiveData<IntArray>() }

    fun decrypt(encryptedPixels: IntArray) {
        encryptedPixels.let {
            val enc = IntArray(it.size)
            viewModelScope.launch {
                it.forEachIndexed { index, pixel ->
                    enc[index] = shuffle(pixel.toColor())
                }
                decryptedPixels.value = enc
            }
        }

    }

    private suspend fun shuffle(cover: Color): Int = withContext(Dispatchers.IO) {
        val resR =
            (cover.toArgb().red and 15).shl(significantBitsCount)
        val resG =
            (cover.toArgb().green and 15).shl(significantBitsCount)
        val resB =
            (cover.toArgb().blue and 15).shl(significantBitsCount)

        return@withContext Color.rgb(resR, resG, resB)
    }
}
