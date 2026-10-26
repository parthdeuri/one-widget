package com.parth.widgets

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = getString(R.string.home_hint)
            textSize = 18f
            setPadding(48, 96, 48, 48)
        })
    }
}
