package com.ruiyu.jsontodart

import com.alibaba.fastjson.JSON
import com.ruiyu.jsontodart.utils.camelCase
import com.ruiyu.utils.Inflector
import com.ruiyu.utils.JsonUtils
import com.ruiyu.utils.toUpperCaseFirstOne


class ModelGenerator(
        val collectInfo: CollectInfo
) {
    var isFirstClass = false
    var allClasses = mutableListOf<ClassDefinition>()
    private fun generateClassDefinition(className: String, parentName: String, jsonRawData: Any) {
        var newClassName = className
        if (collectInfo.modelPrefix()) {
            newClassName = parentName + newClassName
        }
        val preName = if (collectInfo.modelPrefix()) {
            newClassName
        } else ""
        if (jsonRawData is List<*>) {
            // if first element is an array, start in the first element.
            generateClassDefinition(Inflector.getInstance().singularize(newClassName), Inflector.getInstance().singularize(newClassName), jsonRawData[0]!!)
        } else if (jsonRawData is Map<*, *>) {
            val keys = jsonRawData.keys
            val classDefinition = ClassDefinition(Inflector.getInstance().singularize(newClassName))
            keys.forEach { key ->
                val typeDef = TypeDefinition.fromDynamic(jsonRawData[key])
                if (typeDef.name == "Class") {
                    typeDef.name = if (isFirstClass) {
                        preName + collectInfo.modelSuffix().toUpperCaseFirstOne() + camelCase(key as String)
                    } else {
                        preName + camelCase(key as String)
                    }
                }
                if (typeDef.subtype != null && typeDef.subtype == "Class") {
                    typeDef.subtype = preName + camelCase(key as String)
                }
                classDefinition.addField(key as String, typeDef)
            }
            if (allClasses.firstOrNull { cd -> cd == classDefinition } == null) {
                allClasses.add(classDefinition)
            }
            val dependencies = classDefinition.dependencies
            dependencies.forEach { dependency ->
                if (dependency.typeDef.name == "List") {
                    if (((jsonRawData[dependency.name]) as? List<*>)?.isNotEmpty() == true) {
                        val names = (jsonRawData[dependency.name] as List<*>)
                        generateClassDefinition(dependency.className, newClassName, names[0]!!)
                    }
                } else {
                    generateClassDefinition(dependency.className, newClassName, jsonRawData[dependency.name]!!)
                }
            }
        }
    }

    fun generateDartClassesToString(): String {
        //用阿里的防止int变为double
        val jsonRawData = JSON.parseObject(collectInfo.userInputJson)
        JsonUtils.jsonMapMCompletion(jsonRawData)
        generateClassDefinition(collectInfo.firstClassName(), "", jsonRawData)
        return allClasses.joinToString("\n")
    }

}