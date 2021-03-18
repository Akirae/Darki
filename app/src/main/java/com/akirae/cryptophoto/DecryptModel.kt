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
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.util.concurrent.ForkJoinPool

class DecryptModel : ViewModel() {
    private val significantBitsCount = 4
    private val mask = 15

    val uriCover by lazy { MutableLiveData<Uri>() }

    val decryptedPixels by lazy { MutableLiveData<IntArray>() }

    fun decrypt(encryptedPixels: IntArray) {
        viewModelScope.launch(Dispatchers.Default) {
            encryptedPixels
                .map {
                    async {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            shuffle(it.toColor())
                        } else {
                            shuffle(it)
                        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shuffle(cover: Color): Int {
        val resR: Int =
            (cover.toArgb().red and mask).shl(significantBitsCount)
        val resG: Int =
            (cover.toArgb().green and mask).shl(significantBitsCount)
        val resB: Int =
            (cover.toArgb().blue and mask).shl(significantBitsCount)
        return Color.rgb(resR, resG, resB)

    }

    private fun shuffle(cover: Int): Int {
        val resR: Int = (Color.red(cover) and mask).shl(significantBitsCount)
        val resG: Int =
            (Color.green(cover) and mask).shl(significantBitsCount)
        val resB: Int = (Color.blue(cover) and mask).shl(significantBitsCount)
        return Color.rgb(resR, resG, resB)
    }

}
