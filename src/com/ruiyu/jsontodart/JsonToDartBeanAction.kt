package com.ruiyu.jsontodart

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.ruiyu.beanfactory.FlutterBeanFactoryAction
import com.ruiyu.exception.MessageException
import com.ruiyu.setting.Settings
import com.ruiyu.ui.JsonInputDialog
import wu.seal.jsontokotlin.utils.showNotify

class JsonToDartBeanAction : AnAction("JsonToDartBeanAction") {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT) ?: return
        val inputDialog = JsonInputDialog("", project) { inputModel ->
            project.let {
                val dataContext = event.dataContext
                val module = DataKeys.MODULE.getData(dataContext)
                module?.let {
                    val navigatable = DataKeys.NAVIGATABLE.getData(dataContext)
                    val directory: PsiDirectory =
                        if (navigatable is PsiDirectory) {
                            navigatable
                        } else {
                            val root = ModuleRootManager.getInstance(module)
                            var tempDirectory: PsiDirectory? = null
                            for (file in root.sourceRoots) {
                                tempDirectory = PsiManager.getInstance(project).findDirectory(file)
                            }
                            tempDirectory!!
                        }
                    val directoryFactory = PsiDirectoryFactory.getInstance(directory.project)
                    val packageName = directoryFactory.getQualifiedName(directory, true)
                    val psiFileFactory = PsiFileFactory.getInstance(project)
                    val modelSuffix = ServiceManager.getService(Settings::class.java).state.modelSuffix
                    doGenerateKotlinDataClassFileAction(
                        inputModel.apply { className += modelSuffix },
                        project,
                        psiFileFactory,
                        directory,
                        event
                    )

                }
            }
        }
        inputDialog.show()
    }

    private fun doGenerateKotlinDataClassFileAction(
        inputModel: InputModel,
        project: Project?,
        psiFileFactory: PsiFileFactory,
        directory: PsiDirectory,
        event: AnActionEvent
    ) {

        try {
            ModelGenerator(inputModel.className, project, psiFileFactory, directory) {
                if (inputModel.isRefreshBeanFactory) {
                    FlutterBeanFactoryAction.genBeanFactory(event)
                }
            }.generateDartClasses(inputModel.json)
        } catch (e: MessageException) {
            project?.showNotify(e.message!!)
        }


    }

}