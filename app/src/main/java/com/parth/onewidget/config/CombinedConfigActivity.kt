package com.parth.onewidget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.parth.onewidget.providers.CalendarProvider
import com.parth.onewidget.providers.ClockDigitalProvider
import com.parth.onewidget.render.Bg
import com.parth.onewidget.providers.CombinedProvider as P

class CombinedConfigActivity : ConfigActivity() {
    override val screenTitle: String get() = "Clock + calendar"
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

        fun theme(
            name: String, bg: String, time: String, date: String, alarm: String,
            header: String, weekday: String, day: String, weekend: String, circle: String, circleText: String,
            vararg extra: Pair<String, String>,
        ) = preset(
            name, Bg.KEY_BG to bg,
            P.CLK_TIME to time, P.CLK_DATE to date, P.CLK_ALARM to alarm,
            P.CAL_HEADER to header, P.CAL_WEEKDAY to weekday, P.CAL_DAY to day, P.CAL_WEEKEND to weekend,
            P.CAL_TODAY_CIRCLE to circle, P.CAL_TODAY_TEXT to circleText,
            *extra,
        )
        addPresetRow(listOf(
            theme("Dark", "#CC000000", "#FFFFFFFF", "#FFCCCCCC", "#FFFFB74D",
                "#FFFFFFFF", "#FF9E9E9E", "#FFFFFFFF", "#FFFF8A80", "#FF4FC3F7", "#FF000000", *Bg.PLAIN),
            theme("Light", "#E6FFFFFF", "#FF212121", "#FF616161", "#FFE65100",
                "#FF212121", "#FF757575", "#FF212121", "#FFD32F2F", "#FF1976D2", "#FFFFFFFF", *Bg.PLAIN),
            theme("Cherry blossom", "#FFFFE4EC", "#FF880E4F", "#FFAD1457", "#FFC2185B",
                "#FF880E4F", "#FFAD1457", "#FF6D1B3B", "#FFD81B60", "#FFEC407A", "#FFFFFFFF",
                Bg.GRADIENT to "true", Bg.COLOR2 to "#FFF8BBD0", Bg.PATTERN to "6",
                Bg.PATTERN_COLOR to "#FFF06292", Bg.PATTERN_STRENGTH to "55"),
        ))

        addSwitchRow("Stack vertically (otherwise side by side)", P.KEY_STACKED, false)
        addSwitchRow("Swap: calendar first (left or top)", P.KEY_SWAP, false)

        addColorRow("Clock: time colour", P.CLK_TIME, ClockDigitalProvider.DEFAULT_TIME)
        addColorRow("Clock: date colour", P.CLK_DATE, ClockDigitalProvider.DEFAULT_DATE)
        addColorRow("Clock: alarm colour", P.CLK_ALARM, ClockDigitalProvider.DEFAULT_ALARM)
        addSwitchRow("24-hour time", P.CLK_24H, false)
        addSwitchRow("Show seconds", P.CLK_SECONDS, false)
        addSwitchRow("Show date", P.CLK_SHOW_DATE, true)
        addSwitchRow("Show next alarm", P.CLK_SHOW_ALARM, true)
        addSliderRow("Date size", P.CLK_DATE_SIZE, 8, 40, 13, "sp")
        addSliderRow("Alarm size", P.CLK_ALARM_SIZE, 8, 30, 12, "sp")

        addColorRow("Calendar: month title and arrows", P.CAL_HEADER, CalendarProvider.DEFAULT_HEADER)
        addColorRow("Calendar: weekday letters", P.CAL_WEEKDAY, CalendarProvider.DEFAULT_WEEKDAY)
        addColorRow("Calendar: day numbers", P.CAL_DAY, CalendarProvider.DEFAULT_DAY)
        addColorRow("Calendar: weekend numbers", P.CAL_WEEKEND, CalendarProvider.DEFAULT_WEEKEND)
        addColorRow("Calendar: today highlight", P.CAL_TODAY_CIRCLE, CalendarProvider.DEFAULT_TODAY_CIRCLE)
        addColorRow("Calendar: today number", P.CAL_TODAY_TEXT, CalendarProvider.DEFAULT_TODAY_TEXT)
        addSwitchRow("Week starts on Monday", P.CAL_MONDAY_FIRST, false)
        addSliderRow("Month title size", P.CAL_TITLE_SIZE, 10, 28, 14, "sp")
        addSliderRow("Day number size", P.CAL_NUMBER_SCALE, 60, 160, 100, "%")

        addBackgroundSection(P.DEFAULT_BG)
        addAppRow("Tapping the clock opens", P.TAP_CLOCK)
        addAppRow("Tapping the calendar opens", P.TAP_CAL)
        addButton("Save") {
            P.update(this, widgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
