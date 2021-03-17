package com.akirae.cryptophoto

import android.content.ContentValues.TAG
import android.graphics.Color
import android.net.Uri
import android.util.Log
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
import java.util.*

class EncryptModel : ViewModel() {
    private val significantBitsCount = 4
    val uriCover by lazy { MutableLiveData<Uri>() }
    val uriSecret by lazy { MutableLiveData<Uri>() }

    val encryptedPixels by lazy { MutableLiveData<IntArray>() }

    fun encrypt(cover: IntArray, secret: IntArray) {
        val enc = IntArray(cover.size)
        viewModelScope.launch {
            cover.forEachIndexed { index, pixel ->
                enc[index] = shuffle(pixel.toColor(), secret[index].toColor())
            }
            encryptedPixels.value = enc
        }
    }

    private suspend fun shuffle(cover: Color, secret: Color): Int = withContext(Dispatchers.IO) {
        val resR =
            (cover.toArgb().red and 240) or secret.toArgb().red.shr(significantBitsCount)
        val resG =
            (cover.toArgb().green and 240) or secret.toArgb().green.shr(significantBitsCount)
        val resB =
            (cover.toArgb().blue and 240) or secret.toArgb().blue.shr(significantBitsCount)

        return@withContext Color.rgb(resR, resG, resB)
    }


}
