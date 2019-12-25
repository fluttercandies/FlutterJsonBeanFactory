package com.ruiyu.jsontodart.utils

import com.ruiyu.jsontodart.TypeDefinition
import com.ruiyu.utils.Inflector
import com.ruiyu.utils.LogUtil
import com.ruiyu.utils.toUpperCaseFirstOne
import java.math.BigDecimal

val PRIMITIVE_TYPES = mapOf(
        "int" to true,
        "double" to true,
        "String" to true,
        "bool" to true,
        "List" to true,
        "List<int>" to true,
        "List<double>" to true,
        "List<String>" to true,
        "List<bool>" to true,
        "List" to true,
        "Null" to true,
        "dynamic" to true
)


fun getListSubType(typeName: String): String {
    return mapOf(
            "List<int>" to "int",
            "List<double>" to "double",
            "List<String>" to "String",
            "List<bool>" to "bool",
            "List" to "dynamic",
            "List<Null>" to "dynamic"
    )[typeName] ?: typeName.substringAfter("<").substringBefore(">")
}

val dartKeyword = mutableListOf("abstract", "dynamic", "implements", "show",
        "as", "else", "import ", "static",
        "assert", "enum", "in", "super",
        "async", "export", "interface", "switch",
        "await", "external", "is", "sync",
        "break", "extends", "library", "this",
        "case", "factory", "mixin", "throw",
        "catch", "false", "new", "true",
        "class", "final", "null", "try",
        "const", "finally", "on", "typedef",
        "continue", "for", "operator", "var",
        "covariant", "Function", "part", "void",
        "default", "get", "rethrow", "while",
        "deferred", "hide", "return", "with",
        "do", "if", "set", "yield", "list", "map")

fun getTypeName(obj: Any?): String {
    return when (obj) {
        is String -> "String"
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

fun isPrimitiveType(typeName: String): Boolean {
    return PRIMITIVE_TYPES[typeName] ?: return false
}


fun camelCase(init: String): String {
    val ret = StringBuilder(init.length)

    for (word in init.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        if (!word.isEmpty()) {
            ret.append(word.substring(0, 1).toUpperCase())
            ret.append(word.substring(1).toLowerCase())
        }
        if (ret.length != init.length)
            ret.append(" ")
    }
    var result = ret.toString().replace(" ", "")

    /* if (PRIMITIVE_TYPES[result] != null || dartKeyword.contains(result)) {
 //        throw MessageException("Please do not use the keyword $result as the key")
         result +="X"
     }*/
    return result
}

fun camelCaseFirstLower(text: String): String {
    LogUtil.w(text)
    if (text.isEmpty()) {
        return text
    }
    val camelCaseText = if (text.contains("_")) {
        camelCase(text)
    } else {
        text
    }
    if (camelCaseText.length == 1) {
        return camelCaseText.toLowerCase()
    }
    val firstChar = camelCaseText.substring(0, 1).toLowerCase()
    val rest = camelCaseText.substring(1)
    return "$firstChar$rest"
}

//驼峰命名
fun fixFieldName(name: String, typeDef: TypeDefinition? = null, privateField: Boolean = false): String {
    var properName = name;
    if (name.startsWith('_') || name.startsWith("[0-9]")) {
        val firstCharType = typeDef?.name?.substring(0, 1)?.toLowerCase();
        properName = "$firstCharType$name"
    }
    val fieldName = camelCaseFirstLower(properName);
    if (privateField) {
        return "_$fieldName"
    }
    return filedKeywordRename(fieldName)
}

fun filedKeywordRename(key: String): String {
    var notKeyWord = key
    //关键字的修改字段名
    if (dartKeyword.contains(key.toLowerCase())) {
        notKeyWord = "x${key.toUpperCaseFirstOne()}"
    }
    return notKeyWord
}
