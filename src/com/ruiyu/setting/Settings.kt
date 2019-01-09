package com.ruiyu.setting

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "FlutterJsonBeanSettings", storages = [(Storage("FlutterJsonBeanSetting.xml"))])
data class Settings(var modelSuffix: String, var scanFileSetting:List<Array<String>>) : PersistentStateComponent<Settings> {

    constructor() : this("entity",  mutableListOf(arrayOf("entity","static T generateOBJ<T>(json) {",".fromJson(json) as T;"),
        arrayOf("presenter","",""),
        arrayOf("","","")))

    override fun getState(): Settings {
        return this
    }

    override fun loadState(state: Settings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
data class GenerateCode(val scanName:String,val methodLine:String,val classNameLine:String)