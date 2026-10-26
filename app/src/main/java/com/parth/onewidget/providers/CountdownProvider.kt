package com.parth.onewidget.providers

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.parth.onewidget.R
import com.parth.onewidget.config.CountdownConfigActivity
import com.parth.onewidget.prefs.TapAction
import com.parth.onewidget.prefs.WidgetPrefs
import com.parth.onewidget.render.BackgroundRenderer
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

/** Days until (or since) a chosen date, with a label. */
class CountdownProvider : AppWidgetProvider() {

    companion object {
        const val KEY_LABEL = "cd_label"
        const val KEY_DATE = "cd_date"
        const val KEY_LABEL_COLOR = "cd_label_color"
        const val KEY_COUNT_COLOR = "cd_count_color"
        const val KEY_UNIT_COLOR = "cd_unit_color"
        const val KEY_LABEL_SIZE = "cd_label_size"
        const val KEY_UNIT_SIZE = "cd_unit_size"

        const val DEFAULT_LABEL = "Countdown"
        const val DEFAULT_BG = 0xCC000000.toInt()
        const val DEFAULT_LABEL_COLOR = 0xFFCCCCCC.toInt()
        const val DEFAULT_COUNT_COLOR = 0xFFFFFFFF.toInt()
        const val DEFAULT_UNIT_COLOR = 0xFFCCCCCC.toInt()

        private const val ACTION_REFRESH = "com.parth.onewidget.COUNTDOWN_REFRESH"

        fun update(context: Context, id: Int) {
            val label = WidgetPrefs.getString(context, id, KEY_LABEL)?.takeIf { it.isNotEmpty() } ?: DEFAULT_LABEL
            val target = WidgetPrefs.getString(context, id, KEY_DATE)
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

            val (count, unit) = if (target == null) {
                "—" to "Set a date"
            } else {
                when (val days = ChronoUnit.DAYS.between(LocalDate.now(), target)) {
                    0L -> "Today" to ""
                    1L -> "1" to "day to go"
                    -1L -> "1" to "day ago"
                    else -> if (days > 0) "$days" to "days to go" else "${-days}" to "days ago"
                }
            }

            val color = { key: String, def: Int -> WidgetPrefs.getColor(context, id, key, def) }
            val views = RemoteViews(context.packageName, R.layout.widget_countdown)
            views.setImageViewBitmap(R.id.bg, BackgroundRenderer.renderFor(context, id, DEFAULT_BG))

            views.setTextViewText(R.id.label, label)
            views.setTextColor(R.id.label, color(KEY_LABEL_COLOR, DEFAULT_LABEL_COLOR))
            views.setTextViewTextSize(
                R.id.label, TypedValue.COMPLEX_UNIT_SP, WidgetPrefs.getInt(context, id, KEY_LABEL_SIZE, 16).toFloat()
            )
            views.setTextViewText(R.id.count, count)
            views.setTextColor(R.id.count, color(KEY_COUNT_COLOR, DEFAULT_COUNT_COLOR))
            views.setTextViewText(R.id.unit, unit)
            views.setViewVisibility(R.id.unit, if (unit.isEmpty()) View.GONE else View.VISIBLE)
            views.setTextColor(R.id.unit, color(KEY_UNIT_COLOR, DEFAULT_UNIT_COLOR))
            views.setTextViewTextSize(
                R.id.unit, TypedValue.COMPLEX_UNIT_SP, WidgetPrefs.getInt(context, id, KEY_UNIT_SIZE, 14).toFloat()
            )

            val settings = Intent(context, CountdownConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            views.setOnClickPendingIntent(R.id.root, TapAction.pendingIntent(context, id, settings))
            AppWidgetManager.getInstance(context).updateAppWidget(id, views)
        }

        private fun refreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, CountdownProvider::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /** The day count changes at midnight; an inexact alarm just after it is enough. */
        private fun scheduleMidnight(context: Context) {
            val midnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)
            }
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                .setAndAllowWhileIdle(AlarmManager.RTC, midnight.timeInMillis, refreshIntent(context))
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, it) }
        scheduleMidnight(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        update(context, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetPrefs.clear(context, it) }
    }

    override fun onDisabled(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(refreshIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, CountdownProvider::class.java))
            if (ids.isNotEmpty()) {
                ids.forEach { update(context, it) }
                scheduleMidnight(context)
            }
        }
    }
}
