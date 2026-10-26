package com.parth.onewidget.providers

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import com.parth.onewidget.render.BackgroundRenderer
import com.parth.onewidget.render.TimeFit
import android.widget.RemoteViews
import com.parth.onewidget.R
import com.parth.onewidget.config.ClockDigitalConfigActivity
import com.parth.onewidget.prefs.TapAction
import com.parth.onewidget.prefs.WidgetPrefs

class ClockDigitalProvider : AppWidgetProvider() {

    companion object {
        const val KEY_BG = "bg"
        const val KEY_TIME = "time_color"
        const val KEY_DATE = "date_color"
        const val KEY_ALARM = "alarm_color"
        const val KEY_24H = "h24"
        const val KEY_SECONDS = "seconds"
        const val KEY_SHOW_DATE = "show_date"
        const val KEY_SHOW_ALARM = "show_alarm"
        const val KEY_AUTO_FIT = "time_auto_fit"
        const val KEY_TIME_SIZE = "time_size"
        const val KEY_DATE_SIZE = "date_size"
        const val KEY_DATE_POSITION = "date_position"
        const val KEY_DATE_FORMAT = "date_format"

        val DATE_POSITIONS = listOf("Below the time", "Above the time")
        val DATE_FORMATS = listOf("EEE, d MMM", "EEEE, d MMMM", "d MMM yyyy", "MMM d", "dd/MM/yyyy", "yyyy-MM-dd")
        val DATE_FORMAT_LABELS = listOf("Mon, 5 Jan", "Monday, 5 January", "5 Jan 2026", "Jan 5", "05/01/2026", "2026-01-05")
        const val KEY_ALARM_SIZE = "alarm_size"

        const val DEFAULT_BG = 0xCC000000.toInt()
        const val DEFAULT_TIME = 0xFFFFFFFF.toInt()
        const val DEFAULT_DATE = 0xFFCCCCCC.toInt()
        const val DEFAULT_ALARM = 0xFFFFB74D.toInt()

        fun update(context: Context, id: Int) {
            val p = { key: String, def: Int -> WidgetPrefs.getColor(context, id, key, def) }
            val flag = { key: String, def: Boolean -> WidgetPrefs.getBool(context, id, key, def) }

            val timeFormat = (if (flag(KEY_24H, false)) "HH:mm" else "h:mm") +
                if (flag(KEY_SECONDS, false)) ":ss" else ""

            val dateAbove = p(KEY_DATE_POSITION, 0) == 1
            val views = RemoteViews(
                context.packageName,
                if (dateAbove) R.layout.widget_clock_digital_above else R.layout.widget_clock_digital
            )
            views.setImageViewBitmap(R.id.bg, BackgroundRenderer.renderFor(context, id, DEFAULT_BG))

            // Set both formats so the choice overrides the system 12/24h setting.
            // The time always uses the wrap-content box (@id/time_fixed) so the date sits right
            // against it and the block stays centred; auto-fit computes the size from the widget size.
            val showDate = flag(KEY_SHOW_DATE, true)
            val alarmShown = flag(KEY_SHOW_ALARM, true) &&
                (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock != null
            val timeSize = if (flag(KEY_AUTO_FIT, true)) {
                val (widthDp, heightDp) = BackgroundRenderer.widgetSizeDp(context, id)
                val extraLines = (if (showDate) p(KEY_DATE_SIZE, 14) else 0) +
                    (if (alarmShown) p(KEY_ALARM_SIZE, 12) else 0)
                TimeFit.timeSizeSp(context, widthDp, heightDp, flag(KEY_SECONDS, false), extraLines.toFloat())
            } else {
                p(KEY_TIME_SIZE, 48).toFloat()
            }
            views.setViewVisibility(R.id.time, View.GONE)
            views.setViewVisibility(R.id.time_fixed, View.VISIBLE)
            views.setTextViewTextSize(R.id.time_fixed, TypedValue.COMPLEX_UNIT_SP, timeSize)
            listOf(R.id.time, R.id.time_fixed).forEach {
                views.setCharSequence(it, "setFormat12Hour", timeFormat)
                views.setCharSequence(it, "setFormat24Hour", timeFormat)
                views.setTextColor(it, p(KEY_TIME, DEFAULT_TIME))
            }
            views.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_SP, p(KEY_DATE_SIZE, 14).toFloat())
            views.setTextViewTextSize(R.id.alarm, TypedValue.COMPLEX_UNIT_SP, p(KEY_ALARM_SIZE, 12).toFloat())

            views.setViewVisibility(R.id.date, if (flag(KEY_SHOW_DATE, true)) View.VISIBLE else View.GONE)
            val dateFormat = DATE_FORMATS[p(KEY_DATE_FORMAT, 0).coerceIn(0, DATE_FORMATS.lastIndex)]
            views.setCharSequence(R.id.date, "setFormat12Hour", dateFormat)
            views.setCharSequence(R.id.date, "setFormat24Hour", dateFormat)
            views.setTextColor(R.id.date, p(KEY_DATE, DEFAULT_DATE))

            val nextAlarm = if (flag(KEY_SHOW_ALARM, true)) {
                (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                    .nextAlarmClock?.triggerTime
            } else null
            if (nextAlarm != null) {
                views.setViewVisibility(R.id.alarm, View.VISIBLE)
                views.setTextViewText(R.id.alarm, "⏰ " + DateFormat.format("EEE h:mm a", nextAlarm))
                views.setTextColor(R.id.alarm, p(KEY_ALARM, DEFAULT_ALARM))
            } else {
                views.setViewVisibility(R.id.alarm, View.GONE)
            }

            val settings = Intent(context, ClockDigitalConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            views.setOnClickPendingIntent(R.id.root, TapAction.pendingIntent(context, id, settings))

            AppWidgetManager.getInstance(context).updateAppWidget(id, views)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        update(context, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetPrefs.clear(context, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            manager.getAppWidgetIds(ComponentName(context, ClockDigitalProvider::class.java))
                .forEach { update(context, it) }
        }
    }
}
