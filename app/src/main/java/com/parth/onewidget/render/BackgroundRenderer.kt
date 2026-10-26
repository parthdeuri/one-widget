package com.parth.onewidget.render

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.media.ExifInterface
import android.net.Uri
import com.parth.onewidget.prefs.WidgetPrefs
import java.io.File
import java.util.Random
import kotlin.math.max
import kotlin.math.sin

/** Setting keys shared by every widget's background. */
object Bg {
    const val KEY_BG = "bg"
    const val OPACITY = "bg_opacity"
    const val GRADIENT = "bg_gradient"
    const val COLOR2 = "bg_color2"
    const val PATTERN = "bg_pattern"
    const val PATTERN_COLOR = "bg_pattern_color"
    const val PATTERN_STRENGTH = "bg_pattern_strength"
    const val CORNER = "bg_corner"
    const val DIM = "bg_dim"

    const val DEFAULT_COLOR2 = 0xFFF8BBD0.toInt()
    const val DEFAULT_PATTERN_COLOR = 0xFFFFFFFF.toInt()

    val PATTERN_NAMES = listOf("None", "Dots", "Grid", "Stripes", "Waves", "Sparkles", "Sakura petals")

    /** Preset entries that reset the optional background extras. */
    val PLAIN = arrayOf(GRADIENT to "false", PATTERN to "0")
}

/** Draws a widget background bitmap: colour or gradient, optional image, pattern, rounded corners, opacity. */
object BackgroundRenderer {
    private const val MAX_SIDE = 720
    private const val IMAGE_MAX_SIDE = 1080

    fun imageFile(context: Context, id: Int) = File(context.filesDir, "bg_$id.jpg")

    /** Renders at the widget's current size so patterns and images are not stretched. */
    fun renderFor(context: Context, id: Int, defaultColor: Int): Bitmap {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(id)
        val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val wDp = options.getInt(
            if (landscape) AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0
        ).takeIf { it > 0 } ?: 250
        val hDp = options.getInt(
            if (landscape) AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0
        ).takeIf { it > 0 } ?: 250

        var unit = context.resources.displayMetrics.density
        var w = wDp * unit
        var h = hDp * unit
        val scale = MAX_SIDE / max(w, h)
        if (scale < 1f) {
            w *= scale
            h *= scale
            unit *= scale
        }
        return render(context, id, max(w.toInt(), 32), max(h.toInt(), 32), unit, defaultColor)
    }

    private fun render(context: Context, id: Int, w: Int, h: Int, unit: Float, defaultColor: Int): Bitmap {
        val int = { key: String, def: Int -> WidgetPrefs.getInt(context, id, key, def) }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())

