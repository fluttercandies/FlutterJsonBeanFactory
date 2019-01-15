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
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.ruiyu.setting.GenerateCode
import com.ruiyu.setting.Settings
import com.ruiyu.utils.ergodicDartFile
import java.io.File

class FlutterBeanFactoryAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        getGenInfos(e)

    }

    companion object {
        //获取信息
        fun getGenInfos(e: AnActionEvent) {
            val project = e.getData(PlatformDataKeys.PROJECT)!!
            val projectBasePath = project.basePath!!
            val projectSrcPath = project.basePath!! + File.separator + "lib"
            val name = project.name

            //获取配置的遍历list
            val settingsState = ServiceManager.getService(Settings::class.java).state
            //遍历掉不可用参数
            val suffixFileList = settingsState.scanFileSetting.filter {
                it.isNotEmpty() && it[0].isNotEmpty()
            }
            //所有符合条件的文件集合 (projectName to file)
            val dartResultFiles = mutableMapOf<GenerateCode, MutableList<Pair<String, String>>>()
            ergodicDartFile(name, File(projectSrcPath), dartResultFiles, suffixFileList)
            if (dartResultFiles.isEmpty()) {
                Messages.showMessageDialog(
                    project,
                    "Get less than 1 eligible file",
                    "failure",
                    Messages.getErrorIcon()
                )
                return
            }
            dartResultFiles.forEach { t, u ->
                genFactory(GenerateNeedModel(projectBasePath, project, t, u,settingsState.ignoreContainFieldClass))
            }

        }

        //生成文件
        private fun genFactory(generateNeedModel: GenerateNeedModel) {
            generateNeedModel.run {
                val content = dartResultFiles.mapNotNull {
                    generatePackageAndClassName(it.first, File(it.second),generateNeedModel.ignoreContainFieldClass)
                }
                val factoryFile =
                    File((projectBasePath + File.separator + "lib" + File.separator + "${generateNeedModel.generateCode.scanName}_factory.dart"))
                WriteCommandAction.runWriteCommandAction(project) {
                    generateBeanFactory(generateNeedModel.generateCode, factoryFile, content)
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
}
