package com.parth.onewidget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import com.parth.onewidget.render.Bg
import com.parth.onewidget.providers.CountdownProvider as P

class CountdownConfigActivity : ConfigActivity() {
    override val screenTitle: String get() = "Countdown"
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

        addTextRow("Label", P.KEY_LABEL, P.DEFAULT_LABEL)
        addDateRow("Target date", P.KEY_DATE)

        addThemeRow { s -> arrayOf(P.KEY_LABEL_COLOR to s.sub, P.KEY_COUNT_COLOR to s.text, P.KEY_UNIT_COLOR to s.accent) }
        addColorRow("Label colour", P.KEY_LABEL_COLOR, P.DEFAULT_LABEL_COLOR)
        addColorRow("Number colour", P.KEY_COUNT_COLOR, P.DEFAULT_COUNT_COLOR)
        addColorRow("Unit colour", P.KEY_UNIT_COLOR, P.DEFAULT_UNIT_COLOR)
        addSliderRow("Label size", P.KEY_LABEL_SIZE, 8, 40, 16, "sp")
        addSliderRow("Unit size", P.KEY_UNIT_SIZE, 8, 40, 14, "sp")
        addBackgroundSection(P.DEFAULT_BG)
        addAppRow("When tapped, open")
        addButton("Save") {
            P.update(this, widgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
