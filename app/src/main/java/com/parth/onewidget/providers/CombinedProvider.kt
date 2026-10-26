package com.parth.onewidget.providers

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.parth.onewidget.R
import com.parth.onewidget.config.CombinedConfigActivity
import com.parth.onewidget.prefs.TapAction
import com.parth.onewidget.prefs.WidgetPrefs
import com.parth.onewidget.render.BackgroundRenderer
import com.parth.onewidget.render.CalendarRenderer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Digital clock and month calendar in one widget, each side with its own tap target. */
class CombinedProvider : AppWidgetProvider() {

    companion object {
        const val KEY_STACKED = "stacked"
        const val KEY_SWAP = "swap"
        const val TAP_CLOCK = "tap_clock"
        const val TAP_CAL = "tap_cal"

        const val CLK_TIME = "clk_time"
        const val CLK_DATE = "clk_date"
        const val CLK_ALARM = "clk_alarm"
        const val CLK_24H = "clk_24h"
        const val CLK_SECONDS = "clk_seconds"
        const val CLK_SHOW_DATE = "clk_show_date"
        const val CLK_SHOW_ALARM = "clk_show_alarm"
        const val CLK_DATE_SIZE = "clk_date_size"
        const val CLK_ALARM_SIZE = "clk_alarm_size"

        const val CAL_HEADER = "cal_header"
        const val CAL_WEEKDAY = "cal_weekday"
        const val CAL_DAY = "cal_day"
        const val CAL_WEEKEND = "cal_weekend"
        const val CAL_TODAY_CIRCLE = "cal_today_circle"
        const val CAL_TODAY_TEXT = "cal_today_text"
        const val CAL_MONDAY_FIRST = "cal_monday_first"
        const val CAL_TITLE_SIZE = "cal_title_size"
        const val CAL_NUMBER_SCALE = "cal_number_scale"
        private const val CAL_OFFSET = "cal_offset"

        const val DEFAULT_BG = 0xCC000000.toInt()

        private const val ACTION_PREV = "com.parth.onewidget.COMB_PREV"
        private const val ACTION_NEXT = "com.parth.onewidget.COMB_NEXT"
        private const val ACTION_TODAY = "com.parth.onewidget.COMB_TODAY"
        private const val ACTION_REFRESH = "com.parth.onewidget.COMB_REFRESH"
        private const val GRID_SIZE = 630

        fun update(context: Context, id: Int) {
            val stacked = WidgetPrefs.getBool(context, id, KEY_STACKED, false)
            val calendarFirst = WidgetPrefs.getBool(context, id, KEY_SWAP, false)
            val layout = when {
                stacked && calendarFirst -> R.layout.widget_combined_v_cal
                stacked -> R.layout.widget_combined_v_clock
                calendarFirst -> R.layout.widget_combined_h_cal
                else -> R.layout.widget_combined_h_clock
            }
            val views = RemoteViews(context.packageName, layout)
            views.setImageViewBitmap(R.id.bg, BackgroundRenderer.renderFor(context, id, DEFAULT_BG))
            bindClock(context, id, views)
            bindCalendar(context, id, views)

            val settings = Intent(context, CombinedConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            views.setOnClickPendingIntent(R.id.clock_panel, TapAction.pendingIntent(context, id, settings, TAP_CLOCK, 4))
            views.setOnClickPendingIntent(R.id.cal_panel, TapAction.pendingIntent(context, id, settings, TAP_CAL, 5))
            AppWidgetManager.getInstance(context).updateAppWidget(id, views)
        }

        private fun bindClock(context: Context, id: Int, views: RemoteViews) {
            val color = { key: String, def: Int -> WidgetPrefs.getColor(context, id, key, def) }
            val flag = { key: String, def: Boolean -> WidgetPrefs.getBool(context, id, key, def) }
            val size = { key: String, def: Int -> WidgetPrefs.getInt(context, id, key, def).toFloat() }

            val format = (if (flag(CLK_24H, false)) "HH:mm" else "h:mm") + if (flag(CLK_SECONDS, false)) ":ss" else ""
            views.setCharSequence(R.id.time, "setFormat12Hour", format)
            views.setCharSequence(R.id.time, "setFormat24Hour", format)
            views.setTextColor(R.id.time, color(CLK_TIME, ClockDigitalProvider.DEFAULT_TIME))

            views.setViewVisibility(R.id.date, if (flag(CLK_SHOW_DATE, true)) View.VISIBLE else View.GONE)
            views.setCharSequence(R.id.date, "setFormat12Hour", "EEE, d MMM")
            views.setCharSequence(R.id.date, "setFormat24Hour", "EEE, d MMM")
            views.setTextColor(R.id.date, color(CLK_DATE, ClockDigitalProvider.DEFAULT_DATE))
            views.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_SP, size(CLK_DATE_SIZE, 13))

            val nextAlarm = if (flag(CLK_SHOW_ALARM, true)) {
                (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock?.triggerTime
            } else null
            if (nextAlarm != null) {
                views.setViewVisibility(R.id.alarm, View.VISIBLE)
                views.setTextViewText(R.id.alarm, "⏰ " + DateFormat.format("EEE h:mm a", nextAlarm))
                views.setTextColor(R.id.alarm, color(CLK_ALARM, ClockDigitalProvider.DEFAULT_ALARM))
                views.setTextViewTextSize(R.id.alarm, TypedValue.COMPLEX_UNIT_SP, size(CLK_ALARM_SIZE, 12))
            } else {
                views.setViewVisibility(R.id.alarm, View.GONE)
            }
        }

        private fun bindCalendar(context: Context, id: Int, views: RemoteViews) {
            val color = { key: String, def: Int -> WidgetPrefs.getColor(context, id, key, def) }
            val offset = WidgetPrefs.getInt(context, id, CAL_OFFSET, 0)

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
                mondayFirst = WidgetPrefs.getBool(context, id, CAL_MONDAY_FIRST, false),
                colors = CalendarRenderer.Colors(
                    weekday = color(CAL_WEEKDAY, CalendarProvider.DEFAULT_WEEKDAY),
                    day = color(CAL_DAY, CalendarProvider.DEFAULT_DAY),
                    weekend = color(CAL_WEEKEND, CalendarProvider.DEFAULT_WEEKEND),
                    todayCircle = color(CAL_TODAY_CIRCLE, CalendarProvider.DEFAULT_TODAY_CIRCLE),
                    todayText = color(CAL_TODAY_TEXT, CalendarProvider.DEFAULT_TODAY_TEXT),
                ),
                textScale = WidgetPrefs.getInt(context, id, CAL_NUMBER_SCALE, 100) / 100f,
            )
            views.setImageViewBitmap(R.id.grid, grid)

            val header = color(CAL_HEADER, CalendarProvider.DEFAULT_HEADER)
            views.setTextViewText(R.id.title, SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(shown.time))
            listOf(R.id.title, R.id.prev, R.id.next).forEach { views.setTextColor(it, header) }
            views.setTextViewTextSize(
                R.id.title, TypedValue.COMPLEX_UNIT_SP, WidgetPrefs.getInt(context, id, CAL_TITLE_SIZE, 14).toFloat()
            )
            views.setOnClickPendingIntent(R.id.prev, action(context, id, ACTION_PREV, 1))
            views.setOnClickPendingIntent(R.id.next, action(context, id, ACTION_NEXT, 2))
            views.setOnClickPendingIntent(R.id.title, action(context, id, ACTION_TODAY, 3))
        }

        private fun action(context: Context, id: Int, action: String, slot: Int): PendingIntent =
            PendingIntent.getBroadcast(
                context, id * 10 + slot,
                Intent(context, CombinedProvider::class.java)
                    .setAction(action)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        private fun refreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, CombinedProvider::class.java).setAction(ACTION_REFRESH),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /** Redraw just after midnight so the highlighted day moves. */
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
            val current = WidgetPrefs.getInt(context, id, CAL_OFFSET, 0)
            WidgetPrefs.setInt(context, id, CAL_OFFSET, if (delta == 0) 0 else current + delta)
            update(context, id)
        }

        private fun updateAll(context: Context) {
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, CombinedProvider::class.java))
            ids.forEach { update(context, it) }
            if (ids.isNotEmpty()) scheduleMidnight(context)
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
            ACTION_REFRESH, AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED -> updateAll(context)
        }
    }
}
