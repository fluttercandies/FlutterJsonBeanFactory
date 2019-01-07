package com.ruiyu.beanfactory

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.ruiyu.setting.Settings
import com.ruiyu.utils.ergodicDartFile
import com.ruiyu.utils.toUpperCaseFirstOne
import java.io.File

class FlutterBeanFactoryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        genBeanFactory(e)

    }

    companion object {
        fun genBeanFactory(e: AnActionEvent) {
            //获取配置的遍历list
            val suffixFileList = ServiceManager.getService(Settings::class.java).state.suffixFiles.split(",").map {
                it.toLowerCase()
            }.toMutableList()
            val project = e.getData(PlatformDataKeys.PROJECT)!!
            val projectBasePath = project.basePath!!
            val projectSrcPath = project.basePath!! + File.separator + "lib"
            val name = project.name
            //需要生成的所有项目集合
            val projectInfos = listOf(name to projectSrcPath)
            //所有符合条件的文件集合 (projectName to file)
            val dartResultFiles = mutableMapOf<String, MutableList<Pair<String, String>>>()
            projectInfos.forEach {
                ergodicDartFile(it.first, File(it.second), dartResultFiles, suffixFileList)
            }
            if (dartResultFiles.isEmpty()) {
                Messages.showMessageDialog(
                    project,
                    "Get less than 1 eligible file",
                    "failure",
                    Messages.getErrorIcon()
                );
                return
            }
            dartResultFiles.forEach { t, u ->
                genFactory(projectBasePath, project, t, u)
            }
        }

        private fun genFactory(
            projectBasePath: String,
            project: Project,
            fileName: String,
            dartResultFiles: List<Pair<String, String>>
        ) {
            val factoryFile =
                File((projectBasePath + File.separator + "lib" + File.separator + "${fileName}_factory.dart"))
            val content = dartResultFiles.mapNotNull {
                generatePackageAndClassName(it.first, File(it.second))
            }
            WriteCommandAction.runWriteCommandAction(project) {
                generateBeanFactory(fileName.toUpperCaseFirstOne(), factoryFile, content)
                val notificationGroup = NotificationGroup("dart_factory", NotificationDisplayType.BALLOON, true)
                ApplicationManager.getApplication().invokeLater {
                    val notification =
                        notificationGroup.createNotification("factory is generated", NotificationType.INFORMATION)
                    Notifications.Bus.notify(notification, project)
                }
                //刷新目录
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(factoryFile)?.refresh(false, true)
            }
        }
    }

}