        val opacity = int(Bg.OPACITY, 100).coerceIn(0, 100)
        canvas.saveLayerAlpha(rect, opacity * 255 / 100)

        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        val base = int(Bg.KEY_BG, defaultColor)
        if (WidgetPrefs.getBool(context, id, Bg.GRADIENT, false)) {
            fill.shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), base, int(Bg.COLOR2, Bg.DEFAULT_COLOR2), Shader.TileMode.CLAMP)
        } else {
            fill.color = base
        }
        canvas.drawRect(rect, fill)

        decodeImage(imageFile(context, id), w, h)?.let { image ->
            val scale = max(w / image.width.toFloat(), h / image.height.toFloat())
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate((w - image.width * scale) / 2, (h - image.height * scale) / 2)
            }
            canvas.drawBitmap(image, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
            canvas.drawColor(Color.argb(int(Bg.DIM, 30).coerceIn(0, 90) * 255 / 100, 0, 0, 0))
        }

        drawPattern(
            canvas, int(Bg.PATTERN, 0), w, h, unit,
            int(Bg.PATTERN_COLOR, Bg.DEFAULT_PATTERN_COLOR), int(Bg.PATTERN_STRENGTH, 40).coerceIn(0, 100)
        )

        // Clear everything outside the rounded rectangle, with antialiased edges.
        val radius = int(Bg.CORNER, 20) * unit
        val outside = Path().apply {
            addRoundRect(rect, radius, radius, Path.Direction.CW)
            fillType = Path.FillType.INVERSE_EVEN_ODD
        }
        canvas.drawPath(outside, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        })
        canvas.restore()
        return bitmap
    }

    private fun drawPattern(canvas: Canvas, kind: Int, w: Int, h: Int, unit: Float, color: Int, strength: Int) {
        if (kind == 0 || strength == 0) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        fun tint(factor: Float = 1f) {
            val alpha = (Color.alpha(color) * strength / 100f * factor).toInt().coerceIn(0, 255)
            paint.color = (color and 0x00FFFFFF) or (alpha shl 24)
        }
        tint()
        val step = 14f * unit
        val wf = w.toFloat()
        val hf = h.toFloat()

        when (kind) {
            1 -> { // dots
                paint.style = Paint.Style.FILL
                var y = step / 2
                while (y < hf) {
                    var x = step / 2
                    while (x < wf) { canvas.drawCircle(x, y, step * 0.12f, paint); x += step }
                    y += step
                }
            }
            2 -> { // grid
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(1f, unit * 0.8f)
                var x = 0f
                while (x < wf) { canvas.drawLine(x, 0f, x, hf, paint); x += step }
                var y = 0f
                while (y < hf) { canvas.drawLine(0f, y, wf, y, paint); y += step }
            }
            3 -> { // diagonal stripes
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(1f, unit * 1.5f)
                var x = -hf
                while (x < wf) { canvas.drawLine(x, hf, x + hf, 0f, paint); x += step }
            }
            4 -> { // waves
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(1f, unit * 1.5f)
                val spacing = step * 1.2f
                var y = spacing / 2
                while (y < hf + spacing) {
                    val path = Path().apply {
                        moveTo(0f, y)
                        var x = 0f
                        while (x <= wf) {
                            lineTo(x, y + sin(x / (step * 2f) * 6.2831855f) * step * 0.3f)
                            x += 4f
                        }
                    }
                    canvas.drawPath(path, paint)
                    y += spacing
                }
            }
            5 -> { // sparkles
                paint.style = Paint.Style.FILL
                val random = Random(11)
                val count = (wf * hf / (step * step * 3)).toInt().coerceIn(6, 70)
                repeat(count) {
                    val cx = random.nextFloat() * wf
                    val cy = random.nextFloat() * hf
                    val r = step * (0.2f + random.nextFloat() * 0.45f)
                    tint(0.4f + random.nextFloat() * 0.6f)
                    val star = Path().apply {
                        moveTo(cx, cy - r)
                        quadTo(cx, cy, cx + r, cy)
                        quadTo(cx, cy, cx, cy + r)
                        quadTo(cx, cy, cx - r, cy)
                        quadTo(cx, cy, cx, cy - r)
                    }
                    canvas.drawPath(star, paint)
                }
            }
            6 -> { // sakura petals
                paint.style = Paint.Style.FILL
                val random = Random(11)
                val big = 24f * unit
                val count = (wf * hf / (big * big * 3)).toInt().coerceIn(5, 40)
                repeat(count) {
                    val cx = random.nextFloat() * wf
                    val cy = random.nextFloat() * hf
                    val r = big * (0.35f + random.nextFloat() * 0.45f)
                    tint(0.5f + random.nextFloat() * 0.5f)
                    canvas.save()
                    canvas.rotate(random.nextFloat() * 360f, cx, cy)
                    if (random.nextInt(3) == 0) {
                        // a single fallen petal
                        canvas.drawOval(RectF(cx - r * 0.3f, cy - r * 0.6f, cx + r * 0.3f, cy + r * 0.6f), paint)
                    } else {
                        for (k in 0 until 5) {
                            canvas.save()
                            canvas.rotate(k * 72f, cx, cy)
                            canvas.drawOval(RectF(cx - r * 0.32f, cy - r, cx + r * 0.32f, cy), paint)
                            canvas.restore()
                        }
                    }
                    canvas.restore()
                }
            }
        }
    }

    private fun decodeImage(file: File, w: Int, h: Int): Bitmap? {
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= w && bounds.outHeight / (sample * 2) >= h) sample *= 2
        return BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    /** Copies the picked image into app storage, downscaled and rotated upright. */
    fun importImage(context: Context, id: Int, uri: Uri): Boolean = try {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= IMAGE_MAX_SIDE) sample *= 2
        var image = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return false

        val degrees = resolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        if (degrees != 0f) {
            image = Bitmap.createBitmap(image, 0, 0, image.width, image.height, Matrix().apply { postRotate(degrees) }, true)
        }
        imageFile(context, id).outputStream().use { image.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        true
    } catch (e: Exception) {
        false
    }
}
