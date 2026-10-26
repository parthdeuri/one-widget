package com.parth.onewidget.prefs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object TapAction {
    /** Launches the app chosen for this widget, or [fallback] when none is set or it is gone. */
    fun pendingIntent(
        context: Context, widgetId: Int, fallback: Intent,
        key: String = WidgetPrefs.KEY_TAP_PACKAGE, slot: Int = 0,
    ): PendingIntent {
        val pkg = WidgetPrefs.getString(context, widgetId, key)
        val intent = pkg?.takeIf { it.isNotEmpty() }
            ?.let { context.packageManager.getLaunchIntentForPackage(it) }
            ?: fallback
        return PendingIntent.getActivity(
            context, widgetId * 10 + slot, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
