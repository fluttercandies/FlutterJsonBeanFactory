package com.ruiyu.utils

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.google.gson.internal.LinkedTreeMap

/**
 * 处理json的list第一个不完整生成的json dart bean
 * 方案:遍历复制list的所有字段到第一个里
 */
class JsonUtils {
    companion object {
        fun jsonMapMCompletion(jsonRawData: Map<*, *>) {
            jsonRawData.keys.mapIndexed { index, key ->
                val any = jsonRawData[key]!!
                var a = any::class.java
                val b = jsonRawData[key] is Map<*, *>
                if (jsonRawData[key] is Map<*, *>) {
                    jsonMapMCompletion(jsonRawData[key] as Map<*, *>)
                } else if (jsonRawData[key] is JSONArray) {
                    val list = jsonRawData[key] as JSONArray
                    listCompletion(list)
                }
            }
        }

        private fun listCompletion(list: JSONArray) {
            list.mapIndexed { index, any ->

                val toList = list.toList()
                if (toList.isNotEmpty()) {
                    if (toList.size == 1) {
                        if (list[0] is Map<*, *>) {
                            jsonMapMCompletion(list[0] as Map<*, *>)
                        } else if (list[0] is JSONArray) {
                            listCompletion(list[0] as JSONArray)
                        }
                    } else if (index != 0) {
                        when {
                            any is Map<*, *> -> {
                                any.forEach { key, value ->
                                    val firstData = (list[0] as JSONObject)
                                    if (firstData.containsKey(key).not() || firstData[key] == null) {//不包含
                                        firstData[key as String] = value
                                        if (firstData[key] is Map<*, *>) {
                                            jsonMapMCompletion(firstData[key] as Map<*, *>)
                                        } else if (firstData[key] is JSONArray) {
                                            listCompletion(firstData[key] as JSONArray)
                                        }
                                    }
                                }
                                jsonMapMCompletion(any)
                            }
                            list[0] is JSONArray -> listCompletion(list[0] as JSONArray)
                            else -> {//不是list,那么就是基础数据类型

                            }
                        }
                    }


                }

            }
        }

    }
}