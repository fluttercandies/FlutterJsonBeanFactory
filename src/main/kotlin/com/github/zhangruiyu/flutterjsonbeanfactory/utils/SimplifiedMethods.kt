package com.github.zhangruiyu.flutterjsonbeanfactory.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project

/**
 * File contains functions which simply other functions's invoke
 * Created by Seal.Wu on 2018/2/7.
 */


/**
 * do the action that could be roll-back
 */
fun Project?.executeCouldRollBackAction(action: (Project?) -> Unit) {
    CommandProcessor.getInstance().executeCommand(this, {
        ApplicationManager.getApplication().runWriteAction {
            action.invoke(this)
        }
    }, "FlutterJsonBeanFactory", "FlutterJsonBeanFactory")
}

