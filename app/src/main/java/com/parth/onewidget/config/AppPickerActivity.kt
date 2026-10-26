package com.parth.onewidget.config

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.ColorDrawable
import com.parth.onewidget.R
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

/** Lists launchable apps; returns the chosen package in [EXTRA_PACKAGE] ("" means none). */
class AppPickerActivity : Activity() {
    companion object {
        const val EXTRA_PACKAGE = "package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Choose app"

        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps: List<ResolveInfo> = packageManager.queryIntentActivities(launcher, 0)
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }

        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        // Position 0 is the "no app" entry.
        val adapter = object : BaseAdapter() {
            override fun getCount() = apps.size + 1
            override fun getItem(position: Int) = if (position == 0) null else apps[position - 1]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val row = LinearLayout(this@AppPickerActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(16), dp(10), dp(16), dp(10))
                }
                val info = getItem(position)
                if (info != null) {
                    row.addView(ImageView(this@AppPickerActivity).apply {
                        setImageDrawable(info.loadIcon(packageManager))
                    }, LinearLayout.LayoutParams(dp(40), dp(40)))
                }
                row.addView(TextView(this@AppPickerActivity).apply {
                    text = info?.loadLabel(packageManager) ?: "None (open widget settings)"
                    textSize = 16f
                    setPadding(dp(16), 0, 0, 0)
                })
                return row
            }
        }

        setContentView(ListView(this).apply {
            this.adapter = adapter
            setBackgroundColor(getColor(R.color.sakura_bg))
            divider = ColorDrawable(getColor(R.color.sakura_line))
            dividerHeight = 1
            setOnItemClickListener { _, _, position, _ ->
                val pkg = apps.getOrNull(position - 1)?.activityInfo?.packageName ?: ""
                setResult(RESULT_OK, Intent().putExtra(EXTRA_PACKAGE, pkg))
                finish()
            }
        })
    }
}
