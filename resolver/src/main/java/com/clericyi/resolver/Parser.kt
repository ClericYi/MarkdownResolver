package com.clericyi.resolver

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import android.util.Log
import androidx.core.text.set
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * author: ClericYi
 * time: 2020/4/1
 */

class Parser(fileName: String) {
    // 整行定义
    private val TEXT: Short = 1
    private val CODE: Short = 2
    private val QUOTE: Short = 3
    private val UNORDER: Short = 4
    private val ORDER: Short = 5
    private val H1: Short = 6
    private val H2: Short = 7
    private val H3: Short = 8
    private val H4: Short = 9

    // 行内定义
    private val TEXT_INLINE: Short = 20
    private val IMAGE: Short = 21
    private val BOLD: Short = 22
    private val ITALIC: Short = 23
    private val BOLD_ITALIC: Short = 24
    private val LINK_NAME: Short = 25
    private val LINK_PATH: Short = 26
    private val CODE_INLINE: Short = 27


    // 存放 md 的数据
    private var mdList: LinkedList<String> = LinkedList()
    // 存放 md 每行的数据的样式
    private var mdListType: LinkedList<Short> = LinkedList()

    private var mdLineList: LinkedList<LinkedList<String>> = LinkedList()
    private var mdLineTypeList: LinkedList<LinkedList<Short>> = LinkedList()

    private val dealSameStack: Stack<Short> = Stack()

    private var mSpannableStyle = SpannableStringBuilder()

    init {
        readMarkdownFile(fileName)
    }

