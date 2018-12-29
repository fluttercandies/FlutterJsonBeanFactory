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
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.ruiyu.utils.ergodicDartFile
import java.io.File

class FlutterBeanFactoryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        genBeanFactory(e)

    }

    companion object {
        fun genBeanFactory(e: AnActionEvent) {
            //符合以集合内结尾的文件
            val keepFileSuffix = mutableListOf("entity.dart")
            val project = e.getData(PlatformDataKeys.PROJECT)!!
            val projectBasePath = project.basePath!!
            val projectSrcPath = project.basePath!! + File.separator + "lib"
            val name = project.name
            /*  val title = "标题";
              val msg = "2018,起航";
              Messages.showMessageDialog(project, msg, title, Messages.getInformationIcon());
              File(e.getData(PlatformDataKeys.PROJECT)!!.basePath).exists()
              File(e.getData(PlatformDataKeys.PROJECT)!!.basePath).readText()*/
            //需要生成的所有项目集合
            val projectInfos = listOf(name to projectSrcPath)
            //所有符合条件的文件集合 (projectName to file)
            val dartResultFiles = mutableListOf<Pair<String, String>>()
            projectInfos.forEach {
                ergodicDartFile(it.first, File(it.second), dartResultFiles, keepFileSuffix)
            }
            if (dartResultFiles.size == 0) {
                Messages.showMessageDialog(project, "获取符合条件的文件小于1个", "失败", Messages.getErrorIcon());
                return
            }
            val factoryFile = File((projectBasePath + File.separator + "lib" + File.separator + "bean_factory.dart"))
            val content = dartResultFiles.mapNotNull {
                generatePackageAndClassName(it.first, File(it.second))
            }
            WriteCommandAction.runWriteCommandAction(project) {
                generateBeanFactory(factoryFile, content)
                val notificationGroup = NotificationGroup("dart_bean_factory", NotificationDisplayType.BALLOON, true)
                ApplicationManager.getApplication().invokeLater {
                    val notification =
                        notificationGroup.createNotification("bean_factory文件生成完毕", NotificationType.INFORMATION)
                    Notifications.Bus.notify(notification, project)
                }
                //刷新目录
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(factoryFile)?.refresh(false, true)
            }
        }
    }

}
