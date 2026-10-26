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

            val views = RemoteViews(context.packageName, R.layout.widget_clock_digital)
            views.setImageViewBitmap(R.id.bg, BackgroundRenderer.renderFor(context, id, DEFAULT_BG))

            // Set both formats so the choice overrides the system 12/24h setting.
            val autoFit = flag(KEY_AUTO_FIT, true)
            views.setViewVisibility(R.id.time, if (autoFit) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.time_fixed, if (autoFit) View.GONE else View.VISIBLE)
            views.setTextViewTextSize(
                R.id.time_fixed, TypedValue.COMPLEX_UNIT_SP, p(KEY_TIME_SIZE, 48).toFloat()
            )
            listOf(R.id.time, R.id.time_fixed).forEach {
                views.setCharSequence(it, "setFormat12Hour", timeFormat)
                views.setCharSequence(it, "setFormat24Hour", timeFormat)
                views.setTextColor(it, p(KEY_TIME, DEFAULT_TIME))
            }
            views.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_SP, p(KEY_DATE_SIZE, 14).toFloat())
            views.setTextViewTextSize(R.id.alarm, TypedValue.COMPLEX_UNIT_SP, p(KEY_ALARM_SIZE, 12).toFloat())

            views.setViewVisibility(R.id.date, if (flag(KEY_SHOW_DATE, true)) View.VISIBLE else View.GONE)
            views.setCharSequence(R.id.date, "setFormat12Hour", "EEE, d MMM")
            views.setCharSequence(R.id.date, "setFormat24Hour", "EEE, d MMM")
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
