package com.ruiyu.jsontodart

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.ruiyu.beanfactory.FlutterBeanFactoryAction
import com.ruiyu.jsontodart.filetype.DartFileType
import com.ruiyu.ui.JsonInputDialog
import com.ruiyu.utils.Inflector
import com.ruiyu.utils.executeCouldRollBackAction
import wu.seal.jsontokotlin.utils.showNotify

class JsonToDartBeanAction : AnAction("JsonToDartBeanAction") {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return

        val dataContext = event.dataContext
        val module = LangDataKeys.MODULE.getData(dataContext) ?: return

        val navigatable = LangDataKeys.NAVIGATABLE.getData(dataContext)
        val directory = when (navigatable) {
            is PsiDirectory -> navigatable
            is PsiFile -> navigatable.containingDirectory
            else -> {
                val root = ModuleRootManager.getInstance(module)
                root.sourceRoots
                        .asSequence()
                        .mapNotNull {
                            PsiManager.getInstance(project).findDirectory(it)
                        }.firstOrNull()
            }
        } ?: return
        val directoryFactory = PsiDirectoryFactory.getInstance(directory.project)
        val packageName = directoryFactory.getQualifiedName(directory, true)
        val psiFileFactory = PsiFileFactory.getInstance(project)
        try {
            JsonInputDialog(project) { collectInfo ->
                //生成dart文件的内容
                val generatorClassContent = generatorClassModel(collectInfo)
                //文件名字
                val fileName = changeDartFileNameIfCurrentDirectoryExistTheSameFileNameWithoutSuffix(collectInfo.transformInputClassNameToFileName(),directory)

                generateDartDataClassFile(
                        fileName,
                        generatorClassContent,
                        project,
                        psiFileFactory,
                        directory
                )
                val notifyMessage = "Dart Data Class file generated successful"
                if (collectInfo.userIsSelectedRefresh) {
                    FlutterBeanFactoryAction.getGenInfos(event)
                }
                showNotify(notifyMessage, project)
            }.show()
        } catch (e: Exception) {
            project.showNotify(e.message!!)
        }

    }

    private fun generatorClassModel(collectInfo: CollectInfo): String {
        return ModelGenerator(collectInfo).generateDartClassesToString()
    }

    private fun generateDartDataClassFile(
            fileName: String,
            classCodeContent: String,
            project: Project?,
            psiFileFactory: PsiFileFactory,
            directory: PsiDirectory
    ) {

        executeCouldRollBackAction(project) {
            val file = psiFileFactory.createFileFromText("$fileName.dart", DartFileType(), classCodeContent)
            directory.add(file)
        }
    }

    private fun changeDartFileNameIfCurrentDirectoryExistTheSameFileNameWithoutSuffix(
            fileName: String,
            directory: PsiDirectory
    ): String {
        var newFileName = fileName
        val dartFileSuffix = ".dart"
        val fileNamesWithoutSuffix =
                directory.files.filter { it.name.endsWith(dartFileSuffix) }
                        .map { it.name.dropLast(dartFileSuffix.length) }
        while (fileNamesWithoutSuffix.contains(newFileName)) {
            newFileName += "X"
        }
        return newFileName
    }
}