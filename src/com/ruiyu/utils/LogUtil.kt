package com.ruiyu.utils

import com.intellij.openapi.diagnostic.LoggerRt
import com.ruiyu.PLUGIN_NAME

/**
 * Created by Seal.Wu on 2018/3/12.
 */
object LogUtil {

    fun i(info: String) {
        LoggerRt.getInstance(PLUGIN_NAME).info(info)
    }

    fun w(warning: String) {
        LoggerRt.getInstance(PLUGIN_NAME).warn(warning)
    }

    fun e(message: String, e: Throwable) {
        LoggerRt.getInstance(PLUGIN_NAME).error(message, e)
    }
}