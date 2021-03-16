package com.akirae.darki

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun encode(view: View) {
        startActivity<EncodeActivity>()
    }

    fun decode(view: View) {
        startActivity<DecodeActivity>()
    }
}