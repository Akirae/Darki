package com.akirae.darki.ui.encrypt

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.renderscript.Allocation
import androidx.renderscript.RenderScript
import com.akirae.darki.ScriptC_encryption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class EncryptViewModel @Inject constructor(private val renderScript: RenderScript) : ViewModel() {
    var significantBitsCount = 4
    private val mask = (255 shr significantBitsCount) shl significantBitsCount

    val uriCover by lazy { MutableLiveData<Uri?>(null) }
    val uriSecret by lazy { MutableLiveData<Uri?>(null) }

    val resultAllocation by lazy { MutableLiveData<Allocation>() }

    //not used
    val encryptedPixels by lazy { MutableLiveData<IntArray>() }

    fun encryptWithRenderscript(bitmapCover: Bitmap, bitmapSecret: Bitmap) {
        // Allocate buffers
        val mInAllocationCover = Allocation.createFromBitmap(renderScript, bitmapCover)
        val mInAllocationSecret = Allocation.createFromBitmap(renderScript, bitmapSecret)
        val mOutAllocation = Allocation.createFromBitmap(renderScript, bitmapCover)

        //init stego script
        val mScript = ScriptC_encryption(renderScript).apply {
            //set constants
            _mask = mask
            _significantBitsCount = significantBitsCount
        }

        mScript.forEach_encryption(mInAllocationCover, mInAllocationSecret, mOutAllocation)
        resultAllocation.value = mOutAllocation
    }

    //not used
    fun encryptWithNative(cover: IntArray, secret: IntArray) {
        viewModelScope.launch(Dispatchers.IO) {
            cover.parallelMap(secret)
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