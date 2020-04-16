package com.ruiyu.utils

import java.util.regex.Pattern

//首字母转大写
fun String.toUpperCaseFirstOne(): String {
    return when {
        isEmpty() -> ""
        Character.isUpperCase(this[0]) -> this
        else -> StringBuilder().append(Character.toUpperCase(this[0])).append(this.substring(1)).toString()
    }
}

//首字母转小写
fun String.toLowerCaseFirstOne(): String {
    return if (Character.isLowerCase(this[0]))
        this
    else
        StringBuilder().append(Character.toLowerCase(this[0])).append(this.substring(1)).toString()
}

//大写字母转下划线和小写
fun String.upperCharToUnderLine(): String {
    val p = Pattern.compile("[A-Z]")
    if (this == "") {
        return ""
    }
    val builder = StringBuilder(this)
    val mc = p.matcher(this)
    var i = 0
    while (mc.find()) {
        builder.replace(mc.start() + i, mc.end() + i, "_" + mc.group().toLowerCase())
        i++
    }
    if ('_' == builder[0]) {
        builder.deleteCharAt(0)
    }
    return builder.toString()
}

//下划线和小写转大写字母
fun upperTable(str: String): String {
    // 字符串缓冲区
    val sbf = StringBuffer()
    // 如果字符串包含 下划线
    if (str.contains("_")) {
        // 按下划线来切割字符串为数组
        val split = str.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        // 循环数组操作其中的字符串
        var i = 0
        val index = split.size
        while (i < index) {
            // 递归调用本方法
            val upperTable = upperTable(split[i])
            // 添加到字符串缓冲区
            sbf.append(upperTable)
            i++
        }
    } else {// 字符串不包含下划线
        // 转换成字符数组
        val ch = str.toCharArray()
        // 判断首字母是否是字母
        if (ch[0] in 'a'..'z') {
            // 利用ASCII码实现大写
            ch[0] = (ch[0].toInt() - 32).toChar()
        }
        // 添加进字符串缓存区
        sbf.append(ch)
    }
    // 返回
    return sbf.toString()
}