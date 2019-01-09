package com.ruiyu.beanfactory

import com.intellij.openapi.project.Project
import com.ruiyu.setting.GenerateCode

data class GenerateNeedModel(
    var projectBasePath: String,
    var project: Project,
    var generateCode: GenerateCode,
    var dartResultFiles: List<Pair<String, String>>) {
}