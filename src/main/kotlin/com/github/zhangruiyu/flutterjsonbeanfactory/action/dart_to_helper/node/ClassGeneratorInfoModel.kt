package com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.node

import com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart.utils.*
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toLowerCaseFirstOne


/**
 * User: zhangruiyu
 * Date: 2019/12/23
 * Time: 11:32
 */
class HelperFileGeneratorInfo(
    val imports: MutableList<String> = mutableListOf(),
    val classes: MutableList<HelperClassGeneratorInfo> = mutableListOf()
)

class HelperClassGeneratorInfo {
    //协助的类名
    lateinit var className: String
    val fields: MutableList<Filed> = mutableListOf()


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
        sb.append("$className \$${className}FromJson(Map<String, dynamic> json) {\n")
        val classInstanceName = className.toLowerCaseFirstOne()
        sb.append("\t$className $classInstanceName = ${className}();\n")
        fields.forEach { k ->
            //如果deserialize不是false,那么就解析,否则不解析
            if (k.getValueByName<Boolean>("deserialize") != false) {
                sb.append("\t${jsonParseExpression(k, classInstanceName)}\n")
            }
        }
        sb.append("\treturn ${classInstanceName};\n")
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
            if (getListSubTypeCanNull(type).contains("?")) {
                stringBuilder.append("var $classFieldName = jsonConvert.convertList<${getListSubType(type)}>(json['${getJsonName}']);\n")
            } else {
                stringBuilder.append("var $classFieldName = jsonConvert.convertListNotNull<${getListSubType(type)}>(json['${getJsonName}']);\n")
            }

        } else {
            stringBuilder.append("var $classFieldName = jsonConvert.convert<${type}>(json['${getJsonName}']);\n")
        }
        stringBuilder.append("\tif (${classFieldName} != null) {\n")
        stringBuilder.append("\t\t${classInstanceName}.$classFieldName = $classFieldName;")
        stringBuilder.append("\n")
        stringBuilder.append("\t}")
        return stringBuilder.toString()
        /*return when {
            //是否是基础数据类型
            isPrimitiveType(type) -> {
                when {
                    isListType -> {
                        "if (json['$getJsonName'] != null) {\n" +
                                "\t\tdata.$classFieldName = (json['$getJsonName'] as List).map((v) => ${
                                    buildToType(
                                        getListSubType(type),
                                        "v"
                                    )
                                }).toList().cast<${getListSubType(type)}>();\n" +
                                "\t}"
                    }
                    type == "DateTime" -> {
                        "if(json['$getJsonName'] != null){\n\t\tdata.$classFieldName = DateTime.parse(json['$getJsonName']);\n\t}"
                    }
                    else -> {
                        "if (json['$getJsonName'] != null) {\n\t\tdata.$classFieldName = ${
                            buildToType(
                                type,
                                "json['$getJsonName']"
                            )
                        };\n\t}"
                    }
                }
            }
            isListType -> { // list of class  //如果是list,就把名字修改成单数
                //类名
                val listSubType = getListSubType(type)
                "if (json['$getJsonName'] != null) {\n" +
                        "\t\tdata.$classFieldName = (json['$getJsonName'] as List).map((v) => ${listSubType}.fromJson(v)).toList();\n" +
                        "\t}"
            }
            else -> // class
                "if (json['$getJsonName'] != null) {\n\t\tdata.$classFieldName = ${type}.fromJson(json['$getJsonName']);\n\t}"
        }*/
    }

    //生成tojson方法
    private fun jsonGenFunc(): String {
        val sb = StringBuffer();
        sb.append("Map<String, dynamic> \$${className}ToJson(${className} entity) {\n");
        sb.append("\tfinal Map<String, dynamic> data = new Map<String, dynamic>();\n");
        fields.forEach { k ->
            //如果serialize不是false,那么就解析,否则不解析
            if (k.getValueByName<Boolean>("serialize") != false) {
                sb.append("\t${toJsonExpression(k)}\n")
            }
        }
        sb.append("\treturn data;\n");
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
                        "$thisKey${if (filed.isCanNull) "?." else "."}map((v) => v${if (listSubType.endsWith("?")) "?." else "."}toIso8601String()).toList()"
                    } else {
                        thisKey
                    }

                } else {
                    //类名
                    val nullType = if (filed.isCanNull) "?." else "."
                    "$thisKey${nullType}map((v) => v${if (listSubType.endsWith("?")) "?." else "."}toJson()).toList()"
                }

                // class list
                return "data['$getJsonName'] =  $value;"
            }
            //是否是基础数据类型
            isBaseType(type) -> {
                return when (type) {
                    "DateTime" -> {
                        val nullType = if (filed.isCanNull) "?." else "."
                        "data['${getJsonName}'] = ${thisKey}${nullType}toIso8601String();"
                    }
                    else -> "data['$getJsonName'] = $thisKey;"
                }
            }
            // class
            else -> {
                val nullType = if (filed.isCanNull) "?." else "."
                return "data['$getJsonName'] = ${thisKey}${nullType}toJson();"
            }
        }
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
