package com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.project.Project
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.YamlHelper
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.GsonUtil.MapTypeAdapter
import com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart.utils.camelCase
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.JsonUtils
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toUpperCaseFirstOne


class ModelGenerator(
    val collectInfo: CollectInfo,
    val project: Project
) {
    var isFirstClass = true
    var allClasses = mutableListOf<ClassDefinition>()

    //parentType 父类型 是list 或者class
    private fun generateClassDefinition(
        className: String,
        parentName: String,
        jsonRawData: Any,
        parentType: String = "",
        isPrivate: Boolean = false,
    ): MutableList<ClassDefinition> {
        val newClassName = parentName + className
        val preName = newClassName
        if (jsonRawData is List<*>) {
            // if first element is an array, start in the first element.
            generateClassDefinition(newClassName, newClassName, jsonRawData[0]!!, isPrivate = isPrivate)
        } else if (jsonRawData is Map<*, *>) {
            val keys = jsonRawData.keys
            //如果是list,就把名字修改成单数
            val classDefinition = ClassDefinition(
                when {
                    "list" == parentType -> {
                        newClassName
                    }
                    isFirstClass -> {//如果是第一个类
                        isFirstClass = false
                        newClassName + collectInfo.modelSuffix().toUpperCaseFirstOne()
                    }
                    else -> {
                        newClassName
                    }
                },
                isPrivate = isPrivate,
            )
            keys.forEach { key ->
                val typeDef = TypeDefinition.fromDynamic(jsonRawData[key])
                if (typeDef.name == "Class") {
                    typeDef.name = preName + camelCase(key as String)
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
                        generateClassDefinition(dependency.className, newClassName, names[0]!!, "list", isPrivate = isPrivate)
                    }
                } else {
                    generateClassDefinition(dependency.className, newClassName, jsonRawData[dependency.name]!!, isPrivate = isPrivate)
                }
            }
        }
        return allClasses
    }

    fun generateDartClassesToString(fileName: String): String {
        //用阿里的防止int变为double 已解决 还是用google的吧 https://www.codercto.com/a/73857.html
//        val jsonRawData = JSON.parseObject(collectInfo.userInputJson)
        val originalStr = collectInfo.userInputJson.trim()
        val gson = GsonBuilder()
            .registerTypeAdapter(object : TypeToken<Map<String, Any>>() {}.type, MapTypeAdapter()).create()

        val jsonRawData = if (originalStr.startsWith("[")) {
            val list: List<Any> = gson.fromJson(originalStr, object : TypeToken<List<Any>>() {}.type)
            try {
                (JsonUtils.jsonMapMCompletion(list) as List<*>).first()
            } catch (e: Exception) {
                mutableMapOf<String, Any>()
            }

        } else {
            gson.fromJson<Map<String, Any>>(originalStr, object : TypeToken<Map<String, Any>>() {}.type)
        }
//        val jsonRawData = gson.fromJson<Map<String, Any>>(collectInfo.userInputJson, HashMap::class.java)
        val pubSpecConfig = YamlHelper.getPubSpecConfig(project)
        val hasLibJsonAnnotation = pubSpecConfig?.hasLibJsonAnnotation ?: false
        val isJsonSerializableCompat = pubSpecConfig?.isJsonSerializableCompat ?: false
        val isCompat = hasLibJsonAnnotation && isJsonSerializableCompat
        val classContentList = generateClassDefinition(
            collectInfo.firstClassName(), "", JsonUtils.jsonMapMCompletion(jsonRawData)
                ?: mutableMapOf<String, Any>(), isPrivate = isCompat
        )
        val classContent = classContentList.joinToString("\n\n")
        classContentList.fold(mutableListOf<TypeDefinition>()) { acc, de ->
            acc.addAll(de.fields.map { it.value })
            acc
        }
        val stringBuilder = StringBuilder()
        //导包
        stringBuilder.append("import 'dart:convert';")
        stringBuilder.append("\n")

        if (isCompat) {
            stringBuilder.append("import 'package:json_annotation/json_annotation.dart';")
            stringBuilder.append("\n")
            stringBuilder.append("import 'package:${pubSpecConfig?.name}/generated/json/base/json_convert_content.dart';")
            stringBuilder.append("\n\n")
            stringBuilder.append("part '${fileName}.g.dart';")
        } else {
            stringBuilder.append("import 'package:${pubSpecConfig?.name}/generated/json/base/json_field.dart';")
            stringBuilder.append("\n")
            stringBuilder.append("import 'package:${pubSpecConfig?.name}/generated/json/${fileName}.g.dart';")
        }
        stringBuilder.append("\n")
        stringBuilder.append("\n")
        stringBuilder.append(classContent)
        //生成helper类

        //生成
        return stringBuilder.toString()
    }


}