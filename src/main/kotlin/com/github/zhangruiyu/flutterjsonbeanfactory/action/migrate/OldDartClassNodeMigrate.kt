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
            val className: MutableList<String> = mutableListOf()
            file.children.forEach {
                val text = it.text
                val classNode = it?.node
                //是类
                val isExtendsJsonConvert = (text.contains("with") || text.contains(
                    "extends"
                )) && text.contains(
                    "JsonConvert<"
                )
                if (classNode?.elementType == DartTokenTypes.CLASS_DEFINITION && (isExtendsJsonConvert)
                ) {
                    if (classNode is CompositeElement) {
                        for (filedAndMethodNode in classNode.children()) {
                            val nodeName = filedAndMethodNode.text
                            if (filedAndMethodNode.elementType == DartTokenTypes.COMPONENT_NAME) {
                                className.add(nodeName)
                            } else if (filedAndMethodNode.elementType == DartTokenTypes.MIXINS) {
                                //不包含JsonConvert 那么就不转
                                if (nodeName.contains("JsonConvert").not()) {
                                    continue
                                }
                            }

                        }
                    }
                }
            }
            var addImport = if (file.text.contains("json_field.dart").not()) {
                "import 'package:${pubSpecConfig.name}/generated/json/base/json_field.dart';\n"
            } else ""
            addImport += "import 'package:${pubSpecConfig.name}/generated/json/${File(file.name).nameWithoutExtension}.g.dart';\n"
            className.forEach { itemClass ->
                val replaceWith = if (file.text.contains("with JsonConvert<${itemClass}> {")) {
                    "with JsonConvert<${itemClass}> {"
                } else if (file.text.contains("with JsonConvert<${itemClass}>{")) {
                    "with JsonConvert<${itemClass}>{"
                } else if (file.text.contains("with JsonConvert<${itemClass}>  {")) {
                    "with JsonConvert<${itemClass}>  {"
                } else if (file.text.contains("with JsonConvert<${itemClass}>\n{")) {
                    "with JsonConvert<${itemClass}>\n{"
                } else if (file.text.contains("with JsonConvert<${itemClass}>\t{")) {
                    "with JsonConvert<${itemClass}>\t{"
                } else if (file.text.contains("with JsonConvert<${itemClass}>\n\t{")) {
                    "with JsonConvert<${itemClass}>\n\t{"
                } else if (file.text.contains("extends JsonConvert<${itemClass}> {")) {
                    "extends JsonConvert<${itemClass}> {"
                } else if (file.text.contains("extends JsonConvert<${itemClass}>{")) {
                    "extends JsonConvert<${itemClass}>{"
                } else if (file.text.contains("extends JsonConvert<${itemClass}>  {")) {
                    "extends JsonConvert<${itemClass}>  {"
                } else if (file.text.contains("extends JsonConvert<${itemClass}>\n{")) {
                    "extends JsonConvert<${itemClass}>\n{"
                } else if (file.text.contains("extends JsonConvert<${itemClass}>\t{")) {
                    "extends JsonConvert<${itemClass}>\t{"
                } else if (file.text.contains("extends JsonConvert<${itemClass}>\n\t{")) {
                    "extends JsonConvert<${itemClass}>\n\t{"
                } else {
                    "JsonConvert<${itemClass}> {"
                }
                addImport += """
${
                    file.text.lines().filterNot { itemLine ->
                        itemLine.contains("json_convert_content.dart")
                    }.joinToString("\n").replace("class $itemClass ", "@JsonSerializable()\nclass $itemClass ")
                        .replace("class $itemClass\n", "@JsonSerializable()\nclass $itemClass ")
                        .replace(
                            replaceWith,
                            "{\n\n\t${itemClass}();" +
                                    "\n\n\tfactory ${itemClass}.fromJson(Map<String, dynamic> json) => $${itemClass}FromJson(json);\n\n" +
                                    "\tMap<String, dynamic> toJson() => $${itemClass}ToJson(this);\n"
                        )
                }
                """.trimIndent()
            }
            file.virtualFile.commitContent(
                project, """
$addImport
            """.trimIndent()
            )

        }
    }
}