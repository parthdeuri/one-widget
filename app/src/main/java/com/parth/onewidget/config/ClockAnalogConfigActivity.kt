package com.parth.onewidget.config

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.parth.onewidget.render.Bg
import com.parth.onewidget.providers.ClockAnalogProvider as P

class ClockAnalogConfigActivity : ConfigActivity() {
    override val screenTitle: String get() = "Analog clock"
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

        addThemeRow(clearBackground = true) { s ->
            arrayOf(P.KEY_FACE to s.bg, P.KEY_TICKS to s.sub, P.KEY_HOUR to s.text, P.KEY_MINUTE to s.highlight)
        }
        addColorRow("Face colour", P.KEY_FACE, P.DEFAULT_FACE)
        addSliderRow("Face opacity", P.KEY_FACE_OPACITY, 0, 100, 100, "%")
        addColorRow("Ticks and numbers colour", P.KEY_TICKS, P.DEFAULT_TICKS)
        addColorRow("Hour hand colour", P.KEY_HOUR, P.DEFAULT_HOUR)
        addColorRow("Minute hand colour", P.KEY_MINUTE, P.DEFAULT_MINUTE)
        addSwitchRow("Show numbers", P.KEY_NUMBERS, false)
        addSliderRow("Number size", P.KEY_NUMBER_SCALE, 60, 160, 100, "%")
        addSliderRow("Hand thickness", P.KEY_HAND_SCALE, 60, 200, 100, "%")
        addBackgroundSection(P.DEFAULT_BG)
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
