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

        addPresetRow(listOf(
            preset("Dark", P.KEY_BG to "#CC000000", P.KEY_HEADER to "#FFFFFFFF", P.KEY_WEEKDAY to "#FF9E9E9E", P.KEY_DAY to "#FFFFFFFF",
                P.KEY_WEEKEND to "#FFFF8A80", P.KEY_TODAY_CIRCLE to "#FF4FC3F7", P.KEY_TODAY_TEXT to "#FF000000"),
            preset("Light", P.KEY_BG to "#E6FFFFFF", P.KEY_HEADER to "#FF212121", P.KEY_WEEKDAY to "#FF757575", P.KEY_DAY to "#FF212121",
                P.KEY_WEEKEND to "#FFD32F2F", P.KEY_TODAY_CIRCLE to "#FF1976D2", P.KEY_TODAY_TEXT to "#FFFFFFFF"),
            preset("Ocean", P.KEY_BG to "#CC0D47A1", P.KEY_HEADER to "#FFFFFFFF", P.KEY_WEEKDAY to "#FF90CAF9", P.KEY_DAY to "#FFFFFFFF",
                P.KEY_WEEKEND to "#FF80DEEA", P.KEY_TODAY_CIRCLE to "#FFFFFFFF", P.KEY_TODAY_TEXT to "#FF0D47A1"),
            preset("Sunset", P.KEY_BG to "#CCBF360C", P.KEY_HEADER to "#FFFFF3E0", P.KEY_WEEKDAY to "#FFFFAB91", P.KEY_DAY to "#FFFFF3E0",
                P.KEY_WEEKEND to "#FFFFEB3B", P.KEY_TODAY_CIRCLE to "#FFFFEB3B", P.KEY_TODAY_TEXT to "#FFBF360C"),
        ))
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
