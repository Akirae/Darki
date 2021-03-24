package com.akirae.cryptophoto

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class EncryptModel : ViewModel() {
    private val significantBitsCount = 4
    private val mask = (255 shr significantBitsCount) shl significantBitsCount

    val uriCover by lazy { MutableLiveData<Uri>() }
    val uriSecret by lazy { MutableLiveData<Uri>() }

    val encryptedPixels by lazy { MutableLiveData<IntArray>() }

    fun encrypt(cover: IntArray, secret: IntArray) {
        viewModelScope.launch(Dispatchers.Default) {
            cover
                .parallelMap(secret)
                .also {
                    withContext(Dispatchers.Main) {
                        encryptedPixels.value = it.toIntArray()
                    }
                }
        }
    }

    private suspend fun IntArray.parallelMap(secret: IntArray) = coroutineScope {
        mapIndexed { index, pixel ->
            async {
                shuffle(pixel, secret[index])
            }
        }.awaitAll()
    }

    private fun shuffle(cover: Int, secret: Int): Int {
        val resR: Int =
            (Color.red(cover) and mask) or (Color.red(secret).shr(8 - significantBitsCount))
        val resG: Int =
            (Color.green(cover) and mask) or (Color.green(secret).shr(8 - significantBitsCount))
        val resB: Int =
            (Color.blue(cover) and mask) or (Color.blue(secret).shr(8 - significantBitsCount))
        return Color.argb(255, resR, resG, resB)
    }

}