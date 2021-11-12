package com.ruiyu.dart_to_helper.node

import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.CompositeElement
import com.jetbrains.lang.dart.DartElementType
import com.jetbrains.lang.dart.DartTokenTypes
import org.jetbrains.kotlin.psi.psiUtil.children

object GeneratorDartClassNodeToHelperInfo {
    val notSupportType = listOf("static", "const")
    val enums: MutableSet<String> = mutableSetOf()
    fun getDartFileHelperClassGeneratorInfo(file: PsiFile): HelperFileGeneratorInfo? {
        //不包含JsonConvert 那么就不转
        if (file.text.contains("JsonConvert<").not() || file.name == "json_convert_content.dart") {
            return null
        }
        val mutableMapOf = mutableListOf<HelperClassGeneratorInfo>()
        val imports: MutableList<String> = mutableListOf()
        file.children.forEach {
            val text = it.text
            val classNode = it?.node
            if (classNode?.elementType == DartTokenTypes.ENUM_DEFINITION) {
                if (classNode is CompositeElement) {
                    for (filedAndMethodNode in classNode.children()) {
                        if (filedAndMethodNode.elementType == DartTokenTypes.COMPONENT_NAME) {
                            println("发现枚举 ${filedAndMethodNode.text}")
                            enums.add(filedAndMethodNode.text)
                        }
                    }
                }
            }
            //是类
            else if (classNode?.elementType == DartTokenTypes.CLASS_DEFINITION && (text.contains("with") || text.contains("extends")) && text.contains(
                            "JsonConvert<"
                    )
            ) {
                if (classNode is CompositeElement) {
                    val helperClassGeneratorInfo = HelperClassGeneratorInfo()
                    for (filedAndMethodNode in classNode.children()) {
                        val nodeName = filedAndMethodNode.text
                        //是类里字段
                        if (filedAndMethodNode.elementType == DartTokenTypes.CLASS_BODY) {
                            filedAndMethodNode.children().forEach { itemFile ->
                                itemFile.children().forEach { itemFileNode ->
                                    //itemFileNode text : int code
                                    if (itemFileNode.elementType == DartTokenTypes.VAR_DECLARATION_LIST) {
                                        var nameNode: String? = null
                                        var typeNode: String? = null
                                        var isLate = false
                                        var isEnum = false
                                        //当前字段的所有注解
                                        val allAnnotation = mutableListOf<AnnotationValue>()
                                        itemFileNode.firstChildNode.children().forEach { fieldWholeNode ->
                                            //如果第一个是注解,解析注解里的内容
                                            if (fieldWholeNode.elementType == DartTokenTypes.METADATA) {
                                                val annotationWholeNode = fieldWholeNode.firstChildNode;
                                                //@JSONField(name: 'app',serialize:true) 为例
                                                if (
                                                //@
                                                        annotationWholeNode.text == "@" &&
                                                        //JSONField
                                                        fieldWholeNode.firstChildNode.treeNext.elementType == DartTokenTypes.REFERENCE_EXPRESSION && fieldWholeNode.firstChildNode.treeNext.text == "JSONField"
                                                ) {

                                                    if (fieldWholeNode.firstChildNode.treeNext.treeNext.elementType == DartTokenTypes.ARGUMENTS) {
                                                        fieldWholeNode.firstChildNode.treeNext.treeNext.children()
                                                                .forEach { onlyItemWholeMetaValueDataNode ->
                                                                    //onlyItemWholeMetaValueDataNode 只有注解的多个内容:name: 'app',serialize:true
                                                                    if (onlyItemWholeMetaValueDataNode.elementType == DartTokenTypes.ARGUMENT_LIST) {
                                                                        println("注解22 ${onlyItemWholeMetaValueDataNode.text}")

                                                                        onlyItemWholeMetaValueDataNode.children()
                                                                                .forEach { onlyItemMetaValueDataNode ->
                                                                                    // onlyItemMetaValueDataNode  只有注解的单个内容:name: 'app'
                                                                                    println("注解33 ${onlyItemMetaValueDataNode.text}")
                                                                                    if (onlyItemMetaValueDataNode.elementType == DartTokenTypes.NAMED_ARGUMENT) {
                                                                                        var annotationName: String? = null
                                                                                        var annotationValue: Any? = null
                                                                                        onlyItemMetaValueDataNode.children()
                                                                                                .forEach { onlyItemNamedArgumentValueDataNode ->
                                                                                                    //注解里内容的名字  name: 'app' 里的name
                                                                                                    when (onlyItemNamedArgumentValueDataNode.elementType) {
                                                                                                        DartTokenTypes.PARAMETER_NAME_REFERENCE_EXPRESSION -> {
                                                                                                            annotationName =
                                                                                                                    onlyItemNamedArgumentValueDataNode.text.replace(
                                                                                                                            "\'",
                                                                                                                            ""
                                                                                                                    ).replace("\"", "")
                                                                                                        }
                                                                                                        DartTokenTypes.STRING_LITERAL_EXPRESSION -> {
                                                                                                            annotationValue =
                                                                                                                    onlyItemNamedArgumentValueDataNode.text.replace(
                                                                                                                            "\'",
                                                                                                                            ""
                                                                                                                    ).replace("\"", "")
                                                                                                        }
                                                                                                        DartTokenTypes.LITERAL_EXPRESSION -> {
                                                                                                            annotationValue =
                                                                                                                    (onlyItemNamedArgumentValueDataNode.text == "true")
                                                                                                        }
                                                                                                    }

                                                                                                    println("注解的内容 ${onlyItemNamedArgumentValueDataNode.text}")
                                                                                                }
                                                                                        if (annotationName != null && annotationValue != null) {
                                                                                            //注解的实际内容
                                                                                            allAnnotation.add(
                                                                                                    AnnotationValue(
                                                                                                            annotationName!!,
                                                                                                            annotationValue!!
                                                                                                    )
                                                                                            )
                                                                                        }
                                                                                    }

                                                                                }
                                                                    }

                                                                }
                                                    }
                                                }
                                            } else {
                                                val isVar =
                                                        fieldWholeNode.text == "var"
                                                fieldWholeNode.children().forEach {
                                                    println("普通解析222 ${it.firstChildNode.text}")
                                                }
                                                println("普通解析 $nameNode $typeNode")

                                                //不是注解,普通解析
                                                when {
                                                    fieldWholeNode.text == "late" || fieldWholeNode.text == "=" -> {
                                                        isLate = true
                                                    }
                                                    fieldWholeNode.elementType == DartTokenTypes.TYPE || isVar -> {
                                                        typeNode = fieldWholeNode.text
                                                    }
                                                    fieldWholeNode.elementType == DartTokenTypes.COMPONENT_NAME -> {
                                                        nameNode = fieldWholeNode.text
                                                    }
                                                    //  println("普通解析类型 ${itemFieldNode.elementType}")
                                                    //  println("普通解析类型文本 ${itemFieldNode.text}")
                                                }
                                                if (enums.contains(fieldWholeNode.text)) {
                                                    isEnum = true
                                                }
                                                if (fieldWholeNode.elementType is DartElementType) {
                                                    if (notSupportType.contains(fieldWholeNode.text)) {
                                                        val errorMessage =
                                                                "This file contains code that cannot be parsed: ${file.name}. content: ${nodeName}. type not supported ,such as ${notSupportType.joinToString()}"
                                                        throw RuntimeException(errorMessage)
                                                    }
                                                }
                                                println("普通解析类型文本 ${fieldWholeNode.text} 普通解析类型 ${fieldWholeNode.elementType}")
                                            }

                                        }
                                        //如果不是late,但是最后一行包括=号,说明默认赋值了
                                        if (!isLate && itemFileNode.lastChildNode.text.contains("=")) {
                                            isLate = true
                                        }
                                        helperClassGeneratorInfo.addFiled(typeNode!!, nameNode!!, isLate, isEnum, allAnnotation)
                                    }
//                                    var text4 = itemFileNode.text
//                                    var text5 = itemFileNode.text
                                }
//                                val text2 = itemFile.text
//                                val text3 = itemFile.text
                            }
                        } else if (filedAndMethodNode.elementType == DartTokenTypes.COMPONENT_NAME) {
                            helperClassGeneratorInfo.className = (nodeName)
                        } else if (filedAndMethodNode.elementType == DartTokenTypes.MIXINS) {
                            //不包含JsonConvert 那么就不转
                            if (nodeName.contains("JsonConvert").not()) {
                                continue
                            }
                        }

                    }
                    mutableMapOf.add(helperClassGeneratorInfo)
//                    classNode.children() {filedAndMethodNode->
//                        val text1 = filedAndMethodNode.text
//                    }
                }
            } else if (classNode?.elementType == DartTokenTypes.IMPORT_STATEMENT) {
                imports.add(text)
            }

            /* it?.node?.children()?.forEach {
                 val toString = it?.firstChildNode?.toString()
                 val toString33 = it?.lastChildNode?.toString()
            }*/
        }
        return if (mutableMapOf.isEmpty()) null else HelperFileGeneratorInfo(imports, mutableMapOf)
    }
}