package com.ruiyu.dart_to_helper.node

import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.CompositeElement
import com.jetbrains.lang.dart.DartElementType
import com.jetbrains.lang.dart.DartTokenTypes
import org.jetbrains.kotlin.psi.psiUtil.children

object GeneratorDartClassNodeToHelperInfo {
    fun getDartFileHelperClassGeneratorInfo(file: PsiFile): HelperFileGeneratorInfo? {
        //不包含JsonConvert 那么就不转
        if (file.text.contains("with JsonConvert").not() && file.text.contains("extends JsonConvert").not()) {
            return null
        }
        val mutableMapOf = mutableListOf<HelperClassGeneratorInfo>()
        val imports: MutableList<String> = mutableListOf()
        file.children.forEach {
            val text = it.text
            val classNode = it?.node
            //是类
            if (classNode?.elementType == DartTokenTypes.CLASS_DEFINITION) {
                if (classNode is CompositeElement) {
                    val helperClassGeneratorInfo = HelperClassGeneratorInfo()
                    for (filedAndMethodNode in classNode.children()) {
                        val toBinaryName = filedAndMethodNode.elementType.toString()
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
                                                println("普通解析 $nameNode $typeNode")
                                                //不是注解,普通解析
                                                when {
                                                    fieldWholeNode.text == "late" -> {
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
//                                                println("普通解析类型 ${itemFieldNode.elementType}")
//                                                println("普通解析类型文本 ${itemFieldNode.text}")
                                            }

                                        }
                                        helperClassGeneratorInfo.addFiled(typeNode!!, nameNode!!, isLate, allAnnotation)
                                    }
                                    var text4 = itemFileNode.text
                                    var text5 = itemFileNode.text
                                }
                                val text2 = itemFile.text
                                val text3 = itemFile.text
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