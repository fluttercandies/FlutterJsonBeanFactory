package com.ruiyu.setting

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "FlutterJsonBeanSettings", storages = [(Storage("FlutterJsonBeanSettings.xml"))])
data class Settings(var modelSuffix: String, var suffixFiles: String) : PersistentStateComponent<Settings> {

    constructor() : this("Entity", "entity")

    override fun getState(): Settings {
        return this
    }

    override fun loadState(state: Settings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}