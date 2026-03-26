package com.github.zhangruiyu.flutterjsonbeanfactory.utils

import io.flutter.FlutterUtils
import java.util.Locale
import java.util.Locale.getDefault

object FieldUtils {
    private val NON_ALPHANUMERIC = Regex("[^a-zA-Z0-9_]")

    /**
     * 把所有符号换成下划线,再把下换线后一位转成大写
     */
    fun toFieldTypeName(name: String): String {
        val newInit = name.replace(NON_ALPHANUMERIC, "_")
        if (newInit.indexOf('_') == -1) {
            return newInit.toUpperCaseFirstOne()
        }
        val words = newInit.split("_").filter { it.isNotEmpty() }
        if (words.isEmpty()) return filedKeywordRename(newInit)
        
        val ret = StringBuilder(newInit.length)
        for (word in words) {
            ret.append(word.substring(0, 1).uppercase(getDefault()))
            ret.append(word.substring(1).lowercase(getDefault()))
        }

        return filedKeywordRename(ret.toString())
    }


    private fun filedKeywordRename(key: String): String {
        var notKeyWord = key
        //关键字的修改字段名
        if (FlutterUtils.isDartKeyword(key) || key.first().isDigit()) {
            notKeyWord = "x${key.toUpperCaseFirstOne()}"
        }
        return notKeyWord
    }

}