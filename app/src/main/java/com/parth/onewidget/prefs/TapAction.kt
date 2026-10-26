package com.parth.onewidget.prefs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object TapAction {
    /** Launches the app chosen for this widget, or [fallback] when none is set or it is gone. */
    fun pendingIntent(context: Context, widgetId: Int, fallback: Intent): PendingIntent {
        val pkg = WidgetPrefs.getString(context, widgetId, WidgetPrefs.KEY_TAP_PACKAGE)
        val intent = pkg?.takeIf { it.isNotEmpty() }
            ?.let { context.packageManager.getLaunchIntentForPackage(it) }
            ?: fallback
        return PendingIntent.getActivity(
            context, widgetId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
