package com.ruiyu.utils

import org.apache.commons.lang3.time.DateUtils
import java.text.ParseException

/**
 * User: zhangruiyu
 * Date: 2019/12/30
 * Time: 11:17
 */
/*
object DateUtil {
    private val parsePatterns = arrayOf("yyyy-MM-dd", "yyyy年MM月dd日",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd", "yyyy.MM.dd", "HH:mm:ss", "HH时mm分ss秒", "yyyy年MM月dd日 HH时mm分ss秒",
            "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss.SSS", "yyyyMMddHHmmss", "yyyyMMddHHmmssSSS",
            "yyyyMMdd", "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE MMM dd HH:mm:ss zzz yyyy",
            "yyyy.MM.dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss"
    )

    */
/**
     * 判断是否能解析
     *//*

    fun canParseDate(string: String?): Boolean {
        return if (string == null) {
            false
        } else {
            try {
                DateUtils.parseDate(string, *parsePatterns)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}*/
