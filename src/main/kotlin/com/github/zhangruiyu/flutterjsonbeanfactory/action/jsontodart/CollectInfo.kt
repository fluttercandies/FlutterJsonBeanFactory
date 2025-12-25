package com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart

import com.github.zhangruiyu.flutterjsonbeanfactory.setting.Settings
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toLowerCaseFirstOne
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toUpperCaseFirstOne
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.upperCharToUnderLine
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.upperTable
import com.intellij.openapi.components.service

class CollectInfo {
    //用户输入的类名
    var userInputClassName = ""
    var userInputJson = ""

    //用户设置的后缀
    fun modelSuffix(): String {
        val settings = service<Settings>()
        return settings.state.modelSuffix.lowercase(java.util.Locale.getDefault())
//        return ServiceManager.getService(Settings::class.java).state.modelSuffix.lowercase(getDefault())
    }

    //用户输入的类名转为文件名
    fun transformInputClassNameToFileName(): String {
        return if (!userInputClassName.contains("_")) {
            (userInputClassName + modelSuffix().toUpperCaseFirstOne()).upperCharToUnderLine()
        } else {
            (userInputClassName + "_" + modelSuffix().toLowerCaseFirstOne())
        }

    }


    //用户输入的名字转为首个class的名字(文件中的类名)
    fun firstClassName(): String {
        return if (userInputClassName.contains("_")) {
            (upperTable(userInputClassName)).toUpperCaseFirstOne()
        } else {
            (userInputClassName).toUpperCaseFirstOne()
        }
    }

    //用户输入的名字转为首个class的名字(文件中的类名)
    fun firstClassEntityName(): String {
        return if (userInputClassName.contains("_")) {
            (upperTable(userInputClassName).toUpperCaseFirstOne() + modelSuffix().toUpperCaseFirstOne())
        } else {
            (userInputClassName.toUpperCaseFirstOne() + modelSuffix().toUpperCaseFirstOne())
        }
    }
}