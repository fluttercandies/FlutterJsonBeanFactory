package com.ruiyu.setting

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

@State(name = "FlutterJsonBeanSetting", storages = [Storage("FlutterJsonBeanSetting.xml")])
class SettingComponent : Configurable {
    var settingLayout: SettingLayout? = null
    override fun isModified(): Boolean {
        if (settingLayout == null) {
            return false
        }
        return getSettings() != Settings(
            settingLayout!!.getModelSuffix(),
            settingLayout!!.getIgnoreContainFieldClassTextField(),
            settingLayout!!.getSuffixFiles())
    }

    override fun getDisplayName(): String {
        return "FlutterJsonBeanFactory"
    }

    override fun apply() {
        settingLayout?.run {
            getSettings().apply {
                modelSuffix = getModelSuffix()
                scanFileSetting = getSuffixFiles()
            }
        }
    }


    override fun createComponent(): JComponent? {
        settingLayout = SettingLayout(getSettings())
        return settingLayout!!.getRootComponent()
    }

    private fun getSettings(): Settings {
        return ServiceManager.getService(Settings::class.java).state
    }

}
