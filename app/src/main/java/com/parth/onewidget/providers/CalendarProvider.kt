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
import android.widget.RemoteViews
import com.parth.onewidget.render.BackgroundRenderer
import com.parth.onewidget.R
import com.parth.onewidget.config.CalendarConfigActivity
import com.parth.onewidget.prefs.TapAction
import com.parth.onewidget.prefs.WidgetPrefs
import com.parth.onewidget.render.CalendarRenderer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarProvider : AppWidgetProvider() {

    companion object {
        const val KEY_BG = "bg"
        const val KEY_HEADER = "header"
        const val KEY_WEEKDAY = "weekday"
        const val KEY_DAY = "day"
        const val KEY_WEEKEND = "weekend"
        const val KEY_TODAY_CIRCLE = "today_circle"
        const val KEY_TODAY_TEXT = "today_text"
        const val KEY_MONDAY_FIRST = "monday_first"
        const val KEY_TITLE_SIZE = "title_size"
        const val KEY_NUMBER_SCALE = "number_scale"
        private const val KEY_OFFSET = "month_offset"

        const val DEFAULT_BG = 0xCC000000.toInt()
        const val DEFAULT_HEADER = 0xFFFFFFFF.toInt()
        const val DEFAULT_WEEKDAY = 0xFF9E9E9E.toInt()
        const val DEFAULT_DAY = 0xFFFFFFFF.toInt()
        const val DEFAULT_WEEKEND = 0xFFFF8A80.toInt()
        const val DEFAULT_TODAY_CIRCLE = 0xFF4FC3F7.toInt()
        const val DEFAULT_TODAY_TEXT = 0xFF000000.toInt()

        private const val ACTION_PREV = "com.parth.onewidget.CAL_PREV"
        private const val ACTION_NEXT = "com.parth.onewidget.CAL_NEXT"
        private const val ACTION_TODAY = "com.parth.onewidget.CAL_TODAY"
        private const val ACTION_REFRESH = "com.parth.onewidget.CAL_REFRESH"
        private const val GRID_SIZE = 630

        fun update(context: Context, id: Int) {
            val color = { key: String, def: Int -> WidgetPrefs.getColor(context, id, key, def) }
            val offset = WidgetPrefs.getInt(context, id, KEY_OFFSET, 0)

            val now = Calendar.getInstance()
            val shown = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, offset)
            }
            val sameMonth = shown.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                shown.get(Calendar.MONTH) == now.get(Calendar.MONTH)

            val grid = CalendarRenderer.render(
                GRID_SIZE, shown,
                today = if (sameMonth) now.get(Calendar.DAY_OF_MONTH) else 0,
                mondayFirst = WidgetPrefs.getBool(context, id, KEY_MONDAY_FIRST, false),
                colors = CalendarRenderer.Colors(
                    weekday = color(KEY_WEEKDAY, DEFAULT_WEEKDAY),
                    day = color(KEY_DAY, DEFAULT_DAY),
                    weekend = color(KEY_WEEKEND, DEFAULT_WEEKEND),
                    todayCircle = color(KEY_TODAY_CIRCLE, DEFAULT_TODAY_CIRCLE),
                    todayText = color(KEY_TODAY_TEXT, DEFAULT_TODAY_TEXT),
                ),
                textScale = WidgetPrefs.getInt(context, id, KEY_NUMBER_SCALE, 100) / 100f,
            )

            val header = color(KEY_HEADER, DEFAULT_HEADER)
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)
            views.setImageViewBitmap(R.id.bg, BackgroundRenderer.renderFor(context, id, DEFAULT_BG))
            views.setImageViewBitmap(R.id.grid, grid)
            views.setTextViewText(R.id.title, SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(shown.time))
            listOf(R.id.title, R.id.prev, R.id.next).forEach { views.setTextColor(it, header) }
            views.setTextViewTextSize(
                R.id.title, TypedValue.COMPLEX_UNIT_SP, WidgetPrefs.getInt(context, id, KEY_TITLE_SIZE, 16).toFloat()
            )

            views.setOnClickPendingIntent(R.id.prev, action(context, id, ACTION_PREV, 1))
            views.setOnClickPendingIntent(R.id.next, action(context, id, ACTION_NEXT, 2))
            views.setOnClickPendingIntent(R.id.title, action(context, id, ACTION_TODAY, 3))
            val settings = Intent(context, CalendarConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            views.setOnClickPendingIntent(R.id.root, TapAction.pendingIntent(context, id, settings))

            AppWidgetManager.getInstance(context).updateAppWidget(id, views)
        }

        private fun action(context: Context, id: Int, action: String, slot: Int): PendingIntent =
            PendingIntent.getBroadcast(
                context, id * 10 + slot,
                Intent(context, CalendarProvider::class.java)
                    .setAction(action)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        private fun refreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, CalendarProvider::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /** Redraw just after midnight so the highlighted day moves. Inexact is fine here. */
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

        private fun shiftMonth(context: Context, id: Int, delta: Int) {
            val current = WidgetPrefs.getInt(context, id, KEY_OFFSET, 0)
            WidgetPrefs.setInt(context, id, KEY_OFFSET, if (delta == 0) 0 else current + delta)
            update(context, id)
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
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        when (intent.action) {
            ACTION_PREV -> shiftMonth(context, id, -1)
            ACTION_NEXT -> shiftMonth(context, id, 1)
            ACTION_TODAY -> shiftMonth(context, id, 0)
            ACTION_REFRESH -> {
                val ids = AppWidgetManager.getInstance(context)
                    .getAppWidgetIds(ComponentName(context, CalendarProvider::class.java))
                if (ids.isNotEmpty()) {
                    ids.forEach { update(context, it) }
                    scheduleMidnight(context)
                }
            }
        }
    }
}
