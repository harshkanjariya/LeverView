package com.harsh.leverview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lever: LiverView = findViewById(R.id.lever)

        lever.setOnStateChangeListener { type, index ->
        }
    }
}