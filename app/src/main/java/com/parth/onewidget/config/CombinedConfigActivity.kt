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

        addThemeRow { s ->
            arrayOf(
                P.CLK_TIME to s.text, P.CLK_DATE to s.sub, P.CLK_ALARM to s.accent,
                P.CAL_HEADER to s.text, P.CAL_WEEKDAY to s.sub, P.CAL_DAY to s.text, P.CAL_WEEKEND to s.weekend,
                P.CAL_TODAY_CIRCLE to s.highlight, P.CAL_TODAY_TEXT to s.onHighlight,
            )
        }

        addSwitchRow("Stack vertically (otherwise side by side)", P.KEY_STACKED, false)
        addSwitchRow("Swap: calendar first (left or top)", P.KEY_SWAP, false)

        addColorRow("Clock: time colour", P.CLK_TIME, ClockDigitalProvider.DEFAULT_TIME)
        addColorRow("Clock: date colour", P.CLK_DATE, ClockDigitalProvider.DEFAULT_DATE)
        addColorRow("Clock: alarm colour", P.CLK_ALARM, ClockDigitalProvider.DEFAULT_ALARM)
        addSwitchRow("24-hour time", P.CLK_24H, false)
        addSwitchRow("Show seconds", P.CLK_SECONDS, false)
        addSwitchRow("Show date", P.CLK_SHOW_DATE, true)
        addSwitchRow("Show next alarm", P.CLK_SHOW_ALARM, true)
        addChoiceRow("Date position", P.CLK_DATE_POSITION, ClockDigitalProvider.DATE_POSITIONS, 0)
        addChoiceRow("Date format", P.CLK_DATE_FORMAT, ClockDigitalProvider.DATE_FORMAT_LABELS, 0)
        addSwitchRow("Auto-fit time size to widget", P.CLK_AUTO_FIT, true)
        addSliderRow("Time size (when auto-fit is off)", P.CLK_TIME_SIZE, 16, 120, 40, "sp")
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
