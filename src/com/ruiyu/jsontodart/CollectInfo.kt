package com.ruiyu.jsontodart

import com.intellij.openapi.components.ServiceManager
import com.ruiyu.setting.Settings
import com.ruiyu.utils.toLowerCaseFirstOne
import com.ruiyu.utils.toUpperCaseFirstOne
import com.ruiyu.utils.upperCharToUnderLine
import com.ruiyu.utils.upperTable

class CollectInfo {
    //用户输入的类名
    var userInputClassName = ""
    var userInputJson = ""
    var userIsSelectedRefresh: Boolean = false
    //用户设置的后缀
    fun modelSuffix(): String {
        return ServiceManager.getService(Settings::class.java).state.modelSuffix.toLowerCase()
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
            (upperTable(userInputClassName))
        } else {
            (userInputClassName)
        }
    }

    //用户输入的名字转为首个class的名字(文件中的类名)
    fun firstClassEntityName(): String {
        return if (userInputClassName.contains("_")) {
            (upperTable(userInputClassName) + modelSuffix().toUpperCaseFirstOne())
        } else {
            (userInputClassName + modelSuffix().toUpperCaseFirstOne())
        }
    }
}