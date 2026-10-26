package com.parth.onewidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.widget.TextView
import com.parth.onewidget.config.CalendarConfigActivity
import com.parth.onewidget.config.ClockAnalogConfigActivity
import com.parth.onewidget.config.ClockDigitalConfigActivity
import com.parth.onewidget.config.CombinedConfigActivity
import com.parth.onewidget.config.ConfigActivity
import com.parth.onewidget.config.CountdownConfigActivity
import com.parth.onewidget.providers.CalendarProvider
import com.parth.onewidget.providers.ClockAnalogProvider
import com.parth.onewidget.providers.ClockDigitalProvider
import com.parth.onewidget.providers.CombinedProvider
import com.parth.onewidget.providers.CountdownProvider

/** Lists the widgets currently on the home screen, each opening its settings. */
class MainActivity : ConfigActivity() {

    private class Kind(val label: String, val provider: Class<*>, val config: Class<*>)

    private val kinds = listOf(
        Kind("Digital clock", ClockDigitalProvider::class.java, ClockDigitalConfigActivity::class.java),
        Kind("Analog clock", ClockAnalogProvider::class.java, ClockAnalogConfigActivity::class.java),
        Kind("Calendar", CalendarProvider::class.java, CalendarConfigActivity::class.java),
        Kind("Clock + calendar", CombinedProvider::class.java, CombinedConfigActivity::class.java),
        Kind("Countdown", CountdownProvider::class.java, CountdownConfigActivity::class.java),
    )

    override val screenTitle: String get() = getString(R.string.app_name)
    override val screenSubtitle: String get() = "Sakura-style widgets, all yours to customise"
    override val showLogo: Boolean get() = true

    override fun onResume() {
        super.onResume()
        content.removeAllViews()

        addCard(TextView(this).apply {
            text = getString(R.string.home_hint)
            textSize = 15f
            setPadding(0, dp(14), 0, dp(14))
        })

        addSectionTitle("Your widgets")
        val manager = AppWidgetManager.getInstance(this)
        var found = false
        kinds.forEach { kind ->
            manager.getAppWidgetIds(ComponentName(this, kind.provider)).forEachIndexed { index, id ->
                found = true
                addNavRow("${kind.label} ${index + 1}", "Tap to customise") {
                    startActivity(
                        Intent(this, kind.config).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    )
                }
            }
        }
        if (!found) {
            addCard(TextView(this).apply {
                text = getString(R.string.no_widgets)
                textSize = 15f
                setPadding(0, dp(14), 0, dp(14))
            })
        }
    }
}
