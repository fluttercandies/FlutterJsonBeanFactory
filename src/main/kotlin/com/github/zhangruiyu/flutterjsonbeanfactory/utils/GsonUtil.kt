package com.github.zhangruiyu.flutterjsonbeanfactory.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import java.io.IOException
import com.google.gson.stream.JsonReader
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.IllegalStateException
import java.util.ArrayList

/**
 * User: zhangruiyu
 * Date: 2019/12/24
 * Time: 21:19
 */
object GsonUtil {
    /**
     * 实现格式化的时间字符串转时间对象
     */
    private const val DATEFORMAT_default = "yyyy-MM-dd HH:mm:ss"

    /**
     * 使用默认的gson对象进行反序列化
     *
     * @param json
     * @param typeToken
     * @return
     */
    fun <T> fromJsonDefault(json: String?, typeToken: TypeToken<T>): T {
        val gson = Gson()
        return gson.fromJson(json, typeToken.type)
    }

    /**
     * json字符串转list或者map
     *
     * @param json
     * @param typeToken
     * @return
     */
    fun <T> fromJson(json: String?, typeToken: TypeToken<T>): T {
        val gson = GsonBuilder()
            /**
             * 重写map的反序列化
             */
            .registerTypeAdapter(object : TypeToken<Map<String?, Any?>?>() {}.type, MapTypeAdapter()).create()
        return gson.fromJson(json, typeToken.type)
    }

    /**
     * json字符串转bean对象
     *
     * @param json
     * @param cls
     * @return
     */
    fun <T> fromJson(json: String?, cls: Class<T>?): T {
        val gson = GsonBuilder().setDateFormat(DATEFORMAT_default)
            .create()
        return gson.fromJson(json, cls)
    }

    /**
     * 对象转json
     *
     * @param obj
     * @param format
     * @return
     */
    fun toJson(obj: Any?, format: Boolean): String {
        val gsonBuilder = GsonBuilder()
        /**
         * 设置默认时间格式
         */
        gsonBuilder.setDateFormat(DATEFORMAT_default)
        /**
         * 添加格式化设置
         */
        if (format) {
            gsonBuilder.setPrettyPrinting()
        }
        val gson = gsonBuilder.create()
        return gson.toJson(obj)
    }

    class MapTypeAdapter : TypeAdapter<Any?>() {
        @Throws(IOException::class)
        override fun read(`in`: JsonReader): Any? {
            val token = `in`.peek()
            return when (token) {
                JsonToken.BEGIN_ARRAY -> {
                    val list: MutableList<Any?> = ArrayList()
                    `in`.beginArray()
                    while (`in`.hasNext()) {
                        list.add(read(`in`))
                    }
                    `in`.endArray()
                    list
                }

                JsonToken.BEGIN_OBJECT -> {
                    val map: MutableMap<String, Any?> =
                        LinkedTreeMap()
                    `in`.beginObject()
                    while (`in`.hasNext()) {
                        map[`in`.nextName()] = read(`in`)
                    }
                    `in`.endObject()
                    map
                }

                JsonToken.STRING -> `in`.nextString()
                JsonToken.NUMBER -> {
                    /**
                     * 改写数字的处理逻辑，将数字值分为整型与浮点型。
                     */
                    val dbNum = `in`.nextString()
                    if (!dbNum.contains(".")) {
                        // 解析并返回实际的整数值
                        try {
                            dbNum.toLong()
                        } catch (e: NumberFormatException) {
                            0
                        }
                    } else {
                        // 解析并返回实际的浮点数值
                        try {
                            dbNum.toDouble()
                        } catch (e: NumberFormatException) {
                            0.0
                        }
                    }
                }

                JsonToken.BOOLEAN -> `in`.nextBoolean()
                JsonToken.NULL -> {
                    `in`.nextNull()
                    null
                }

                else -> throw IllegalStateException()
            }
        }

        @Throws(IOException::class)
        override fun write(out: JsonWriter, value: Any?) {
            // 序列化无需实现
        }
    }
}