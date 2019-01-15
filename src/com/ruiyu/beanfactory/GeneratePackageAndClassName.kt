package com.ruiyu.beanfactory

import java.io.File

//生成包名信息和类名信息
fun generatePackageAndClassName(projectName: String, file: File,ignoreContainFieldClass:String): Pair<String, String>? {
    file.inputStream().bufferedReader().useLines {

        val result = it.firstOrNull { line ->
            line.contains("class")
        }
        return if(result == null){
            null
        }else{
            var className = result.trim().removeSuffix("{").removePrefix("class").trim().split(" ")[0]
            //去掉基类
            if(ignoreContainFieldClass.isNotEmpty() && className.contains(ignoreContainFieldClass,true)){
                return null
            }
            //去掉泛型
            if(className.contains("<")){
                className = className.split("<")[0]
            }
            val adbPath = file.absolutePath
            val tag = "lib"
    //        println(file.absolutePath.removePrefix())
            className to "package:$projectName${adbPath.substring(
                file.absolutePath.indexOf(tag) + tag.length,
                adbPath.length
            )}"
        }

    }

}