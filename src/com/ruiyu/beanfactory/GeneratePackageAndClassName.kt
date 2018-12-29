package com.ruiyu.beanfactory

import java.io.File

//生成包名信息和类名信息
fun generatePackageAndClassName(projectName: String, file: File): Pair<String, String>? {
    file.inputStream().bufferedReader().useLines {

        val result = it.firstOrNull { line ->
            line.contains("class")
        }
        if(result == null){
            return null
        }else{
            val className = result.trim().removeSuffix("{").removePrefix("class").trim()
            val adbPath = file.absolutePath
            val tag = "lib"
//        println(file.absolutePath.removePrefix())
            return className to "package:$projectName${adbPath.substring(
                file.absolutePath.indexOf(tag) + tag.length,
                adbPath.length
            )}"
        }

    }

}