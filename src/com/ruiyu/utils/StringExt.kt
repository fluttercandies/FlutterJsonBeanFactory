package com.ruiyu.utils

//首字母转大写
fun String.toUpperCaseFirstOne(): String {
    return if (Character.isUpperCase(this[0]))
        this
    else
        StringBuilder().append(Character.toUpperCase(this[0])).append(this.substring(1)).toString()
}