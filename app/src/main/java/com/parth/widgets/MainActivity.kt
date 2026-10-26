package com.parth.widgets

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import com.parth.widgets.config.ConfigActivity

/** Temporary demo of the shared settings rows, using widget id 0. Replaced by a widget list later. */
class MainActivity : ConfigActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = 0
        content.addView(TextView(this).apply {
            text = getString(R.string.home_hint)
            textSize = 16f
            setPadding(0, 0, 0, dp(16))
        })
        addColorRow("Background colour", "bg", Color.parseColor("#CC000000"))
        addColorRow("Text colour", "text", Color.WHITE)
        addAppRow("When tapped, open")
    }
}
