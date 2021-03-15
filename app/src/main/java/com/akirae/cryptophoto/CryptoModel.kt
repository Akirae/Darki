package com.akirae.cryptophoto

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class CryptoModel : ViewModel() {
    val uriCover by lazy { MutableLiveData<Uri>() }
    var uriSecret:Uri? = null
    val photoFile by lazy { MutableLiveData<File>() }

    var textToEncrypt = ""
}