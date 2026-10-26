package com.parth.onewidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.parth.onewidget.config.CalendarConfigActivity
import com.parth.onewidget.config.ClockAnalogConfigActivity
import com.parth.onewidget.config.ClockDigitalConfigActivity
import com.parth.onewidget.config.ConfigActivity
import com.parth.onewidget.providers.CalendarProvider
import com.parth.onewidget.providers.ClockAnalogProvider
import com.parth.onewidget.providers.ClockDigitalProvider

/** Lists the widgets currently on the home screen, each with a button to reopen its settings. */
class MainActivity : ConfigActivity() {

    private class Kind(val label: String, val provider: Class<*>, val config: Class<*>)

    private val kinds = listOf(
        Kind("Digital clock", ClockDigitalProvider::class.java, ClockDigitalConfigActivity::class.java),
        Kind("Analog clock", ClockAnalogProvider::class.java, ClockAnalogConfigActivity::class.java),
        Kind("Calendar", CalendarProvider::class.java, CalendarConfigActivity::class.java),
    )

    override fun onResume() {
        super.onResume()
        content.removeAllViews()
        content.addView(ImageView(this).apply { setImageResource(R.drawable.ic_logo) },
            LinearLayout.LayoutParams(dp(96), dp(96)))
        content.addView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 24f
            setPadding(0, dp(8), 0, dp(8))
        })
        content.addView(TextView(this).apply {
            text = getString(R.string.home_hint)
            textSize = 16f
            setPadding(0, 0, 0, dp(16))
        })

        val manager = AppWidgetManager.getInstance(this)
        var found = false
        kinds.forEach { kind ->
            manager.getAppWidgetIds(ComponentName(this, kind.provider)).forEachIndexed { index, id ->
                found = true
                addButton("Edit ${kind.label} ${index + 1}") {
                    startActivity(
                        Intent(this, kind.config).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    )
                }
            }
        }
        if (!found) {
            content.addView(TextView(this).apply { text = getString(R.string.no_widgets) })
        }
    }
}
