package com.parth.onewidget.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin

/** Draws an analog clock face to a square bitmap; every colour is a parameter. */
object AnalogClockRenderer {

    class Colors(val face: Int, val ticks: Int, val hourHand: Int, val minuteHand: Int)

    fun render(size: Int, colors: Colors, showNumbers: Boolean, hour: Int, minute: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val c = size / 2f
        val r = c - size * 0.01f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.style = Paint.Style.FILL
        paint.color = colors.face
        canvas.drawCircle(c, c, r, paint)

        paint.style = Paint.Style.STROKE
        paint.color = colors.ticks
        paint.strokeWidth = size * 0.015f
        canvas.drawCircle(c, c, r - paint.strokeWidth / 2, paint)

        paint.strokeCap = Paint.Cap.ROUND
        for (i in 0 until 60) {
            val isHour = i % 5 == 0
            paint.strokeWidth = size * if (isHour) 0.014f else 0.006f
            val outer = r - size * 0.04f
            val inner = outer - size * if (isHour) 0.06f else 0.03f
            canvas.save()
            canvas.rotate(i * 6f, c, c)
            canvas.drawLine(c, c - outer, c, c - inner, paint)
            canvas.restore()
        }

        if (showNumbers) {
            paint.style = Paint.Style.FILL
            paint.color = colors.ticks
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = size * 0.09f
            val centreOffset = -(paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2
            for (n in 1..12) {
                val angle = Math.toRadians(n * 30.0)
                val x = c + r * 0.68f * sin(angle).toFloat()
                val y = c - r * 0.68f * cos(angle).toFloat() + centreOffset
                canvas.drawText(n.toString(), x, y, paint)
            }
        }

        val hourAngle = ((hour % 12) + minute / 60f) * 30f
        val minuteAngle = minute * 6f
        hand(canvas, paint, c, hourAngle, r * 0.5f, size * 0.03f, colors.hourHand)
        hand(canvas, paint, c, minuteAngle, r * 0.75f, size * 0.02f, colors.minuteHand)

        paint.style = Paint.Style.FILL
        paint.color = colors.minuteHand
        canvas.drawCircle(c, c, size * 0.025f, paint)
        return bitmap
    }

    private fun hand(canvas: Canvas, paint: Paint, c: Float, angle: Float, length: Float, width: Float, color: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = width
        paint.color = color
        canvas.save()
        canvas.rotate(angle, c, c)
        canvas.drawLine(c, c + length * 0.15f, c, c - length, paint)
        canvas.restore()
    }
}
