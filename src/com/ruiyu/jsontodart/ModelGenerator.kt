package com.ruiyu.jsontodart

import com.alibaba.fastjson.JSON
import com.ruiyu.jsontodart.utils.camelCase
import com.ruiyu.utils.JsonUtils


class ModelGenerator(
        val collectInfo: CollectInfo

) {

    var allClasses = mutableListOf<ClassDefinition>()
    private fun generateClassDefinition(className: String, jsonRawData: Any) {

        if (jsonRawData is List<*>) {
            // if first element is an array, start in the first element.
            generateClassDefinition(className, jsonRawData[0]!!)
        } else if (jsonRawData is Map<*, *>) {
            val keys = jsonRawData.keys
            val classDefinition = ClassDefinition(className);
            keys.forEach { key ->
                val typeDef = TypeDefinition.fromDynamic(jsonRawData[key])
                if (typeDef.name == "Class") {
                    typeDef.name = camelCase(key as String)
                }
                if (typeDef.subtype != null && typeDef.subtype == "Class") {
                    typeDef.subtype = camelCase(key as String)
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
                        generateClassDefinition(dependency.className, names[0]!!)
                    }
                } else {
                    generateClassDefinition(dependency.className, jsonRawData[dependency.name]!!)
                }
            }
        }
    }

     fun generateDartClassesToString(): String {
        //用阿里的防止int变为double
        val jsonRawData = JSON.parseObject(collectInfo.userInputJson, HashMap::class.java)
        JsonUtils.jsonMapMCompletion(jsonRawData)
        generateClassDefinition(collectInfo.firstClassEntityName(), jsonRawData)
        return allClasses.joinToString("\n")
    }

}