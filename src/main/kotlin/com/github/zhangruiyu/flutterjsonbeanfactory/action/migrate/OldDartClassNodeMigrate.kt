package com.github.zhangruiyu.flutterjsonbeanfactory.action.migrate

import com.github.zhangruiyu.flutterjsonbeanfactory.utils.PubSpecConfig
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.YamlHelper
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.commitContent
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.CompositeElement
import com.jetbrains.lang.dart.DartTokenTypes
import org.jetbrains.kotlin.psi.psiUtil.children
import java.io.File

object OldDartClassNodeMigrate {
    fun getDartFileHelperClassGeneratorInfo(project: Project, pubSpecConfig: PubSpecConfig, file: PsiFile) {
        //不包含JsonConvert 那么就不转
        if ((file.text.contains("JsonConvert<")) && file.name != "json_convert_content.dart") {
            file.children.forEach {
                val text = it.text
                val classNode = it?.node
                //是类
                val isExtendsJsonConvert = (text.contains("with") || text.contains(
                    "extends"
                )) && text.contains(
                    "JsonConvert<"
                )
                var className: String? = null
                if (classNode?.elementType == DartTokenTypes.CLASS_DEFINITION && (isExtendsJsonConvert)
                ) {
                    if (classNode is CompositeElement) {
                        for (filedAndMethodNode in classNode.children()) {
                            val nodeName = filedAndMethodNode.text
                            if (filedAndMethodNode.elementType == DartTokenTypes.COMPONENT_NAME) {
                                className = nodeName
                            } else if (filedAndMethodNode.elementType == DartTokenTypes.MIXINS) {
                                //不包含JsonConvert 那么就不转
                                if (nodeName.contains("JsonConvert").not()) {
                                    continue
                                }
                            }

                        }
                    }
                }
                if (className != null) {
                    val replaceWith = if (file.text.contains("with JsonConvert<${className}> {")) {
                        "with JsonConvert<${className}> {"
                    } else if (file.text.contains("with JsonConvert<${className}>{")) {
                        "with JsonConvert<${className}>{"
                    } else if (file.text.contains("with JsonConvert<${className}>  {")) {
                        "with JsonConvert<${className}>  {"
                    } else if (file.text.contains("with JsonConvert<${className}>\n{")) {
                        "with JsonConvert<${className}>\n{"
                    } else if (file.text.contains("with JsonConvert<${className}>\t{")) {
                        "with JsonConvert<${className}>\t{"
                    } else if (file.text.contains("with JsonConvert<${className}>\n\t{")) {
                        "with JsonConvert<${className}>\n\t{"
                    } else if (file.text.contains("extends JsonConvert<${className}> {")) {
                        "extends JsonConvert<${className}> {"
                    } else if (file.text.contains("extends JsonConvert<${className}>{")) {
                        "extends JsonConvert<${className}>{"
                    } else if (file.text.contains("extends JsonConvert<${className}>  {")) {
                        "extends JsonConvert<${className}>  {"
                    } else if (file.text.contains("extends JsonConvert<${className}>\n{")) {
                        "extends JsonConvert<${className}>\n{"
                    } else if (file.text.contains("extends JsonConvert<${className}>\t{")) {
                        "extends JsonConvert<${className}>\t{"
                    } else if (file.text.contains("extends JsonConvert<${className}>\n\t{")) {
                        "extends JsonConvert<${className}>\n\t{"
                    } else {
                        "JsonConvert<${className}> {"
                    }

                    val result = """
import 'package:${pubSpecConfig.name}/generated/json/${File(file.name).nameWithoutExtension}_helper.dart';
${
                        file.text.lines().filterNot { itemLine ->
                            itemLine.contains("json_convert_content.dart")
                        }.joinToString("\n").replace("class $className", "@JsonSerializable()\nclass $className")
                            .replace(
                                replaceWith,
                                "{\n\n\tfactory ${className}.fromJson(Map<String, dynamic> json) => $${className}FromJson(json);\n\n" +
                                        "\tMap<String, dynamic> toJson() => $${className}ToJson(this);\n"
                            )
                    }
                """.trimIndent()
                    file.virtualFile.commitContent(project, result)
                }
            }
        }
    }
}