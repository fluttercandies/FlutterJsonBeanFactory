package com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.node

import com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart.utils.*
import com.github.zhangruiyu.flutterjsonbeanfactory.file.FileHelpers
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toLowerCaseFirstOne
import com.intellij.openapi.vfs.VirtualFile


/**
 * User: zhangruiyu
 * Date: 2019/12/23
 * Time: 11:32
 */
class HelperFileGeneratorInfo(
    val directory: VirtualFile?,
    val name: String,
    val partOf: String?,
    val imports: MutableList<String> = mutableListOf(),
    val classes: MutableList<HelperClassGeneratorInfo> = mutableListOf()
)

class HelperClassGeneratorInfo(
    private val isCompat: Boolean = false,
) {
    //协助的类名
    lateinit var className: String

    //类泛型列表
    val genericsType: MutableList<String> = mutableListOf()
    val fields: MutableList<Filed> = mutableListOf()
    private val indent = FileHelpers.indent


    fun addFiled(type: String, name: String, isLate: Boolean, annotationValue: List<AnnotationValue>?) {
        //如果是?结尾是可空类型
        fields.add(
            Filed(
                if (type.endsWith("?")) type.take(type.length - 1) else type,
                name,
                isLate,
                type.endsWith("?")
            ).apply {
                this.annotationValue = annotationValue
            })
    }


    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(jsonParseFunc())
        sb.append("\n")
        sb.append("\n")
        sb.append(jsonGenFunc())
        return sb.toString()
    }

    //生成fromjson方法
    private fun jsonParseFunc(): String {
        val sb = StringBuffer();
        sb.append("\n")
        val classInstanceName = className.toLowerCaseFirstOne()
        if (isCompat) {
            val classGenericType = genericsType.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = "<", postfix = ">")
                .orEmpty()
            val genericFactory = genericsType.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = ", ", postfix = "") { "$it Function(Object? json) fromJson$it" }
                .orEmpty()
            sb.append("$className$classGenericType _\$${className}FromJson$classGenericType(Map<String, dynamic> json$genericFactory) {\n")
            sb.append("${indent}final $className$classGenericType $classInstanceName = ${className}${classGenericType}();\n")
        } else {
            sb.append("$className \$${className}FromJson(Map<String, dynamic> json) {\n")
            sb.append("${indent}final $className $classInstanceName = ${className}();\n")
        }
        fields.forEach { k ->
            //如果deserialize不是false,那么就解析,否则不解析
            if (k.getValueByName<Boolean>("deserialize") != false) {
                sb.append("${indent}${jsonParseExpression(k, classInstanceName)}\n")
            }
        }
        sb.append("${indent}return ${classInstanceName};\n")
        sb.append("}")
        return sb.toString()
    }

    private fun jsonParseExpression(filed: Filed, classInstanceName: String): String {
        val type = filed.type
        //class里的字段名
        val classFieldName = filed.name
        //从json里取值的名称
        val getJsonName = filed.getValueByName("name") ?: classFieldName
        //是否是list
        val isListType = isListType(type)
        val stringBuilder = StringBuilder()
        if (isListType) {
            //如果泛型里带null
            if (getListSubTypeCanNull(type).endsWith("?")) {
                stringBuilder.append(
                    "final List<${getListSubType(type)}?>? $classFieldName = jsonConvert.convertList<${
                        getListSubType(
                            type
                        )
                    }>(json['${getJsonName}']);\n"
                )
            } else {
                stringBuilder.append(
                    "final List<${getListSubType(type)}>? $classFieldName = jsonConvert.convertListNotNull<${
                        getListSubType(
                            type
                        )
                    }>(json['${getJsonName}']);\n"
                )
            }

        } else {
            stringBuilder.append("final ${type}? $classFieldName = jsonConvert.convert<${type}>(json['${getJsonName}']);\n")
        }
        stringBuilder.append("${indent}if (${classFieldName} != null) {\n")
        stringBuilder.append("${indent}${indent}${classInstanceName}.$classFieldName = $classFieldName;")
        stringBuilder.append("\n")
        stringBuilder.append("${indent}}")
        return stringBuilder.toString()
    }

    //生成tojson方法
    private fun jsonGenFunc(): String {
        val sb = StringBuffer();
        if (isCompat) {
            val classGenericType = genericsType.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = "<", postfix = ">")
                .orEmpty()
            val genericFactory = genericsType.takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = ", ", postfix = "") {
                    "Object? Function($it value) toJson$it"
                }
                .orEmpty()
            sb.append("Map<String, dynamic> _\$${className}ToJson${classGenericType}(${className}${classGenericType} entity$genericFactory) {\n");
        } else {
            sb.append("Map<String, dynamic> \$${className}ToJson(${className} entity) {\n");
        }
        sb.append("${indent}final Map<String, dynamic> data = <String, dynamic>{};\n");
        fields.forEach { k ->
            //如果serialize不是false,那么就解析,否则不解析
            if (k.getValueByName<Boolean>("serialize") != false) {
                sb.append("${indent}${toJsonExpression(k)}\n")
            }
        }
        sb.append("${indent}return data;\n");
        sb.append("}");
        return sb.toString()

    }

    private fun toJsonExpression(filed: Filed): String {
        val type = filed.type
        val name = filed.name
        //从json里取值的名称
        val getJsonName = filed.getValueByName("name") ?: name
        //是否是list
        val isListType = isListType(type)
        val thisKey = "entity.$name"
        when {
            isListType -> {
                //1判断是否是基础数据类型
                //1.1拿到List的泛型
                val listSubType = getListSubTypeCanNull(type)
                //1.2判断是否是基础数据类型
                val value = if (isBaseType(listSubType)) {
                    if (listSubType.replace("?", "") == "DateTime") {
                        "$thisKey${if (filed.isCanNull) "?." else "."}map((v) => v${canNullSymbol(listSubType.endsWith("?"))}toIso8601String()).toList()"
                    } else {
                        thisKey
                    }

                } else {
                    //类名
                    val filedGenericType = getListSubType(type)
                    if (isCompat && (filedGenericType in genericsType)) {
                        if (listSubType.endsWith('?')) {
                            "$thisKey${canNullSymbol(filed.isCanNull)}map((e) => JsonConvert.nullableGenericToJson(e, toJson${filedGenericType})).toList()"
                        } else {
                            "$thisKey${canNullSymbol(filed.isCanNull)}map(toJson${filedGenericType}).toList()"
                        }
                    } else {
                        "$thisKey${canNullSymbol(filed.isCanNull)}map((v) => v${canNullSymbol(listSubType.endsWith("?"))}toJson()).toList()"
                    }
                }

                // class list
                return "data['$getJsonName'] =  $value;"
            }
            //是否是基础数据类型
            isBaseType(type) -> {
                return when (type) {
                    "DateTime" -> {
                        "data['${getJsonName}'] = ${thisKey}${canNullSymbol(filed.isCanNull)}toIso8601String();"
                    }
                    else -> "data['$getJsonName'] = $thisKey;"
                }
            }
            //是map
            isMapType(type) -> {
                return "data['$getJsonName'] = $thisKey;"
            }
            // 泛型
            type in genericsType -> {
                return if (filed.isCanNull) {
                    "data['$getJsonName'] = JsonConvert.nullableGenericToJson(${thisKey}, toJson$type);"
                } else {
                    "data['$getJsonName'] = toJson$type(${thisKey});"
                }
            }
            // class
            else -> {
                return "data['$getJsonName'] = ${thisKey}${canNullSymbol(filed.isCanNull)}toJson();"
            }
        }
    }

    private fun canNullSymbol(isCanNull: Boolean): String {
        return if (isCanNull) "?." else "."
    }

}

class Filed constructor(
    //字段类型
    var type: String,
    //字段名字
    var name: String,
    //是否是late修饰
    var isLate: Boolean,
    //是否是可空类型
    var isCanNull: Boolean,
) {

    //待定
    var isPrivate: Boolean? = null

    //注解的值
    var annotationValue: List<AnnotationValue>? = null

    fun <T> getValueByName(name: String): T? {
        return annotationValue?.firstOrNull { it.name == name }?.getValueByName()
    }
}

@Suppress("UNCHECKED_CAST")
class AnnotationValue(val name: String, private val value: Any) {
    fun <T> getValueByName(): T {
        return value as T
    }
}
