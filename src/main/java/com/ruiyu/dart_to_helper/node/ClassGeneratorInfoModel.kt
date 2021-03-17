package com.ruiyu.dart_to_helper.node

import com.ruiyu.jsontodart.utils.*
import com.ruiyu.utils.toLowerCaseFirstOne


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
        fields.add(Filed(type, name, isLate).apply {
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
        sb.append("${className.toLowerCaseFirstOne()}FromJson(${className} data, Map<String, dynamic> json) {\n")
        fields.forEach { k ->
            //如果deserialize不是false,那么就解析,否则不解析
            if (k.getValueByName<Boolean>("deserialize") != false) {
                sb.append("\t${jsonParseExpression(k)}\n")
            }
        }
        sb.append("\treturn data;\n")
        sb.append("}")
        return sb.toString()
    }

    private fun jsonParseExpression(filed: Filed): String {
        val type = filed.type.replace("?","")
        val name = filed.name
        //从json里取值的名称
        val getJsonName = filed.getValueByName("name") ?: name
        //是否是list
        val isListType = isListType(type)
        return when {
            //是否是基础数据类型
            isPrimitiveType(type) -> {
                when {
                    isListType -> {
                        "if (json['$getJsonName'] != null) {\n" +
                                "\t\tdata.$name = (json['$getJsonName'] as List).map((v) => ${
                                    buildToType(
                                        getListSubType(type),
                                        "v"
                                    )
                                }).toList().cast<${getListSubType(type)}>();\n" +
                                "\t}"
                    }
                    type == "DateTime" -> {
                        "if(json['$getJsonName'] != null){\n\t\tdata.$name = DateTime.parse(json['$getJsonName']);\n\t}"
                    }
                    else -> {
                        "if (json['$getJsonName'] != null) {\n\t\tdata.$name = ${
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
                        "\t\tdata.$name = (json['$getJsonName'] as List).map((v) => ${listSubType}().fromJson(v)).toList();\n" +
                        "\t}"
            }
            else -> // class
                "if (json['$getJsonName'] != null) {\n\t\tdata.$name = ${type.replace("?","")}().fromJson(json['$getJsonName']);\n\t}"
        }
    }

    //生成tojson方法
    private fun jsonGenFunc(): String {
        val sb = StringBuffer();
        sb.append("Map<String, dynamic> ${className.toLowerCaseFirstOne()}ToJson(${className} entity) {\n");
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
            //是否是基础数据类型
            isPrimitiveType(type) -> {
                return when {
                    isListType && getListSubType(type) == "DateTime" -> {
                        "${thisKey}\n" +
                                "        ${isLateCallSymbol(filed.isLate)}map((v) => v?.toString())\n" +
                                "        ${isLateCallSymbol(filed.isLate)}toList()\n" +
                                "        ${isLateCallSymbol(filed.isLate)}cast<String>();"
                    }
                    type == "DateTime" -> {
                        "data['${getJsonName}'] = ${thisKey}?.toString();"
                    }
                    else -> "data['$getJsonName'] = $thisKey;"
                }
            }
            isListType -> {
                //如果泛型里包含?,那么就需要?调用了
                val nullType = if (getListSubTypeCanNull(type).last() == '?') "?." else "."
                //类名
                val value =
                    "$thisKey${isLateCallSymbol(filed.isLate)}map((v) => v${nullType}toJson())${isLateCallSymbol(filed.isLate)}toList()"
                // class list
                return "data['$getJsonName'] =  $value;"
            }
            else -> {
                // class
                return "data['$getJsonName'] = ${thisKey}${isLateCallSymbol(filed.isLate)}toJson();"
            }
        }
    }

    private fun isLateCallSymbol(isLate: Boolean): String {
        return if (isLate) {
            return "."
        } else "?."
    }


    private fun buildToType(typeName: String, value: String): String {
        return when {
            typeName.equals("int", true) -> {
                "$value is String\n" +
                        "\t\t\t\t? int.tryParse(${value})\n" +
                        "\t\t\t\t: ${value}.toInt()"
            }
            typeName.equals("double", true) -> {
                "$value is String\n" +
                        "\t\t\t\t? double.tryParse(${value})\n" +
                        "\t\t\t\t: ${value}.toDouble()"
            }
            typeName.equals("num", true) -> {
                "$value is String\n" +
                        "\t\t\t\t? num.tryParse(${value})\n" +
                        "\t\t\t\t: $value"
            }
            typeName.equals("string", true) -> {
                "${value}.toString()"
            }
            typeName.equals("DateTime", true) -> {
                "DateTime.parse(${value})"
            }
            else -> value
        }
    }


}

class Filed constructor(
    //字段类型
    var type: String,
    //字段名字
    var name: String,
    //是否是late修饰
    var isLate: Boolean
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
