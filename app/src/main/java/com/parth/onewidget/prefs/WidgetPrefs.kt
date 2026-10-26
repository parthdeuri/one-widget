package com.parth.onewidget.prefs

import android.content.Context

/** Per-widget settings, stored as "<appWidgetId>_<name>" in one SharedPreferences file. */
object WidgetPrefs {
    const val KEY_TAP_PACKAGE = "tap_pkg"

    private fun sp(c: Context) = c.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)

    fun getColor(c: Context, id: Int, key: String, def: Int): Int = sp(c).getInt("${id}_$key", def)

    fun setColor(c: Context, id: Int, key: String, value: Int) {
        sp(c).edit().putInt("${id}_$key", value).apply()
    }

    fun getInt(c: Context, id: Int, key: String, def: Int): Int = getColor(c, id, key, def)

    fun setInt(c: Context, id: Int, key: String, value: Int) = setColor(c, id, key, value)

    fun getString(c: Context, id: Int, key: String): String? = sp(c).getString("${id}_$key", null)

    fun setString(c: Context, id: Int, key: String, value: String?) {
        sp(c).edit().putString("${id}_$key", value).apply()
    }

    fun getBool(c: Context, id: Int, key: String, def: Boolean): Boolean =
        sp(c).getBoolean("${id}_$key", def)

    fun setBool(c: Context, id: Int, key: String, value: Boolean) {
        sp(c).edit().putBoolean("${id}_$key", value).apply()
    }

    fun clear(c: Context, id: Int) {
        val edit = sp(c).edit()
        sp(c).all.keys.filter { it.startsWith("${id}_") }.forEach { edit.remove(it) }
        edit.apply()
    }
}
