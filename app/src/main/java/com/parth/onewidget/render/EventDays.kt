package com.parth.onewidget.render

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import java.util.Calendar
import java.util.TimeZone

/** Days of a month that have at least one calendar event. Empty without the Calendar permission. */
object EventDays {
    fun forMonth(context: Context, firstOfMonth: Calendar): Set<Int> {
        if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return emptySet()
        }
        val year = firstOfMonth.get(Calendar.YEAR)
        val month = firstOfMonth.get(Calendar.MONTH)
        val start = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, start.timeInMillis)
        ContentUris.appendId(builder, end.timeInMillis)

        val days = mutableSetOf<Int>()
        try {
            val projection = arrayOf(
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
            )
            context.contentResolver.query(builder.build(), projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val begin = cursor.getLong(0)
                    val finish = cursor.getLong(1)
                    // All-day events are stored in UTC; timed events in local time.
                    val zone = if (cursor.getInt(2) == 1) TimeZone.getTimeZone("UTC") else TimeZone.getDefault()
                    val day = Calendar.getInstance(zone).apply { timeInMillis = begin }
                    val last = Calendar.getInstance(zone).apply { timeInMillis = maxOf(begin, finish - 1) }
                    var guard = 0
                    while (!day.after(last) && guard++ < 62) {
                        if (day.get(Calendar.YEAR) == year && day.get(Calendar.MONTH) == month) {
                            days += day.get(Calendar.DAY_OF_MONTH)
                        }
                        day.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission revoked mid-query: show no dots.
        }
        return days
    }
}
