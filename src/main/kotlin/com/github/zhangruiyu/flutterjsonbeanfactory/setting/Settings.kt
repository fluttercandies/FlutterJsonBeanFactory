package com.github.zhangruiyu.flutterjsonbeanfactory.setting

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "FlutterJsonBeanFactorySettings", storages = [(Storage("FlutterJsonBeanFactorySettings.xml"))])
data class Settings(
    var modelSuffix: String,
    var isOpenNullSafety: Boolean?,
    var isOpenNullAble: Boolean?
) : PersistentStateComponent<Settings> {

    constructor() : this(
        "entity", null, null
    )

    override fun getState(): Settings {
        return this
    }

    override fun loadState(state: Settings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}