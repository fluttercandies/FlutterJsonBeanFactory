package com.ruiyu.jsontodart.utils

import com.intellij.ui.MessageException
import com.ruiyu.jsontodart.TypeDefinition
import jdk.nashorn.internal.runtime.regexp.RegExp
import java.util.regex.Pattern

val PRIMITIVE_TYPES = mapOf(
    "int" to true,
    "double" to true,
    "String" to true,
    "bool" to true,
    "List" to true,
    "List<Int>" to true,
    "List<Double>" to true,
    "List<String>" to true,
    "List<Boolean>" to true,
    "Null" to true
)

fun getTypeName(obj: Any?): String {
    return when (obj) {
        is String -> "String"
        is Int -> "int"
        is Double -> "double"
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
    val result = ret.toString().replace(" ", "")
    if (PRIMITIVE_TYPES[result] != null) {
        throw MessageException("Please do not use the keyword $result as the key")
    }
    return result
}

fun camelCaseFirstLower(text: String): String {
    val camelCaseText = camelCase(text)
    val firstChar = camelCaseText.substring(0, 1).toLowerCase()
    val rest = camelCaseText.substring(1)
    return "$firstChar$rest"
}


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
    return fieldName;
}


