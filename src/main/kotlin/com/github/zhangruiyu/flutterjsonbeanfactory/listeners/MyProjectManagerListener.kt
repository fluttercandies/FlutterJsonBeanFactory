package com.github.zhangruiyu.flutterjsonbeanfactory.listeners

import com.github.zhangruiyu.flutterjsonbeanfactory.App
import com.github.zhangruiyu.flutterjsonbeanfactory.action.migrate.MigrateOldProject
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.github.zhangruiyu.flutterjsonbeanfactory.services.MyProjectService

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        App.project = project
        project.service<MyProjectService>()
    }
}
