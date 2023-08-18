package com.github.zhangruiyu.flutterjsonbeanfactory.utils

import io.flutter.FlutterUtils

object FieldUtils {
    /**
     * 把所有符号换成下划线,再把下换线后一位转成大写
     */
    fun toFieldTypeName(name: String): String {
        val init = name.replace(Regex("[^a-zA-Z0-9_]"), "_")
        val newInit = init.replace("-", "_")
        if (newInit.contains("_").not()) {
            return newInit.toUpperCaseFirstOne()
        }
        val ret = StringBuilder(newInit.length)
        for (word in newInit.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (word.isNotEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase())
                ret.append(word.substring(1).toLowerCase())
            }
            if (ret.length != newInit.length)
                ret.append(" ")
        }

        /* if (PRIMITIVE_TYPES[result] != null || dartKeyword.contains(result)) {
     //        throw MessageException("Please do not use the keyword $result as the key")
             result +="X"
         }*/
        return filedKeywordRename(ret.toString().replace(" ", ""))
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