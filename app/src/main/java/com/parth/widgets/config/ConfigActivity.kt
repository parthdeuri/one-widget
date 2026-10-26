package com.parth.widgets.config

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import com.parth.widgets.prefs.WidgetPrefs

/** Base for widget settings screens: set [widgetId], then add rows to [content]. */
open class ConfigActivity : Activity() {
    protected var widgetId = 0
    protected lateinit var content: LinearLayout

    private var appSubtitle: TextView? = null

    private companion object {
        const val REQ_APP = 1
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
