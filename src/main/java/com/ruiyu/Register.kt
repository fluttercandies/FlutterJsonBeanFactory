package com.ruiyu

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.ruiyu.file.FileHelpers

class Register(private val project: Project) : ProjectComponent {

    override fun initComponent() {}

    override fun disposeComponent() {}

    override fun getComponentName(): String {
        return "FlutterJsonBeanFactory"
    }

    override fun projectOpened() {
        FileHelpers.getPubSpecConfig(project)?.let { pubSpecConfig ->
            if (!pubSpecConfig.isFlutterModule) {
                return
            }
//
//            val am = ActionManager.getInstance()
//            if (am.getAction(REBUILD_FILE_ACTION_ID) != null) {
//                return
//            }
//
//            val newFileAction = NewArbFileAction()
//            am.registerAction(NEW_FILE_ACTION_ID, newFileAction)
//
//            val rebuildFileAction = RebuildI18nFile()
//            am.registerAction(REBUILD_FILE_ACTION_ID, rebuildFileAction)

//            (am.getAction("ToolbarRunGroup") as? DefaultActionGroup)?.let { windowM ->
//                windowM.addSeparator()
//                windowM.add(newFileAction)
//                windowM.add(rebuildFileAction)
//                windowM.addSeparator()
//            }
        }
    }

    override fun projectClosed() {
//        val am = ActionManager.getInstance()
//        am.getAction(NEW_FILE_ACTION_ID)?.templatePresentation?.isEnabled = false
//        am.getAction(REBUILD_FILE_ACTION_ID)?.templatePresentation?.isEnabled = false
    }

    companion object {
        private const val NEW_FILE_ACTION_ID = "FlutterI18n.NewArbFileAction"
        private const val REBUILD_FILE_ACTION_ID = "FlutterI18n.RebuildI18nFile"
    }
}
