package com.parth.onewidget.config

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.parth.onewidget.render.Bg
import com.parth.onewidget.providers.CalendarProvider as P

class CalendarConfigActivity : ConfigActivity() {
    override val screenTitle: String get() = "Calendar"
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

        addThemeRow { s ->
            arrayOf(
                P.KEY_HEADER to s.text, P.KEY_WEEKDAY to s.sub, P.KEY_DAY to s.text, P.KEY_WEEKEND to s.weekend,
                P.KEY_TODAY_CIRCLE to s.highlight, P.KEY_TODAY_TEXT to s.onHighlight,
            )
        }
        addColorRow("Month title and arrows", P.KEY_HEADER, P.DEFAULT_HEADER)
        addColorRow("Weekday letters", P.KEY_WEEKDAY, P.DEFAULT_WEEKDAY)
        addColorRow("Day numbers", P.KEY_DAY, P.DEFAULT_DAY)
        addColorRow("Weekend numbers", P.KEY_WEEKEND, P.DEFAULT_WEEKEND)
        addColorRow("Today highlight", P.KEY_TODAY_CIRCLE, P.DEFAULT_TODAY_CIRCLE)
        addColorRow("Today number", P.KEY_TODAY_TEXT, P.DEFAULT_TODAY_TEXT)
        addSwitchRow("Week starts on Monday", P.KEY_MONDAY_FIRST, false)
        addSwitchRow("Show dots on days with events", P.KEY_EVENT_DOTS, false)
        addColorRow("Event dot colour", P.KEY_EVENT_DOT, P.DEFAULT_EVENT_DOT)
        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            addButton("Allow calendar access for event dots") {
                requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), 100)
            }
        }
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
