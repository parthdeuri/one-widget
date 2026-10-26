package com.parth.onewidget.providers

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.parth.onewidget.render.BackgroundRenderer
import com.parth.onewidget.R
import com.parth.onewidget.config.ClockAnalogConfigActivity
import com.parth.onewidget.prefs.TapAction
import com.parth.onewidget.prefs.WidgetPrefs
import com.parth.onewidget.render.AnalogClockRenderer
import java.util.Calendar

class ClockAnalogProvider : AppWidgetProvider() {

    companion object {
        const val KEY_FACE = "face"
        const val KEY_TICKS = "ticks"
        const val KEY_HOUR = "hour_hand"
        const val KEY_MINUTE = "minute_hand"
        const val KEY_NUMBERS = "numbers"
        const val KEY_BG = "bg"
        const val KEY_FACE_OPACITY = "face_opacity"
        const val KEY_NUMBER_SCALE = "number_scale"
        const val KEY_HAND_SCALE = "hand_scale"

        /** Transparent by default, so the clock face floats on the wallpaper as before. */
        const val DEFAULT_BG = 0x00000000

        const val DEFAULT_FACE = 0xFF212121.toInt()
        const val DEFAULT_TICKS = 0xFFBDBDBD.toInt()
        const val DEFAULT_HOUR = 0xFFFFFFFF.toInt()
        const val DEFAULT_MINUTE = 0xFF4FC3F7.toInt()

        private const val ACTION_TICK = "com.parth.onewidget.ANALOG_TICK"
        private const val BITMAP_SIZE = 400

        fun update(context: Context, id: Int) {
            val faceBase = WidgetPrefs.getColor(context, id, KEY_FACE, DEFAULT_FACE)
            val faceAlpha = Color.alpha(faceBase) *
                WidgetPrefs.getInt(context, id, KEY_FACE_OPACITY, 100).coerceIn(0, 100) / 100
            val colors = AnalogClockRenderer.Colors(
                face = (faceBase and 0x00FFFFFF) or (faceAlpha shl 24),
                ticks = WidgetPrefs.getColor(context, id, KEY_TICKS, DEFAULT_TICKS),
                hourHand = WidgetPrefs.getColor(context, id, KEY_HOUR, DEFAULT_HOUR),
                minuteHand = WidgetPrefs.getColor(context, id, KEY_MINUTE, DEFAULT_MINUTE),
            )
            val now = Calendar.getInstance()
            val bitmap = AnalogClockRenderer.render(
                BITMAP_SIZE, colors,
                WidgetPrefs.getBool(context, id, KEY_NUMBERS, false),
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE),
                numberScale = WidgetPrefs.getInt(context, id, KEY_NUMBER_SCALE, 100) / 100f,
                handScale = WidgetPrefs.getInt(context, id, KEY_HAND_SCALE, 100) / 100f,
            )

            val views = RemoteViews(context.packageName, R.layout.widget_clock_analog)
            views.setImageViewBitmap(R.id.bg, BackgroundRenderer.renderFor(context, id, DEFAULT_BG))
            views.setImageViewBitmap(R.id.face, bitmap)
            val settings = Intent(context, ClockAnalogConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            views.setOnClickPendingIntent(R.id.root, TapAction.pendingIntent(context, id, settings))
            AppWidgetManager.getInstance(context).updateAppWidget(id, views)
        }

        private fun tickIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, ClockAnalogProvider::class.java).setAction(ACTION_TICK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /** Schedules the next redraw at the top of the next minute. */
        private fun scheduleNext(context: Context) {
            val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val now = System.currentTimeMillis()
            val next = now - now % 60_000 + 60_000
            val pending = tickIntent(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()) {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC, next, pending)
            } else {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC, next, pending)
            }
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, it) }
        scheduleNext(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        update(context, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { WidgetPrefs.clear(context, it) }
    }

    override fun onDisabled(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(tickIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) {
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, ClockAnalogProvider::class.java))
            if (ids.isNotEmpty()) {
                ids.forEach { update(context, it) }
                scheduleNext(context)
            }
        }
    }
}
