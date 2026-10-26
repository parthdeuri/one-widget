package com.parth.onewidget.config

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

/** HSV + alpha sliders with a live swatch and an editable #AARRGGBB field. */
object ColorPickerDialog {
    fun show(context: Context, initial: Int, onPick: (Int) -> Unit) {
        val density = context.resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val hsv = FloatArray(3)
        Color.colorToHSV(initial, hsv)
        var alpha = Color.alpha(initial)
        var updating = false

        fun current() = Color.HSVToColor(alpha, hsv)

        val swatch = View(context)
        val hex = EditText(context).apply {
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            filters = arrayOf(InputFilter.LengthFilter(9))
        }
        val syncs = mutableListOf<() -> Unit>()

        fun syncAll(fromHex: Boolean) {
            updating = true
            swatch.background = GradientDrawable().apply {
                setColor(current())
                cornerRadius = dp(12).toFloat()
                setStroke(dp(1), 0xFFF8BBD0.toInt())
            }
            if (fromHex) syncs.forEach { it() } else hex.setText(String.format("#%08X", current()))
            updating = false
        }

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), 0)
        }
        root.addView(swatch, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)))
        root.addView(hex)

        fun addBar(label: String, max: Int, read: () -> Int, write: (Int) -> Unit) {
            root.addView(TextView(context).apply { text = label })
            val bar = SeekBar(context).apply {
                this.max = max
                progress = read()
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                        if (fromUser) { write(p); syncAll(false) }
                    }
                    override fun onStartTrackingTouch(s: SeekBar) {}
                    override fun onStopTrackingTouch(s: SeekBar) {}
                })
            }
            syncs += { bar.progress = read() }
            root.addView(bar)
        }

        addBar("Hue", 360, { hsv[0].toInt() }, { hsv[0] = it.toFloat() })
        addBar("Saturation", 100, { (hsv[1] * 100).toInt() }, { hsv[1] = it / 100f })
        addBar("Brightness", 100, { (hsv[2] * 100).toInt() }, { hsv[2] = it / 100f })
        addBar("Opacity", 255, { alpha }, { alpha = it })

        hex.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (updating) return
                val parsed = try { Color.parseColor(s.toString()) } catch (e: IllegalArgumentException) { return }
                Color.colorToHSV(parsed, hsv)
                alpha = Color.alpha(parsed)
                syncAll(true)
            }
        })

        syncAll(false)

        AlertDialog.Builder(context)
            .setTitle("Pick colour")
            .setView(ScrollView(context).apply { addView(root) })
            .setPositiveButton(android.R.string.ok) { _, _ -> onPick(current()) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