    private fun readMarkdownFile(fileName: String): Boolean {
        return try {
            mdList.clear()
            mdListType.clear()
            val fis = FileInputStream(fileName)
            val dis = InputStreamReader(fis, "UTF-8")
            val mdFile = BufferedReader(dis)

            // 读取 markdown 文件
            var mdLine = mdFile.readLine()
            while (mdLine != null) {
                if (mdLine.isNotEmpty()) {
                    mdList.add(mdLine)
                }
                mdLine = mdFile.readLine()
            }
            mdFile.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun loadMD():SpannableStringBuilder {
        defineAreaType()
        translateLineToText()
        translateTextInline()
        return mSpannableStyle
    }

    /**
     * 先把Markdown整体的数据进行划分
     *
     */
    private fun defineAreaType() {
        // 小分区时的临时数据保存
        val tempList = LinkedList<String>()
        val tempType = LinkedList<Short>()

        // 对每一层的md语法进行分析
        // 但是先对代码块进行分析，因为代码块内不允许其他的样式存在
        var codeBlock = false
        for (i in mdList.indices) {
            val line = mdList[i]
            if (line.length > 2 && line[0] == '`' && line[1] == '`' && line[2] == '`') {
                // 进入代码区
                if (!codeBlock) {
                    // 代码块开始位置
                    tempType.add(CODE)
                    tempList.add("")
                    codeBlock = true
                } else if (codeBlock) {
                    // 代码块结束位置
                    tempType.add(CODE)
                    tempList.add("")
                    codeBlock = false
                } else {
                    tempType.add(TEXT)
                    tempList.add(line)
                }
            } else {
                tempType.add(TEXT)
                tempList.add(line)
            }
        }

        mdList = tempList.clone() as LinkedList<String>
        mdListType = tempType.clone() as LinkedList<Short>
        tempList.clear()
        tempType.clear()
        // 定位其他区，注意代码区内无其他格式
        var isCodeArea = false
        for (i in mdList.indices) {
            val line = mdList[i]
            // 对代码块的判断
            if (mdListType[i] == CODE && !isCodeArea) {
                isCodeArea = true
                tempList.add(line)
                tempType.add(CODE)
                continue
            }
            if (mdListType[i] == CODE && isCodeArea) {
                isCodeArea = false
                tempList.add(line)
                tempType.add(CODE)
                continue
            }
            // 代码区不含其他格式
            if (!isCodeArea) {
                // 进入引用区
                if (line.length > 2 && line[0] == '>') {
                    tempList.add(line)
                    tempType.add(QUOTE)
                }
                // 进入无序列表区
                else if (line[0] == '-' || line[0] == '+' || line[0] == '*') {
                    tempList.add(line)
                    tempType.add(UNORDER)
                }
                // 进入有序列表区
                else if (line.length > 1 && (line[0] >= '1' || line[0] <= '9') && line[1] == '.') {
                    tempList.add(line)
                    tempType.add(ORDER)
                } else if (line.length > 2 && line[0] == '#' && line[line.length - 1] != '#') {
                    var count = 0
                    for (index in line.indices) {
                        if (line[index] != '#') break
                        count++
                    }
                    tempList.add(line)
                    tempType.add(chooseTitle(count))
                } else {
                    tempList.add(line)
                    tempType.add(TEXT)
                }
            } else {
                // 代码区不接受其他格式
                tempList.add(line)
                tempType.add(TEXT)
            }
        }

        mdList = tempList.clone() as LinkedList<String>
        mdListType = tempType.clone() as LinkedList<Short>
        tempList.clear()
        tempType.clear()
    }

    private fun chooseTitle(count: Int): Short {
        return when (count) {
            1 -> H1
            2 -> H2
            3 -> H3
            else -> H4
        }
    }

    fun print() {
        for (i in mdLineList.indices) {
            Log.e("Print:", mdLineList[i].toString() + ":" + mdLineTypeList[i])
        }
    }

    // 行间解析
    // 主要是为了将各种不必要的元素去除
    private fun translateLineToText() {
        for (i in mdList.indices) {
            var line = mdList[i]
            val typeLine = mdListType[i]
            when (typeLine) {
                QUOTE -> mdList[i] = line.substring(2)
                UNORDER -> mdList[i] = line.substring(2)
                ORDER -> mdList[i] = line.substring(3)
                H1 -> mdList[i] = line.substring(2)
                H2 -> mdList[i] = line.substring(3)
                H3 -> mdList[i] = line.substring(4)
                H4 -> {
                    var count = 1
                    for (index in line.indices) {
                        if (line[index] != '#') break
                        count++
                    }
                    mdList[i] = line.substring(count)
                }
            }
        }
    }

    private fun translateTextInline() {
        for (i in mdList.indices) {
            var line = mdList[i]
            val type = mdListType[i]

            mdLineList.add(LinkedList())
            mdLineTypeList.add(LinkedList())

            if (type == TEXT) {
                val builder = SpannableStringBuilder()
                translateTextInlineToSpannable(line, builder, i)
                mSpannableStyle.append(builder)
            } else {
                // 作用在行与行之间
                var isSame = false

                when {
                    dealSameStack.isEmpty() -> {
                        dealSameStack.push(type)
                    }
                    dealSameStack.peek() == type -> {
                        dealSameStack.push(type)
                        isSame = true
                    }
                    else -> {
                        for (i in dealSameStack.indices) {
                            dealSameStack.pop()
                        }
                        dealSameStack.push(type)
                    }
                }

                if (type == ORDER) {
                    line = dealSameStack.size.toString() + ". " + line
                    mdLineList[i].add(line)
                    mdLineTypeList[i].add(type)
                } else if (type == UNORDER) {
                    line = "● $line"
                } else {
                    mdLineList[i].add(line)
                    mdLineTypeList[i].add(type)
                }
                typeChooseBetweenLines(type, line, isSame)
            }
        }
    }

    /**
     * 用于数据选择
     * 仅处理行与行之间的关系
     */
    private fun typeChooseBetweenLines(type: Short, line: String, isSame: Boolean) {
        var start = mSpannableStyle.length
        val end = mSpannableStyle.length + line.length
        if (isSame) {
            Log.e("Stack", dealSameStack[0].toString())
            for (i in 0..dealSameStack.size - 2) {
                start -= (mdLineList[mdLineList.size - 1 - i][0].length + 1)
            }
        }
        mSpannableStyle.append(line + '\n')
        when (type) {
            CODE -> {

            }
            QUOTE -> {
            }
            H1 -> {
                mSpannableStyle.setSpan(Styles.h1Span.what, start, end, Styles.h1Span.flag)
            }
            H2 -> {
                mSpannableStyle.setSpan(Styles.h2Span.what, start, end, Styles.h2Span.flag)
            }
            H3 -> {
                mSpannableStyle.setSpan(Styles.h3Span.what, start, end, Styles.h3Span.flag)
            }
            H4 -> {
                mSpannableStyle.setSpan(Styles.h4Span.what, start, end, Styles.h4Span.flag)
            }
        }
    }

    /**
     * 暂时先把行内部分逻辑代码转化成HTML形式
     * @param mark 语句
     * @return 暂时先是html 语句
     */
    private fun translateTextInlineToSpannable(
        str: String,
        builder: SpannableStringBuilder,
        position: Int
    ) {
        var i = 0
        var old_i = i
        while (i < str.length) {
            // 图片 ![]()
            if (i < str.length - 4 && str[i] == '!' && str[i + 1] == '[') {
                val index1 = str.indexOf(']', i + 1)
                if (index1 != -1 // 首先要找到
                    && index1 < str.length - 1 //其次不要爆
                    && str[index1 + 1] == '('
                    && str.indexOf(')', index1 + 2) != -1
                ) {
                    val index2 = str.indexOf(')', index1 + 2)
                    val picPath = str.substring(index1 + 2, index2)

                    i = index2
                    mdLineList[position].add(picPath)
                    mdLineTypeList[position].add(IMAGE)
                }
            }
            // 链接
            if (i < str.length - 3 && (i > 0 && str[i] == '[' && str[i - 1] != '!' || str[0] == '[')) {
                val index1 = str.indexOf(']', i + 1)
                if (index1 != -1
                    && index1 < str.length - 1
                    && str[index1 + 1] == '('
                    && str.indexOf(')', index1 + 2) != -1
                ) {
                    val index2 = str.indexOf(')', index1 + 2)
                    val linkName = str.substring(i + 1, index1)
                    val linkPath = str.substring(index1 + 2, index2)

                    i = index2

                    mdLineList[position].add(linkName)
                    mdLineList[position].add(linkPath)
                    mdLineTypeList[position].add(LINK_NAME)
                    mdLineTypeList[position].add(LINK_PATH)
                }
            }
            // 代码
            if (i < str.length - 1 && str[i] == '`') {
                val index = str.indexOf('`', i + 1)
                if (index != -1) {
                    val code = str.substring(i + 1, index)
                    i = index

                    mdLineList[position].add(code)
                    mdLineTypeList[position].add(CODE_INLINE)
                }
            }
            // 粗体
            if (i < str.length - 2 && str[i] == '*' && str[i + 1] == '*') {
                val index = str.indexOf("**", i + 3) // 如果中间没有字就没意义
                if (index != -1) {
                    val boldText = str.substring(i + 2, index)

                    i = index + 1

                    mdLineList[position].add(boldText)
                    mdLineTypeList[position].add(BOLD)
                }
            }
            // 斜体
            if (i < str.length - 2 && str[i] == '*' && str[i + 1] != '*') {
                val index = str.indexOf('*', i + 2)
                if (index != -1) {
                    val italicText = str.substring(i + 1, index)
                    i = index

                    mdLineList[position].add(italicText)
                    mdLineTypeList[position].add(ITALIC)
                }
            }
            // 文本直接保存
            if (old_i == i) {
                if (mdLineTypeList[position].isNotEmpty() && mdLineTypeList[position].last == TEXT_INLINE) {
                    mdLineList[position][mdLineList[position].size - 1] =
                        mdLineList[position].last + str[i]
                } else {
                    mdLineList[position].add(str[old_i].toString())
                    mdLineTypeList[position].add(TEXT_INLINE)
                }
            }
            i++
            old_i = i
        }

        // 上面对整个数据做完了解析，接下来就是要通过Spannable拼接完成
        for (i in mdLineTypeList[position].indices) {
            if(mdLineTypeList[position][i] == LINK_PATH) continue

            typeChooseInline(
                mdLineTypeList[position][i],
                mdLineList[position][i],
                builder,
                position,
                i
            )
        }
        builder.append('\n')
    }

    private fun typeChooseInline(
        type: Short,
        line: String,
        builder: SpannableStringBuilder,
        pos1: Int,
        pos2: Int
    ) {


        val start = builder.length
        val end = start + line.length
        builder.append(line)
        when (type) {
            BOLD -> {
                builder.setSpan(Styles.boldSpan.what, start, end, Styles.boldSpan.flag)
            }
            ITALIC -> {
                builder.setSpan(Styles.italicSpan.what, start, end, Styles.italicSpan.flag)
            }
            LINK_NAME -> {
                builder.setSpan(Styles.underLineSpan.what, start, end, Styles.underLineSpan.flag)
                builder.setSpan(Styles.linkSpan.what, start, end, Styles.linkSpan.flag)
                builder.setSpan(URLSpan(mdLineList[pos1][pos2 + 1]), start, end, Styles.linkSpan.flag)
            }
        }
    }
}