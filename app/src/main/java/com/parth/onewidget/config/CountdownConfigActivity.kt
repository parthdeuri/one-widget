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

        addPresetRow(listOf(
            preset("Dark", *Bg.PLAIN, Bg.KEY_BG to "#CC000000", P.KEY_LABEL_COLOR to "#FFCCCCCC", P.KEY_COUNT_COLOR to "#FFFFFFFF", P.KEY_UNIT_COLOR to "#FFCCCCCC"),
            preset("Light", *Bg.PLAIN, Bg.KEY_BG to "#E6FFFFFF", P.KEY_LABEL_COLOR to "#FF616161", P.KEY_COUNT_COLOR to "#FF212121", P.KEY_UNIT_COLOR to "#FF616161"),
            preset("Ocean", *Bg.PLAIN, Bg.KEY_BG to "#CC0D47A1", P.KEY_LABEL_COLOR to "#FFBBDEFB", P.KEY_COUNT_COLOR to "#FFFFFFFF", P.KEY_UNIT_COLOR to "#FF80DEEA"),
            preset("Sunset", *Bg.PLAIN, Bg.KEY_BG to "#CCBF360C", P.KEY_LABEL_COLOR to "#FFFFCCBC", P.KEY_COUNT_COLOR to "#FFFFF3E0", P.KEY_UNIT_COLOR to "#FFFFEB3B"),
            preset("Cherry blossom", Bg.KEY_BG to "#FFFFE4EC", Bg.GRADIENT to "true", Bg.COLOR2 to "#FFF8BBD0",
                Bg.PATTERN to "6", Bg.PATTERN_COLOR to "#FFF06292", Bg.PATTERN_STRENGTH to "55",
                P.KEY_LABEL_COLOR to "#FFAD1457", P.KEY_COUNT_COLOR to "#FF880E4F", P.KEY_UNIT_COLOR to "#FFC2185B"),
        ))
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
