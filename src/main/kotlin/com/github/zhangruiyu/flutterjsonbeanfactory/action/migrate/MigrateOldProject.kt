package com.github.zhangruiyu.flutterjsonbeanfactory.action.migrate

import com.github.zhangruiyu.flutterjsonbeanfactory.utils.YamlHelper
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex

object MigrateOldProject {
    /**
     * 获取所有符合生成的file
     */
    fun getAllEntityFiles(project: Project) {
        val pubSpecConfig = YamlHelper.getPubSpecConfig(project)
        val psiManager = PsiManager.getInstance(project)
        return FilenameIndex.getAllFilesByExt(project, "dart").filter {
            //不过滤entity结尾了
            /*it.path.endsWith("_${ServiceManager.getService(Settings::class.java).state.modelSuffix.toLowerCase()}.dart") && */it.path.contains(
            "${project.name}/lib/"
        )
        }.sortedBy {
            it.path
        }.forEach {
            psiManager.findFile(it)?.run {
                OldDartClassNodeMigrate.getDartFileHelperClassGeneratorInfo(project, pubSpecConfig!!,this)
            }
        }
    }

}