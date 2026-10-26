package com.parth.onewidget.config

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.parth.onewidget.R
import com.parth.onewidget.prefs.WidgetPrefs
import com.parth.onewidget.render.BackgroundRenderer
import com.parth.onewidget.render.Bg

/** Base for widget settings screens: set [widgetId], then add rows to [content]. */
open class ConfigActivity : Activity() {
    protected var widgetId = 0
    protected lateinit var content: LinearLayout

    private val appSubtitles = mutableMapOf<String, TextView>()
    private var pendingAppKey = WidgetPrefs.KEY_TAP_PACKAGE
    private var imageSubtitle: TextView? = null

    private companion object {
        const val REQ_APP = 1
        const val REQ_IMAGE = 2
    }

    protected fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    protected open val screenTitle: String get() = "Widget settings"
    protected open val screenSubtitle: String get() = "Make it yours: colours, background and tap action"
    protected open val showLogo: Boolean get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(banner())
            addView(content)
        }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    /** Pink gradient header with blossoms, the screen title and, on the home screen, the logo. */
    private fun banner(): View {
        val frame = FrameLayout(this).apply { background = getDrawable(R.drawable.header_gradient) }
        frame.addView(
            ImageView(this).apply { setImageResource(R.drawable.deco_sakura) },
            FrameLayout.LayoutParams(dp(220), dp(110), Gravity.END or Gravity.TOP)
        )
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }
        if (showLogo) {
            row.addView(ImageView(this).apply {
                setImageResource(R.drawable.ic_logo)
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(0, 0, view.width, view.height, dp(16).toFloat())
                    }
                }
                clipToOutline = true
            }, LinearLayout.LayoutParams(dp(64), dp(64)).apply { rightMargin = dp(16) })
        }
        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(TextView(this).apply {
            text = screenTitle
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.sakura_deep))
        })
        texts.addView(TextView(this).apply {
            text = screenSubtitle
            textSize = 14f
            setTextColor(getColor(R.color.sakura_subtext))
        })
        row.addView(texts)
        frame.addView(row)
        return frame
    }

    /** Puts a row inside a rounded white card. */
    protected fun addCard(view: View) {
        view.background = getDrawable(R.drawable.card_bg)
        view.setPadding(dp(16), view.paddingTop, dp(16), view.paddingBottom)
        content.addView(view, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(8) })
    }

    protected fun addSectionTitle(text: String) {
        content.addView(TextView(this).apply {
            this.text = text
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.sakura_deep))
            setPadding(dp(4), dp(16), 0, dp(8))
        })
    }

    private fun swatchBackground(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(10).toFloat()
        setStroke(dp(1), getColor(R.color.sakura_line))
    }

    /** A tappable row with a title, optional subtitle and a chevron; used for lists of destinations. */
    protected fun addNavRow(title: String, subtitle: String, onClick: () -> Unit) {
        val sub = TextView(this).apply {
            textSize = 13f
            setTextColor(getColor(R.color.sakura_subtext))
            text = subtitle
        }
        val chevron = TextView(this).apply {
            text = "›"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(getColor(R.color.sakura_primary))
        }
        val row = row(title, sub, chevron)
        row.setOnClickListener { onClick() }
        addCard(row)
    }

    protected fun addColorRow(label: String, key: String, default: Int) {
        val swatch = View(this).apply {
            background = swatchBackground(WidgetPrefs.getColor(this@ConfigActivity, widgetId, key, default))
        }
        val row = row(label, null, swatch)
        row.setOnClickListener {
            val initial = WidgetPrefs.getColor(this, widgetId, key, default)
            ColorPickerDialog.show(this, initial) { picked ->
                WidgetPrefs.setColor(this, widgetId, key, picked)
                swatch.background = swatchBackground(picked)
            }
        }
        addCard(row)
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
                isAllCaps = false
                background = getDrawable(R.drawable.btn_soft)
                setTextColor(getColor(R.color.sakura_deep))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(44)
                ).apply { rightMargin = dp(8) }
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
        addSectionTitle("Theme presets")
        content.addView(HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(buttons)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(16) })
    }

    /** One shared colour theme; each widget maps these roles onto its own setting keys. */
    protected class ThemeSpec(
        val name: String,
        val bg: String, val bg2: String?,
        val text: String, val sub: String, val accent: String,
        val highlight: String, val onHighlight: String, val weekend: String,
        val pattern: Int = 0, val patternColor: String = "#FFFFFFFF", val strength: Int = 40,
    )

    private val themeSpecs = listOf(
        ThemeSpec("Dark", "#E6141418", null, "#FFF5F5F7", "#FFB0B3B8", "#FFFFB74D",
            "#FF64B5F6", "#FF0B0B0F", "#FFFF8A80"),
        ThemeSpec("Light", "#F2FAFAFA", null, "#FF1F1F24", "#FF5F6368", "#FFD84315",
            "#FF1976D2", "#FFFFFFFF", "#FFD32F2F"),
        ThemeSpec("Midnight", "#F20D1B3D", "#F2311B5B", "#FFE8EAF6", "#FF9FA8DA", "#FFFFD54F",
            "#FF7C4DFF", "#FFFFFFFF", "#FFFF8A80", pattern = 5, strength = 45),
        ThemeSpec("Ocean", "#F20277BD", "#F200ACC1", "#FFFFFFFF", "#FFE1F5FE", "#FFFFF176",
            "#FFFFFFFF", "#FF01579B", "#FFFFE082", pattern = 4, strength = 25),
        ThemeSpec("Sunset", "#F2FF7043", "#F2C2185B", "#FFFFFFFF", "#FFFFE0B2", "#FFFFEB3B",
            "#FFFFFFFF", "#FFBF360C", "#FFFFF59D", pattern = 1, strength = 20),
        ThemeSpec("Forest", "#F22E7D32", "#F2004D40", "#FFF1F8E9", "#FFC5E1A5", "#FFFFF176",
            "#FFAED581", "#FF1B5E20", "#FFFFCC80", pattern = 3, strength = 12),
        ThemeSpec("Lavender", "#F2EDE7F6", "#F2D1C4E9", "#FF311B92", "#FF5E35B1", "#FFAD1457",
            "#FF7E57C2", "#FFFFFFFF", "#FFC2185B", pattern = 1, patternColor = "#FFB39DDB", strength = 50),
        ThemeSpec("Cherry blossom", "#FFFFE4EC", "#FFF8BBD0", "#FF880E4F", "#FFAD1457", "#FFC2185B",
            "#FFEC407A", "#FFFFFFFF", "#FFD81B60", pattern = 6, patternColor = "#FFF06292", strength = 55),
    )

    /**
     * Theme buttons. [colors] maps a theme onto this widget's colour keys; [clearBackground] is
     * for widgets with no card behind them (the analog clock).
     */
    protected fun addThemeRow(clearBackground: Boolean = false, colors: (ThemeSpec) -> Array<Pair<String, String>>) {
        val presets = mutableListOf<Preset>()
        themeSpecs.forEach { spec ->
            val values = linkedMapOf(
                Bg.KEY_BG to spec.bg,
                Bg.GRADIENT to (spec.bg2 != null).toString(),
                Bg.COLOR2 to (spec.bg2 ?: spec.bg),
                Bg.PATTERN to spec.pattern.toString(),
                Bg.PATTERN_COLOR to spec.patternColor,
                Bg.PATTERN_STRENGTH to spec.strength.toString(),
            )
            values.putAll(colors(spec))
            if (clearBackground) {
                values[Bg.KEY_BG] = "#00000000"
                values[Bg.GRADIENT] = "false"
                values[Bg.PATTERN] = "0"
            }
            presets += Preset(spec.name, values)
        }
        addPresetRow(presets)
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
        addCard(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            addView(title)
            addView(bar)
        })
    }

    protected fun addChoiceRow(label: String, key: String, options: List<String>, default: Int) {
        val subtitle = TextView(this).apply {
            textSize = 13f; setTextColor(getColor(R.color.sakura_subtext))
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
        addCard(row)
    }

    private fun addImageRow() {
        val subtitle = TextView(this).apply { textSize = 13f; setTextColor(getColor(R.color.sakura_subtext)) }
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
        addCard(row)
    }

    private fun showImageState(view: TextView) {
        view.text = if (BackgroundRenderer.imageFile(this, widgetId).exists()) "Image set" else "None"
    }

    /** Colour, gradient, opacity, corners, pattern and image: shared by every widget. */
    protected fun addBackgroundSection(defaultColor: Int) {
        addSectionTitle("Background")
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
        addCard(Switch(this).apply {
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
            isAllCaps = false
            textSize = 16f
            setTextColor(Color.WHITE)
            background = getDrawable(R.drawable.btn_primary)
            setOnClickListener { onClick() }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
        ).apply { topMargin = dp(8); bottomMargin = dp(4) })
    }

    protected fun addTextRow(label: String, key: String, default: String) {
        val subtitle = TextView(this).apply {
            textSize = 13f; setTextColor(getColor(R.color.sakura_subtext))
            text = WidgetPrefs.getString(this@ConfigActivity, widgetId, key) ?: default
        }
        val row = row(label, subtitle, null)
        row.setOnClickListener {
            val input = EditText(this).apply {
                setText(subtitle.text)
                setSingleLine()
                setSelection(text.length)
            }
            AlertDialog.Builder(this)
                .setTitle(label)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val value = input.text.toString().trim()
                    WidgetPrefs.setString(this, widgetId, key, value)
                    subtitle.text = value.ifEmpty { default }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        addCard(row)
    }

    /** Stores the picked date as "yyyy-MM-dd". */
    protected fun addDateRow(label: String, key: String) {
        val subtitle = TextView(this).apply {
            textSize = 13f; setTextColor(getColor(R.color.sakura_subtext))
            text = WidgetPrefs.getString(this@ConfigActivity, widgetId, key) ?: "Not set"
        }
        val row = row(label, subtitle, null)
        row.setOnClickListener {
            val saved = WidgetPrefs.getString(this, widgetId, key)?.split("-")?.mapNotNull { it.toIntOrNull() }
            val today = java.util.Calendar.getInstance()
            val (y, m, d) = if (saved != null && saved.size == 3) {
                Triple(saved[0], saved[1] - 1, saved[2])
            } else {
                Triple(today.get(java.util.Calendar.YEAR), today.get(java.util.Calendar.MONTH), today.get(java.util.Calendar.DAY_OF_MONTH))
            }
            DatePickerDialog(this, { _, year, month, day ->
                val value = String.format("%04d-%02d-%02d", year, month + 1, day)
                WidgetPrefs.setString(this, widgetId, key, value)
                subtitle.text = value
            }, y, m, d).show()
        }
        addCard(row)
    }

    protected fun addAppRow(label: String, key: String = WidgetPrefs.KEY_TAP_PACKAGE) {
        val subtitle = TextView(this).apply { textSize = 13f; setTextColor(getColor(R.color.sakura_subtext)) }
        appSubtitles[key] = subtitle
        showTapApp(subtitle, key)
        val row = row(label, subtitle, null)
        row.setOnClickListener {
            pendingAppKey = key
            @Suppress("DEPRECATION")
            startActivityForResult(Intent(this, AppPickerActivity::class.java), REQ_APP)
        }
        addCard(row)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_APP && resultCode == RESULT_OK) {
            val pkg = data?.getStringExtra(AppPickerActivity.EXTRA_PACKAGE).orEmpty()
            WidgetPrefs.setString(this, widgetId, pendingAppKey, pkg)
            appSubtitles[pendingAppKey]?.let { showTapApp(it, pendingAppKey) }
        }
        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri == null || !BackgroundRenderer.importImage(this, widgetId, uri)) {
                Toast.makeText(this, "Could not use that image", Toast.LENGTH_SHORT).show()
            }
            imageSubtitle?.let { showImageState(it) }
        }
    }

    private fun showTapApp(view: TextView, key: String) {
        val pkg = WidgetPrefs.getString(this, widgetId, key)
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
