package com.parth.onewidget.render

import android.content.Context

/**
 * Picks a time text size that fits the widget, so the time can use a wrap-content box and sit
 * right above the date with the whole block centred (autosize needs a fixed-height box instead,
 * which leaves a gap).
 */
object TimeFit {
    private const val PADDING_DP = 24f
    private const val MAX_DP = 96f

    /**
     * @param extraLinesSp text sizes (sp) of the lines shown under or over the time, summed
     */
    fun timeSizeSp(context: Context, widthDp: Float, heightDp: Float, seconds: Boolean, extraLinesSp: Float): Float {
        val fontScale = context.resources.configuration.fontScale
        // Widest case: two-digit hour. A digit is about 0.58em wide, a colon 0.3em.
        val digits = if (seconds) 6 else 4
        val colons = if (seconds) 2 else 1
        val widthInEm = digits * 0.58f + colons * 0.3f

        val byWidth = (widthDp - PADDING_DP) / widthInEm
        val byHeight = (heightDp - PADDING_DP - extraLinesSp * fontScale * 1.3f) / 1.2f
        val sizeDp = minOf(byWidth, byHeight, MAX_DP).coerceAtLeast(12f)
        return sizeDp / fontScale
    }
}
