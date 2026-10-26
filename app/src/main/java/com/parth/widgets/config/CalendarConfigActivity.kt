package com.parth.widgets.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.parth.widgets.providers.CalendarProvider as P

class CalendarConfigActivity : ConfigActivity() {
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
        addColorRow("Month title and arrows", P.KEY_HEADER, P.DEFAULT_HEADER)
        addColorRow("Weekday letters", P.KEY_WEEKDAY, P.DEFAULT_WEEKDAY)
        addColorRow("Day numbers", P.KEY_DAY, P.DEFAULT_DAY)
        addColorRow("Weekend numbers", P.KEY_WEEKEND, P.DEFAULT_WEEKEND)
        addColorRow("Today highlight", P.KEY_TODAY_CIRCLE, P.DEFAULT_TODAY_CIRCLE)
        addColorRow("Today number", P.KEY_TODAY_TEXT, P.DEFAULT_TODAY_TEXT)
        addSwitchRow("Week starts on Monday", P.KEY_MONDAY_FIRST, false)
        addAppRow("When tapped, open")
        addButton("Save") {
            P.update(this, widgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
