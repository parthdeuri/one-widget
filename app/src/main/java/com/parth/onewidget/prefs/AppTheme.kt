package com.parth.onewidget.prefs

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import com.parth.onewidget.R

/** The look of the app's own screens (never of widgets): Sakura or Normal, chosen explicitly. */
object AppTheme {
    const val NORMAL = 0
    const val SAKURA = 1

    /** Indexed by mode, so a chooser position is the mode. */
    val NAMES = listOf("Normal", "Sakura")

    private fun prefs(context: Context) = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun mode(context: Context): Int = prefs(context).getInt("app_theme", NORMAL)

    fun setMode(context: Context, mode: Int) {
        prefs(context).edit().putInt("app_theme", mode).apply()
    }

    /** Call before super.onCreate so the whole screen is inflated with the chosen theme. */
    fun applyTo(activity: Activity) {
        activity.setTheme(
            if (mode(activity) == NORMAL) R.style.Theme_OneWidget_Normal else R.style.Theme_OneWidget_Sakura
        )
    }

    /** Resolves one of the owXxx colour attributes against the activity's current theme. */
    fun color(context: Context, attr: Int): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(attr, value, true)
        return value.data
    }

    fun blossoms(context: Context): Boolean {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.owBlossoms, value, true)
        return value.data != 0
    }
}
