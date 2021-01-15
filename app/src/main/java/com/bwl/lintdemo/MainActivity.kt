package com.bwl.lintdemo

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Color.parseColor("color")
        try {
            Color.parseColor("color")
        } catch (e: Exception) {

        }
    }
}