package com.parth.onewidget.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.text.DateFormatSymbols
import java.util.Calendar

/** Draws a weekday row plus a six-week month grid to a square bitmap. */
object CalendarRenderer {

    class Colors(
        val weekday: Int,
        val day: Int,
        val weekend: Int,
        val todayCircle: Int,
        val todayText: Int,
    )

    /**
     * @param firstOfMonth any Calendar set to the first day of the month to draw
     * @param today day of month to highlight, or 0 when the shown month is not the current one
     */
    fun render(size: Int, firstOfMonth: Calendar, today: Int, mondayFirst: Boolean, colors: Colors): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cell = size / 7f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = cell * 0.4f
        }
        val baseline = -(paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2
        val startDay = if (mondayFirst) 1 else 0 // 0 = Sunday
        val symbols = DateFormatSymbols.getInstance().shortWeekdays // index 1 = Sunday

        // Column -> weekday index (0 = Sunday); weekends are Sunday and Saturday.
        fun weekdayOf(col: Int) = (col + startDay) % 7
        fun isWeekend(col: Int) = weekdayOf(col).let { it == 0 || it == 6 }

        for (col in 0 until 7) {
            paint.color = colors.weekday
            val label = symbols[weekdayOf(col) + 1].take(1)
            canvas.drawText(label, cell * (col + 0.5f), cell * 0.5f + baseline, paint)
        }

        val firstWeekday = firstOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        val leading = (firstWeekday - startDay + 7) % 7
        val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            val index = leading + day - 1
            val col = index % 7
            val row = index / 7 + 1
            val cx = cell * (col + 0.5f)
            val cy = cell * (row + 0.5f)
            if (day == today) {
                paint.color = colors.todayCircle
                canvas.drawCircle(cx, cy, cell * 0.42f, paint)
                paint.color = colors.todayText
            } else {
                paint.color = if (isWeekend(col)) colors.weekend else colors.day
            }
            canvas.drawText(day.toString(), cx, cy + baseline, paint)
        }
        return bitmap
    }
}
