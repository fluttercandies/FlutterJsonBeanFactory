package com.ruiyu.setting

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "FlutterJsonBeanFactorySettings", storages = [(Storage("FlutterJsonBeanFactorySettings.xml"))])
data class Settings(
    var modelSuffix: String,
    var ignoreContainFieldClass: String,
    var scanFileSetting: List<Array<String>>
) : PersistentStateComponent<Settings> {

    constructor() : this(
        "entity", "base", mutableListOf(
            arrayOf("entity", "static T generateOBJ<T>(json) {", ".fromJson(json) as T;"),
            arrayOf("", "", ""),
            arrayOf("", "", "")
        )
    )

    override fun getState(): Settings {
        return this
    }

    override fun loadState(state: Settings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}

/**
 *  扫描的名字  方法 类名后面的
 */
data class GenerateCode(val scanName: String, val methodLine: String, val classNameLine: String)