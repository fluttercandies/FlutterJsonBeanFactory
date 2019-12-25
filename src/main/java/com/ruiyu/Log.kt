package com.ruiyu

import com.intellij.openapi.diagnostic.Logger

class Log {

    private val log: Logger

    init {
        log = Logger.getInstance(getInfo().clazz.substringAfterLast('.'))
    }

    fun e() {
        val info = getInfo()
        log.error(info.method)
    }

    fun e(message: Any?) {
        val info = getInfo()
        log.error("${info.method} ${message?.toString() ?: "null"}")
    }

    fun e(vararg messages: Any?) {
        val info = getInfo()
        val length = maxOf(info.method.length, 64)

        log.error("=".repeat(length))
        log.error(info.method)
        log.error("▁".repeat(length))
        messages.forEach { log.error(it.toString()) }
        log.error("▔".repeat(length))
    }

    fun e(message: Any?, throwable: Throwable) {
        val info = getInfo()
        log.error("${info.method} ${message?.toString() ?: "null"}", throwable)
    }

    fun w() {
        val info = getInfo()
        log.warn(info.method)
    }

    fun w(message: Any?) {
        val info = getInfo()
        log.warn("${info.method} ${message?.toString() ?: "null"}")

    }

    fun w(vararg messages: Any?) {
        val info = getInfo()
        val length = maxOf(info.method.length, 64)

        log.warn("=".repeat(length))
        log.warn(info.method)
        log.warn("▁".repeat(length))
        messages.forEach { log.warn(it.toString()) }
        log.warn("▔".repeat(length))
    }

    fun i() {
        val info = getInfo()
        log.info(info.method)
    }

    fun i(message: Any?) {
        val info = getInfo()
        log.info("${info.method} ${message?.toString() ?: "null"}")
    }

    fun getInfo(): LogCallerInfo {
        val list = Thread.currentThread().stackTrace
        val traceElement = list[if (list.size <= 4) list.size - 1 else 4]
        val method = traceElement.methodName
        val line = "(${traceElement.lineNumber})"
        val clazz = traceElement.className
        return LogCallerInfo(clazz, method, line)
    }

    inner class LogCallerInfo(val clazz: String, val method: String, val line: String)
}
