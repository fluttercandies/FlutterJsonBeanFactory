package com.github.zhangruiyu.flutterjsonbeanfactory.setting

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "FlutterJsonBeanFactorySettings", storages = [(Storage("FlutterJsonBeanFactorySettings.xml"))])
data class Settings(
    var modelSuffix: String,
    var isOpenNullAble: Boolean?,
    var setDefault: Boolean?,
    var boolDefaultValue: String = "false",
    var stringDefaultValue: String = "''",
    var intDefaultValue: String = "0",
    var doubleDefaultValue: String = "0.0",
    var listDefaultValue: String = "[]",
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

    fun stringFieldDefaultValue(): String? {
        return if (setDefault == true && stringDefaultValue.isNotEmpty()) {
            stringDefaultValue
        } else {
            null
        }
    }

    fun boolFieldDefaultValue(): String? {
        return if (setDefault == true && boolDefaultValue.isNotEmpty()) {
            boolDefaultValue
        } else {
            null
        }
    }

    fun intFieldDefaultValue(): String? {
        return if (setDefault == true && intDefaultValue.isNotEmpty()) {
            intDefaultValue
        } else {
            null
        }
    }

    fun doubleFieldDefaultValue(): String? {
        return if (setDefault == true && doubleDefaultValue.isNotEmpty()) {
            doubleDefaultValue
        } else {
            null
        }
    }

    fun listFieldDefaultValue(): String? {
        return if (setDefault == true && listDefaultValue.isNotEmpty()) {
            listDefaultValue
        } else {
            null
        }
    }
}