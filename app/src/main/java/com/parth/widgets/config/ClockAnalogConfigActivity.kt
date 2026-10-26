package com.parth.widgets.config

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.parth.widgets.providers.ClockAnalogProvider as P

class ClockAnalogConfigActivity : ConfigActivity() {
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
            preset("Dark", P.KEY_FACE to "#FF212121", P.KEY_TICKS to "#FFBDBDBD", P.KEY_HOUR to "#FFFFFFFF", P.KEY_MINUTE to "#FF4FC3F7"),
            preset("Light", P.KEY_FACE to "#FFFAFAFA", P.KEY_TICKS to "#FF616161", P.KEY_HOUR to "#FF212121", P.KEY_MINUTE to "#FFD32F2F"),
            preset("Ocean", P.KEY_FACE to "#FF0D47A1", P.KEY_TICKS to "#FFBBDEFB", P.KEY_HOUR to "#FFFFFFFF", P.KEY_MINUTE to "#FF80DEEA"),
            preset("Sunset", P.KEY_FACE to "#FFBF360C", P.KEY_TICKS to "#FFFFCCBC", P.KEY_HOUR to "#FFFFF3E0", P.KEY_MINUTE to "#FFFFEB3B"),
        ))
        addColorRow("Face colour", P.KEY_FACE, P.DEFAULT_FACE)
        addColorRow("Ticks and numbers colour", P.KEY_TICKS, P.DEFAULT_TICKS)
        addColorRow("Hour hand colour", P.KEY_HOUR, P.DEFAULT_HOUR)
        addColorRow("Minute hand colour", P.KEY_MINUTE, P.DEFAULT_MINUTE)
        addSwitchRow("Show numbers", P.KEY_NUMBERS, false)
        addAppRow("When tapped, open")

        val alarms = getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarms.canScheduleExactAlarms()) {
            addButton("Allow precise minute updates") {
                startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))
                )
            }
        }

        addButton("Save") {
            P.update(this, widgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
