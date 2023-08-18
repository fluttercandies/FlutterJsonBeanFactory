package com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart.utils

import com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart.TypeDefinition
//import com.github.zhangruiyu.flutterjsonbeanfactory.utils.DateUtil
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.LogUtil
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toUpperCaseFirstOne
import io.flutter.FlutterUtils
import java.math.BigDecimal

private val PRIMITIVE_TYPES = mapOf(
    "int" to true,
    "num" to true,
    "double" to true,
    "String" to true,
    "bool" to true,
    "List" to true,
    "DateTime" to true,
    "List<int>" to true,
    "List<double>" to true,
    "List<String>" to true,
    "List<bool>" to true,
    "List<num>" to true,
    "List<DateTime>" to true,
    "List<dynamic>" to true,
    "List" to true,
    "Null" to true,
    "var" to true,
    "dynamic" to true
)

//基础类型
private val BASE_TYPES = mapOf(
    "int" to true,
    "num" to true,
    "double" to true,
    "String" to true,
    "bool" to true,
    "List" to true,
    "DateTime" to true,
    "Null" to true,
    "var" to true,
    "dynamic" to true
)

/**
 * 是否是主数据类型
 */
fun isPrimitiveType(typeName: String): Boolean {
    return PRIMITIVE_TYPES[typeName.replace("?", "")] ?: false
}

/**
 * 是否是基础数据类型
 */
fun isBaseType(typeName: String): Boolean {
    return BASE_TYPES[typeName] ?: false
}


//是否是List类型
fun isListType(typeName: String): Boolean {
    return when {
        typeName.contains("List<") -> {
            true
        }
        else -> {
            typeName == "List"
        }
    }
}

fun getTypeName(obj: Any?): String {
    return when (obj) {
        is String -> /*if (DateUtil.canParseDate(obj.toString())) "DateTime" else*/ "String"
        is Int -> "int"
        is Double -> "double"
        is Long -> "int"
        is BigDecimal -> "double"
        is Boolean -> "bool"
        null -> "Null"
        is List<*> -> "List"
        else -> // assumed class
            "Class"
    }
}


