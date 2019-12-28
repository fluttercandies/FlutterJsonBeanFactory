package com.ruiyu.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap

/**
 * 处理json的list第一个不完整生成的json dart bean
 * 方案:遍历复制list的所有字段到第一个里
 */
class JsonUtils {
    companion object {
        fun jsonMapMCompletion(data: Any?, initData: MutableMap<String, Any?>? = null): Any? {
            if (data == null) return null
            val returnData = initData ?: mutableMapOf()
            when (data) {
                is Map<*, *> -> {
                    data.forEach {
                        val key = it.key as String
                        //如果包含,并且字段不为null,那么就不去修改,如果包含,但是包含的是null,那么以最后一个做标准
                        if ((returnData.containsKey(key) && returnData[key] != null).not()) {
                            returnData[it.key as String] = jsonMapMCompletion(it.value)
                        }
                    }
                    return returnData
                }
                is List<*> -> {
                    if (data.isEmpty()) return listOf<String>()
                    return if (data.first() is Map<*, *>) {
                        //这个map中只有一个数据,但是map数据是最完整的
                        val listNewDataMap = mutableMapOf<String, Any?>()
                        data.forEach {
                            //如果是map,手写一个map,然后元数据每个map都遍历到这个值中,判断null和字段存在,主要为了防止map中字段不同意造成的json类不完整问题
                            if (it is Map<*, *>) {
                                //此时
                                jsonMapMCompletion(it, listNewDataMap)
                            }
                        }
                        listOf(listNewDataMap)
                    } else {
                        data
                    }

                }
                else -> {
                    return data
                }
            }
        }

    }
}
