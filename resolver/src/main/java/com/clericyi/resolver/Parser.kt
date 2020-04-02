package com.clericyi.resolver

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

/**
 * author: ClericYi
 * time: 2020/4/1
 */

class Parser {
    private val TEXT: Short = 1
    private val CODE: Short = 2
    private val QUOTE: Short = 3
    private val UNORDER: Short = 4
    private val ORDER: Short = 5
    private val H1: Short = 6
    private val H2: Short = 7
    private val H3: Short = 8
    private val H4: Short = 9

    private val BLANK_LINE: Short = 11


    // 存放 md 的数据
    private var mdList: LinkedList<String> = LinkedList()
    // 存放 md 每行的数据的样式
    private var mdListType: LinkedList<Short> = LinkedList()

    constructor(fileName: String) {
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

    fun loadMD() {
        defineAreaType()
        translateLineToText()
        translateTextInline()
        print()
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
        for (i in mdList.indices) {
            print(mdListType[i])
            println(":" + mdList[i])
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
            val line = mdList[i]
            val typeLine = mdListType[i]
            if (typeLine == TEXT) {
                mdList[i] = translateInline(line).toString()
            }
        }
    }

    /**
     * 暂时先把行内部分逻辑代码转化成HTML形式
     * @param mark 语句
     * @return 暂时先是html 语句
     */
    private fun translateInline(line: String): String? {
        var line = line
        var i = 0
        while (i < line.length) {
            // 图片
            if (i < line.length - 4 && line[i] == '!' && line[i + 1] == '[') {
                val index1 = line.indexOf(']', i + 1)
                if (index1 != -1 && line[index1 + 1] == '('
                    && line.indexOf(')', index1 + 2) != -1
                ) {
                    val index2 = line.indexOf(')', index1 + 2)
                    val picName = line.substring(i + 2, index1)
                    val picPath = line.substring(index1 + 2, index2)
                    line = line.replace(
                        line.substring(i, index2 + 1),
                        "<img alt='$picName' src='$picPath' />"
                    )
                    i = index2
                }
            }
            // 链接
            if (i < line.length - 3 && (i > 0 && line[i] == '[' && line[i - 1] != '!' || line[0] == '[')) {
                val index1 = line.indexOf(']', i + 1)
                if (index1 != -1 && line[index1 + 1] == '('
                    && line.indexOf(')', index1 + 2) != -1
                ) {
                    val index2 = line.indexOf(')', index1 + 2)
                    val linkName = line.substring(i + 1, index1)
                    val linkPath = line.substring(index1 + 2, index2)
                    line = line.replace(
                        line.substring(i, index2 + 1),
                        "<a href='$linkPath'> $linkName</a>"
                    )
                    i = index2
                }
            }
            // 行内引用
            if (i < line.length - 1 && line[i] == '`' && line[i + 1] != '`') {
                val index = line.indexOf('`', i + 1)
                if (index != -1) {
                    val quoteName = line.substring(i + 1, index)
                    line =
                        line.replace(line.substring(i, index + 1), "<code>$quoteName</code>")
                    i = index
                }
            }
            // 粗体
            if (i < line.length - 2 && line[i] == '*' && line[i + 1] == '*') {
                val index = line.indexOf("**", i + 1)
                if (index != -1) {
                    val quoteName = line.substring(i + 2, index)
                    line = line.replace(
                        line.substring(i, index + 2),
                        "<strong>$quoteName</strong>"
                    )
                    i = index
                }
            }
            // 斜体
            if (i < line.length - 2 && line[i] == '*' && line[i + 1] != '*') {
                val index = line.indexOf('*', i + 1)
                if (index != -1) {
                    val quoteName = line.substring(i + 1, index)
                    line = line.replace(line.substring(i, index + 1), "<i>$quoteName</i>")
                    i = index
                }
            }
            i++
        }
        return line
    }
}