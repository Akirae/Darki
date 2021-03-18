package com.akirae.cryptophoto

import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColor
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class EncryptModel : ViewModel() {
    private val significantBitsCount = 4
    private val mask = (255 shr significantBitsCount) shl significantBitsCount

    val uriCover by lazy { MutableLiveData<Uri>() }
    val uriSecret by lazy { MutableLiveData<Uri>() }

    val encryptedPixels by lazy { MutableLiveData<IntArray>() }

    fun encrypt(cover: IntArray, secret: IntArray) {

        viewModelScope.launch(Dispatchers.Default) {
            cover.mapIndexed { index, pixel ->
                async {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        shuffle(pixel.toColor(), secret[index].toColor())
                    } else {
                        shuffle(pixel, secret[index])
                    }

                }
            }.awaitAll()
                .also {
                    withContext(Dispatchers.Main) {
                        encryptedPixels.value = it.toIntArray()
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun shuffle(cover: Color, secret: Color): Int {
        val resR: Int =
            (cover.toArgb().red and mask) or secret.toArgb().red.shr(significantBitsCount)
        val resG: Int =
            (cover.toArgb().green and mask) or secret.toArgb().green.shr(significantBitsCount)
        val resB: Int =
            (cover.toArgb().blue and mask) or secret.toArgb().blue.shr(significantBitsCount)
        return Color.rgb(resR, resG, resB)

    }





    private fun shuffle(cover: Int, secret: Int): Int {
        val resR: Int = (Color.red(cover) and mask) or Color.red(secret).shr(significantBitsCount)
        val resG: Int =
            (Color.green(cover) and mask) or Color.green(secret).shr(significantBitsCount)
        val resB: Int = (Color.blue(cover) and mask) or Color.blue(secret).shr(significantBitsCount)
        return Color.rgb(resR, resG, resB)
    }


}
