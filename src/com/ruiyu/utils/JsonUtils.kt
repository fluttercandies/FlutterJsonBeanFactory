package com.ruiyu.utils

import com.google.gson.internal.LinkedTreeMap

/**
 * 处理json的list第一个不完整生成的json dart bean
 * 方案:遍历复制list的所有字段到第一个里
 */
class JsonUtils {
    companion object {
        fun jsonMapMCompletion(jsonRawData: Map<*, *>) {
            jsonRawData.keys.mapIndexed { index, key ->
                if (jsonRawData[key] is Map<*, *>) {
                    jsonMapMCompletion(jsonRawData[key] as Map<*, *>)
                } else if (jsonRawData[key] is ArrayList<*>) {
                    val list = jsonRawData[key] as ArrayList<*>
                    listCompletion(list)
                }
            }
        }

        private fun listCompletion(list: ArrayList<*>) {
            list.mapIndexed { index, any ->
                if (list.isNotEmpty()) {
                    if (list.size == 1) {
                        if (list[0] is Map<*, *>) {
                            jsonMapMCompletion(list[0] as Map<*, *>)
                        } else if (list[0] is ArrayList<*>) {
                            listCompletion(list[0] as ArrayList<*>)
                        }
                    } else if (index != 0) {
                        when {
                            any is Map<*, *> -> {
                                any.forEach { key, value ->
                                    val firstData = (list[0] as LinkedTreeMap<Any?, Any?>)
                                    if (firstData.containsKey(key).not() || firstData[key] == null) {//不包含
                                        firstData[key] = value
                                        if (firstData[key] is Map<*, *>) {
                                            jsonMapMCompletion(firstData[key] as Map<*, *>)
                                        } else if (firstData[key] is ArrayList<*>) {
                                            listCompletion(firstData[key] as ArrayList<*>)
                                        }
                                    }
                                }
                                jsonMapMCompletion(any)
                            }
                            list[0] is ArrayList<*> -> listCompletion(list[0] as ArrayList<*>)
                            else -> {//不是list,那么就是基础数据类型

                            }
                        }
                    }


                }

            }
        }

    }
}