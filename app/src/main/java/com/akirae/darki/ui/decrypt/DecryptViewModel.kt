package com.akirae.darki.ui.decrypt

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.renderscript.Allocation
import androidx.renderscript.RenderScript
import com.akirae.darki.ScriptC_decryption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class DecryptViewModel @Inject constructor(private val renderScript: RenderScript): ViewModel() {
    private val significantBitsCount = 4
    val uriCover by lazy { MutableLiveData<Uri>() }
    val resultAllocation by lazy { MutableLiveData<Allocation>() }



    fun decryptWithRenderscript(bitmapCover: Bitmap) {
        // Allocate buffers
        val mInAllocationCover = Allocation.createFromBitmap(renderScript, bitmapCover)
        val mOutAllocation = Allocation.createFromBitmap(renderScript, bitmapCover)

        //init stego script
        val mScript = ScriptC_decryption(renderScript).apply {
            //set constants
            _significantBitsCount = significantBitsCount
        }

        mScript.forEach_decryption(mInAllocationCover, mOutAllocation)
        resultAllocation.value = mOutAllocation
    }

    //not used
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
