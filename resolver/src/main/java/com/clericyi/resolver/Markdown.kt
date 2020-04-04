package com.clericyi.resolver

import android.text.SpannableStringBuilder

/**
 * author: ClericYi
 * time: 2020/4/3
 */

class Markdown {
    companion object{
        fun parser(fileName: String): SpannableStringBuilder{
            val parser = Parser(fileName)
            return parser.loadMD()
        }
    }
}