package com.clericyi.resolver

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.style.*

/**
 * author: ClericYi
 * time: 2020/4/2
 */
class Styles {
    companion object {
        private val BLUE = Color.parseColor("#000066")

        private const val h1Size = 40
        private const val h2Size = 34
        private const val h3Size = 28
        private const val h4Size = 22

        val linkSpan = SpanBean(ForegroundColorSpan(BLUE), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val underLineSpan = SpanBean(UnderlineSpan(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val h1Span = SpanBean(AbsoluteSizeSpan(h1Size, true), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val h2Span = SpanBean(AbsoluteSizeSpan(h2Size, true), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val h3Span = SpanBean(AbsoluteSizeSpan(h3Size, true), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val h4Span = SpanBean(AbsoluteSizeSpan(h4Size, true), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        // 三种字体
        val boldSpan = SpanBean(StyleSpan(Typeface.BOLD),Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val italicSpan = SpanBean(StyleSpan(Typeface.ITALIC),Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        val boldAndItalicSpan = SpanBean(StyleSpan(Typeface.BOLD_ITALIC),Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}