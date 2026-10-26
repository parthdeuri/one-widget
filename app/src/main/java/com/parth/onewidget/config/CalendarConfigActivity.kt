package com.parth.onewidget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.parth.onewidget.render.Bg
import com.parth.onewidget.providers.CalendarProvider as P

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
            preset("Dark", *Bg.PLAIN, P.KEY_BG to "#CC000000", P.KEY_HEADER to "#FFFFFFFF", P.KEY_WEEKDAY to "#FF9E9E9E", P.KEY_DAY to "#FFFFFFFF",
                P.KEY_WEEKEND to "#FFFF8A80", P.KEY_TODAY_CIRCLE to "#FF4FC3F7", P.KEY_TODAY_TEXT to "#FF000000"),
            preset("Light", *Bg.PLAIN, P.KEY_BG to "#E6FFFFFF", P.KEY_HEADER to "#FF212121", P.KEY_WEEKDAY to "#FF757575", P.KEY_DAY to "#FF212121",
                P.KEY_WEEKEND to "#FFD32F2F", P.KEY_TODAY_CIRCLE to "#FF1976D2", P.KEY_TODAY_TEXT to "#FFFFFFFF"),
            preset("Ocean", *Bg.PLAIN, P.KEY_BG to "#CC0D47A1", P.KEY_HEADER to "#FFFFFFFF", P.KEY_WEEKDAY to "#FF90CAF9", P.KEY_DAY to "#FFFFFFFF",
                P.KEY_WEEKEND to "#FF80DEEA", P.KEY_TODAY_CIRCLE to "#FFFFFFFF", P.KEY_TODAY_TEXT to "#FF0D47A1"),
            preset("Sunset", *Bg.PLAIN, P.KEY_BG to "#CCBF360C", P.KEY_HEADER to "#FFFFF3E0", P.KEY_WEEKDAY to "#FFFFAB91", P.KEY_DAY to "#FFFFF3E0",
                P.KEY_WEEKEND to "#FFFFEB3B", P.KEY_TODAY_CIRCLE to "#FFFFEB3B", P.KEY_TODAY_TEXT to "#FFBF360C"),
            preset("Cherry blossom", P.KEY_BG to "#FFFFE4EC", Bg.GRADIENT to "true", Bg.COLOR2 to "#FFF8BBD0",
                Bg.PATTERN to "6", Bg.PATTERN_COLOR to "#FFF06292", Bg.PATTERN_STRENGTH to "55",
                P.KEY_HEADER to "#FF880E4F", P.KEY_WEEKDAY to "#FFAD1457", P.KEY_DAY to "#FF6D1B3B",
                P.KEY_WEEKEND to "#FFD81B60", P.KEY_TODAY_CIRCLE to "#FFEC407A", P.KEY_TODAY_TEXT to "#FFFFFFFF"),
        ))
        addColorRow("Month title and arrows", P.KEY_HEADER, P.DEFAULT_HEADER)
        addColorRow("Weekday letters", P.KEY_WEEKDAY, P.DEFAULT_WEEKDAY)
        addColorRow("Day numbers", P.KEY_DAY, P.DEFAULT_DAY)
        addColorRow("Weekend numbers", P.KEY_WEEKEND, P.DEFAULT_WEEKEND)
        addColorRow("Today highlight", P.KEY_TODAY_CIRCLE, P.DEFAULT_TODAY_CIRCLE)
        addColorRow("Today number", P.KEY_TODAY_TEXT, P.DEFAULT_TODAY_TEXT)
        addSwitchRow("Week starts on Monday", P.KEY_MONDAY_FIRST, false)
        addSliderRow("Month title size", P.KEY_TITLE_SIZE, 10, 28, 16, "sp")
        addSliderRow("Day number size", P.KEY_NUMBER_SCALE, 60, 160, 100, "%")
        addBackgroundSection(P.DEFAULT_BG)
        addAppRow("When tapped, open")
        addButton("Save") {
            P.update(this, widgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
