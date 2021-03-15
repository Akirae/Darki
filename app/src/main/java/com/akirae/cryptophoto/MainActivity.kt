package com.akirae.cryptophoto

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun decrypt(view: View) {
        startActivity(Intent(this@MainActivity, CryptoActivity::class.java))
    }
    fun encrypt(view: View) {
        startActivity(Intent(this@MainActivity, CryptoActivity::class.java))
    }
}