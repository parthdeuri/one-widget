package com.parth.widgets.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.parth.widgets.providers.ClockDigitalProvider as P

class ClockDigitalConfigActivity : ConfigActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, result)

        addColorRow("Background colour", P.KEY_BG, P.DEFAULT_BG)
        addColorRow("Time colour", P.KEY_TIME, P.DEFAULT_TIME)
        addColorRow("Date colour", P.KEY_DATE, P.DEFAULT_DATE)
        addColorRow("Alarm colour", P.KEY_ALARM, P.DEFAULT_ALARM)
        addSwitchRow("24-hour time", P.KEY_24H, false)
        addSwitchRow("Show seconds", P.KEY_SECONDS, false)
        addSwitchRow("Show date", P.KEY_SHOW_DATE, true)
        addSwitchRow("Show next alarm", P.KEY_SHOW_ALARM, true)
        addAppRow("When tapped, open")
        addButton("Save") {
            P.update(this, widgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
