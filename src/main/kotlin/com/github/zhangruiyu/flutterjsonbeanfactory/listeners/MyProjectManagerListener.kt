package com.github.zhangruiyu.flutterjsonbeanfactory.listeners

import com.github.zhangruiyu.flutterjsonbeanfactory.App
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        App.project = project
    }
}
