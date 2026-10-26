package com.parth.onewidget.config

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.graphics.Color
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.parth.onewidget.prefs.WidgetPrefs
import com.parth.onewidget.render.BackgroundRenderer
import com.parth.onewidget.render.Bg

/** Base for widget settings screens: set [widgetId], then add rows to [content]. */
open class ConfigActivity : Activity() {
    protected var widgetId = 0
    protected lateinit var content: LinearLayout

    private var appSubtitle: TextView? = null
    private var imageSubtitle: TextView? = null

    private companion object {
        const val REQ_APP = 1
        const val REQ_IMAGE = 2
    }

    protected fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }
        setContentView(ScrollView(this).apply { addView(content) })
    }

    protected fun addColorRow(label: String, key: String, default: Int) {
        val swatch = View(this).apply {
            setBackgroundColor(WidgetPrefs.getColor(this@ConfigActivity, widgetId, key, default))
        }
        val row = row(label, null, swatch)
        row.setOnClickListener {
            val initial = WidgetPrefs.getColor(this, widgetId, key, default)
            ColorPickerDialog.show(this, initial) { picked ->
                WidgetPrefs.setColor(this, widgetId, key, picked)
                swatch.setBackgroundColor(picked)
            }
        }
        content.addView(row)
    }

    /** Values are "#AARRGGBB" colours, "true"/"false" flags, or plain integers. */
    protected class Preset(val name: String, val values: Map<String, String>)

    protected fun preset(name: String, vararg values: Pair<String, String>) = Preset(name, values.toMap())

    /** A scrolling row of buttons; each applies its values, then reloads the screen. */
    protected fun addPresetRow(presets: List<Preset>) {
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        presets.forEach { preset ->
            buttons.addView(Button(this).apply {
                text = preset.name
                setOnClickListener {
                    preset.values.forEach { (key, value) ->
                        when {
                            value.startsWith("#") -> WidgetPrefs.setColor(this@ConfigActivity, widgetId, key, Color.parseColor(value))
                            value == "true" || value == "false" -> WidgetPrefs.setBool(this@ConfigActivity, widgetId, key, value.toBoolean())
                            else -> WidgetPrefs.setInt(this@ConfigActivity, widgetId, key, value.toInt())
                        }
                    }
                    recreate()
                }
            })
        }
        content.addView(TextView(this).apply { text = "Theme presets"; textSize = 16f })
        content.addView(HorizontalScrollView(this).apply { addView(buttons) })
    }

    protected fun addSliderRow(label: String, key: String, min: Int, max: Int, default: Int, suffix: String = "") {
        val title = TextView(this).apply { textSize = 16f }
        val current = WidgetPrefs.getInt(this, widgetId, key, default).coerceIn(min, max)
        fun show(value: Int) { title.text = "$label: $value$suffix" }
        show(current)
        val bar = SeekBar(this).apply {
            this.max = max - min
            progress = current - min
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, progress: Int, fromUser: Boolean) {
                    show(progress + min)
                    if (fromUser) WidgetPrefs.setInt(this@ConfigActivity, widgetId, key, progress + min)
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {}
            })
        }
        content.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            addView(title)
            addView(bar)
        })
    }

    protected fun addChoiceRow(label: String, key: String, options: List<String>, default: Int) {
        val subtitle = TextView(this).apply {
            textSize = 13f
            text = options.getOrElse(WidgetPrefs.getInt(this@ConfigActivity, widgetId, key, default)) { options[0] }
        }
        val row = row(label, subtitle, null)
        row.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(label)
                .setItems(options.toTypedArray()) { _, which ->
                    WidgetPrefs.setInt(this, widgetId, key, which)
                    subtitle.text = options[which]
                }
                .show()
        }
        content.addView(row)
    }

    private fun addImageRow() {
        val subtitle = TextView(this).apply { textSize = 13f }
        imageSubtitle = subtitle
        showImageState(subtitle)
        val row = row("Background image", subtitle, null)
        row.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Background image")
                .setItems(arrayOf("Choose image", "Remove image")) { _, which ->
                    if (which == 0) {
                        val pick = Intent(Intent.ACTION_OPEN_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType("image/*")
                        @Suppress("DEPRECATION")
                        startActivityForResult(pick, REQ_IMAGE)
                    } else {
                        BackgroundRenderer.imageFile(this, widgetId).delete()
                        showImageState(subtitle)
                    }
                }
                .show()
        }
        content.addView(row)
    }

    private fun showImageState(view: TextView) {
        view.text = if (BackgroundRenderer.imageFile(this, widgetId).exists()) "Image set" else "None"
    }

    /** Colour, gradient, opacity, corners, pattern and image: shared by every widget. */
    protected fun addBackgroundSection(defaultColor: Int) {
        content.addView(TextView(this).apply {
            text = "Background"
            textSize = 20f
            setPadding(0, dp(20), 0, 0)
        })
        addColorRow("Background colour", Bg.KEY_BG, defaultColor)
        addSwitchRow("Gradient background", Bg.GRADIENT, false)
        addColorRow("Gradient end colour", Bg.COLOR2, Bg.DEFAULT_COLOR2)
        addSliderRow("Opacity", Bg.OPACITY, 0, 100, 100, "%")
        addSliderRow("Corner roundness", Bg.CORNER, 0, 50, 20, "dp")
        addChoiceRow("Pattern", Bg.PATTERN, Bg.PATTERN_NAMES, 0)
        addColorRow("Pattern colour", Bg.PATTERN_COLOR, Bg.DEFAULT_PATTERN_COLOR)
        addSliderRow("Pattern strength", Bg.PATTERN_STRENGTH, 0, 100, 40, "%")
        addImageRow()
        addSliderRow("Image dimming", Bg.DIM, 0, 90, 30, "%")
    }

    protected fun addSwitchRow(label: String, key: String, default: Boolean) {
        content.addView(Switch(this).apply {
            text = label
            textSize = 16f
            setPadding(0, dp(12), 0, dp(12))
            isChecked = WidgetPrefs.getBool(this@ConfigActivity, widgetId, key, default)
            setOnCheckedChangeListener { _, checked ->
                WidgetPrefs.setBool(this@ConfigActivity, widgetId, key, checked)
            }
        })
    }

    protected fun addButton(label: String, onClick: () -> Unit) {
        content.addView(Button(this).apply {
            text = label
            setOnClickListener { onClick() }
        })
    }

    protected fun addAppRow(label: String) {
        val subtitle = TextView(this).apply { textSize = 13f }
        appSubtitle = subtitle
        showTapApp(subtitle)
        val row = row(label, subtitle, null)
        row.setOnClickListener {
            @Suppress("DEPRECATION")
            startActivityForResult(Intent(this, AppPickerActivity::class.java), REQ_APP)
        }
        content.addView(row)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_APP && resultCode == RESULT_OK) {
            val pkg = data?.getStringExtra(AppPickerActivity.EXTRA_PACKAGE).orEmpty()
            WidgetPrefs.setString(this, widgetId, WidgetPrefs.KEY_TAP_PACKAGE, pkg)
            appSubtitle?.let { showTapApp(it) }
        }
        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri == null || !BackgroundRenderer.importImage(this, widgetId, uri)) {
                Toast.makeText(this, "Could not use that image", Toast.LENGTH_SHORT).show()
            }
            imageSubtitle?.let { showImageState(it) }
        }
    }

    private fun showTapApp(view: TextView) {
        val pkg = WidgetPrefs.getString(this, widgetId, WidgetPrefs.KEY_TAP_PACKAGE)
        view.text = if (pkg.isNullOrEmpty()) "None (opens widget settings)" else try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0))
        } catch (e: PackageManager.NameNotFoundException) {
            pkg
        }
    }

    private fun row(label: String, subtitle: TextView?, trailing: View?): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }
        val text = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        text.addView(TextView(this).apply { this.text = label; textSize = 16f })
        subtitle?.let { text.addView(it) }
        row.addView(text, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        trailing?.let { row.addView(it, LinearLayout.LayoutParams(dp(40), dp(40))) }
        return row
    }
}
