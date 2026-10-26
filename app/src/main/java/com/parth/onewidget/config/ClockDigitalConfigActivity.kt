package com.parth.onewidget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.parth.onewidget.render.Bg
import com.parth.onewidget.providers.ClockDigitalProvider as P

class ClockDigitalConfigActivity : ConfigActivity() {
    override val screenTitle: String get() = "Digital clock"
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
            preset("Dark", *Bg.PLAIN, P.KEY_BG to "#CC000000", P.KEY_TIME to "#FFFFFFFF", P.KEY_DATE to "#FFCCCCCC", P.KEY_ALARM to "#FFFFB74D"),
            preset("Light", *Bg.PLAIN, P.KEY_BG to "#E6FFFFFF", P.KEY_TIME to "#FF212121", P.KEY_DATE to "#FF616161", P.KEY_ALARM to "#FFE65100"),
            preset("Ocean", *Bg.PLAIN, P.KEY_BG to "#CC0D47A1", P.KEY_TIME to "#FFFFFFFF", P.KEY_DATE to "#FFBBDEFB", P.KEY_ALARM to "#FF80DEEA"),
            preset("Sunset", *Bg.PLAIN, P.KEY_BG to "#CCBF360C", P.KEY_TIME to "#FFFFF3E0", P.KEY_DATE to "#FFFFCCBC", P.KEY_ALARM to "#FFFFEB3B"),
            preset("Cherry blossom", P.KEY_BG to "#FFFFE4EC", Bg.GRADIENT to "true", Bg.COLOR2 to "#FFF8BBD0",
                Bg.PATTERN to "6", Bg.PATTERN_COLOR to "#FFF06292", Bg.PATTERN_STRENGTH to "55",
                P.KEY_TIME to "#FF880E4F", P.KEY_DATE to "#FFAD1457", P.KEY_ALARM to "#FFC2185B"),
        ))
        addColorRow("Time colour", P.KEY_TIME, P.DEFAULT_TIME)
        addColorRow("Date colour", P.KEY_DATE, P.DEFAULT_DATE)
        addColorRow("Alarm colour", P.KEY_ALARM, P.DEFAULT_ALARM)
        addSwitchRow("24-hour time", P.KEY_24H, false)
        addSwitchRow("Show seconds", P.KEY_SECONDS, false)
        addSwitchRow("Show date", P.KEY_SHOW_DATE, true)
        addChoiceRow("Date position", P.KEY_DATE_POSITION, P.DATE_POSITIONS, 0)
        addChoiceRow("Date format", P.KEY_DATE_FORMAT, P.DATE_FORMAT_LABELS, 0)
        addSwitchRow("Show next alarm", P.KEY_SHOW_ALARM, true)
        addSwitchRow("Auto-fit time size to widget", P.KEY_AUTO_FIT, true)
        addSliderRow("Time size (when auto-fit is off)", P.KEY_TIME_SIZE, 16, 120, 48, "sp")
        addSliderRow("Date size", P.KEY_DATE_SIZE, 8, 40, 14, "sp")
        addSliderRow("Alarm size", P.KEY_ALARM_SIZE, 8, 30, 12, "sp")
        addBackgroundSection(P.DEFAULT_BG)
        addAppRow("When tapped, open")
        addButton("Save") {
            P.update(this, widgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
