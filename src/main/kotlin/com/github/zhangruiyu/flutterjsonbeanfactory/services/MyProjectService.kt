package com.github.zhangruiyu.flutterjsonbeanfactory.services

import com.intellij.openapi.project.Project
import com.github.zhangruiyu.flutterjsonbeanfactory.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
