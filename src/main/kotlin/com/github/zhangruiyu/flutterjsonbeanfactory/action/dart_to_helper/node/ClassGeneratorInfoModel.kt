package com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.node

import com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.model.FieldClassTypeInfo
import com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart.utils.*
import com.github.zhangruiyu.flutterjsonbeanfactory.setting.Settings
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toLowerCaseFirstOne
import com.intellij.openapi.application.ApplicationManager


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


    fun addFiled(
        type: String,
        typeNodeInfo: FieldClassTypeInfo,
        name: String,
        isLate: Boolean,
        annotationValue: List<AnnotationValue>?
    ) {
        //如果是?结尾是可空类型
        fields.add(
            Filed(
                if (type.endsWith("?")) type.take(type.length - 1) else type,
                typeNodeInfo,
                name,
                isLate,
                type.endsWith("?")
            ).apply {
                this.annotationValue = annotationValue
            })
//        fields.forEach {
//            it.genFromJson()
//        }
    }


    override fun toString(): String {
        val sb = StringBuffer()
        sb.append(jsonParseFunc())
        sb.append("\n")
        sb.append("\n")
        sb.append(jsonGenFunc())
        sb.append(copyWithFunc())
        return sb.toString()
    }

    //生成fromjson方法
    private fun jsonParseFunc(): String {
        val sb = StringBuffer();
        sb.append("\n")
        sb.append("$className \$${className}FromJson(Map<String, dynamic> json) {\n")
        val classInstanceName = className.toLowerCaseFirstOne()
        sb.append("\tfinal $className $classInstanceName = ${className}();\n")
        fields.forEach { k ->
            //如果deserialize不是false,那么就解析,否则不解析
            if (k.getValueByName<Boolean>("deserialize") != false) {
//                sb.append("\t${jsonParseExpression(k, classInstanceName)}\n")
                sb.append(k.generateFromJsonField(classInstanceName))
            }
        }
        sb.append("\treturn ${classInstanceName};\n")
        sb.append("}")
        return sb.toString()
    }


    //生成tojson方法
    private fun jsonGenFunc(): String {
        val sb = StringBuffer();
        sb.append("Map<String, dynamic> \$${className}ToJson(${className} entity) {\n");
        sb.append("\tfinal Map<String, dynamic> data = <String, dynamic>{};\n");
        fields.forEach { k ->
            //如果serialize不是false,那么就解析,否则不解析
            if (k.getValueByName<Boolean>("serialize") != false) {
                sb.append("\t${k.toJsonExpression()}\n")
            }
        }
        sb.append("\treturn data;\n");
        sb.append("}");
        return sb.toString()
    }


    private fun copyWithFunc(): String {
        val sb = StringBuffer()
        sb.append("\n")
        sb.append("\n")
        sb.append("extension ${className}Ext on $className {")
        sb.append("\n")
        sb.append("\t$className copyWith({")
        sb.append("\n")
        fields.forEach {
            sb.append("\t${it.type}? ${it.name},\n")
        }
        sb.append("\t}) {\n")
        sb.append("\t\treturn $className()")
        fields.forEach {
            sb.append("\n\t\t\t..${it.name} = ${it.name} ?? this.${it.name}")
        }
        sb.append(";")
        sb.append("}")
        sb.append("}")
        return sb.toString()

    }


}

class Filed(
    //字段类型
    var type: String,
    var typeNodeInfo: FieldClassTypeInfo,
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

    /**
     * 生成formJson 字段
     */
    fun generateFromJsonField(classInstanceName: String): String {
        val sb = StringBuffer()
        //class里的字段名
        //从json里取值的名称
        val jsonName = getValueByName("name") ?: name
        ///如果是dynamic那么不写?
        val typeNullString = if (typeNodeInfo.primaryType == "dynamic" || typeNodeInfo.primaryType == "var") "" else "?"
        val a = "final ${typeNodeInfo.primaryType + (typeNodeInfo.genericityString ?: "")}${typeNullString} $name = ${
            generateFromJsonByType(
                "json['${jsonName}']",
                true,
                typeNodeInfo
            )
        };"
        println("打印\n")
        println(a)
        sb.append(a)
        sb.append("\tif (${name} != null) {\n")
        sb.append("\t\t${classInstanceName}.$name = $name;")
        sb.append("\n")
        sb.append("\t}")
        return sb.toString()
    }

    /**
     * isRoot 如果是最外层的话,那么不写as
     */
    private fun generateFromJsonByType(value: String, isRoot: Boolean, typeNodeInfo: FieldClassTypeInfo?): String {

        println("genFromType\n $value")
        println("genFromType\n $typeNodeInfo")
        return if (typeNodeInfo?.isMap() == true) {
            val a = generateFromJsonMap(value, typeNodeInfo)
            println("打印\n")
            println(a)
            a
        } else if (typeNodeInfo?.isList() == true || typeNodeInfo?.isSet() == true) {
            generateFromJsonList(value, typeNodeInfo)
        } else {
            if (typeNodeInfo?.primaryType == null || typeNodeInfo.primaryType == "dynamic" || typeNodeInfo.primaryType == "var") {
                value
            } else {
                val sb = StringBuffer()

                sb.append("jsonConvert.convert<${typeNodeInfo.primaryType}>(${value}")
                ///是否是枚举
                val isEnum = getValueByName<Boolean>("isEnum") == true
                if (isEnum) {
                    sb.append(", enumConvert: (v) => ${typeNodeInfo.primaryType}.values.byName(v)")
                }
                sb.append(")")
                if (!typeNodeInfo.nullable && !isRoot) {
                    sb.append(
                        " as ${typeNodeInfo.primaryType}"
                    )
                }
                sb.toString()
            }
        }
    }

    fun generateFromJsonMap(value: String, typeNodeInfo: FieldClassTypeInfo?): String {
        val nullString = nullString(typeNodeInfo?.nullable == true)
        val sb = StringBuffer()
        sb.append("\n")
        sb.append("\t\t")
        sb.append("(${value} as Map<String, dynamic>${nullString})${nullString}.map(")
        sb.append("\n")
        sb.append("\t")
        sb.append("(k, e) => MapEntry(k,")
        if (typeNodeInfo?.nullable == true) {
            sb.append("\te == null ? null : ")
        }
        val genericityChildType = typeNodeInfo?.genericityChildType?.genericityChildType
        sb.append(
            generateFromJsonByType(
                "e",
                false,
                genericityChildType
            )
        )
        ///上面generateFromJsonByType已经添加了 那么这里就需要写了
//        ///并且不是list,如果是list的话那么就会有警告,因为不用转list了
//        if (genericityChildType?.nullable != true && genericityChildType?.isList() != true) {
//            sb.append(
//                " as ${genericityChildType?.primaryType}${
//                    nullString(
//                        genericityChildType?.nullable
//                    )
//                }"
//            )
//        }
        //MapEntry的括号
        sb.append(")")
        //map的括号
        sb.append(")")
        return sb.toString()
    }

    fun generateFromJsonList(value: String, typeNodeInfo: FieldClassTypeInfo?): String {
        val sb = StringBuffer()
        val nullString = nullString(typeNodeInfo?.nullable == true)
        sb.append("(${value} as ${typeNodeInfo?.primaryType}<dynamic>${nullString})${nullString}.map(")
        sb.append("\n")
        sb.append("\t")
        val genericityChildType = typeNodeInfo?.genericityChildType
        sb.append("(e) => ${generateFromJsonByType("e", false, genericityChildType)}")
        ///上面generateFromJsonByType已经添加了 那么这里就需要写了
//        if (genericityChildType?.nullable != true) {
//            ///并且不是map,list,如果是的话那么就会有警告,因为不用转了
//            if (genericityChildType?.isMap() != true && genericityChildType?.isList() != true) {
//                sb.append(
//                    " as ${genericityChildType?.primaryType}${genericityChildType?.genericityString ?: ""}${
//                        nullString(
//                            genericityChildType?.nullable
//                        )
//                    }"
//                )
//            }
//
//        }
        sb.append(")")
        sb.append(".to${typeNodeInfo?.primaryType}()")
        return sb.toString()
    }

    /**
     * 是否为null的字符串
     */
    private fun nullString(nullable: Boolean?): String {
        return if (nullable == true) {
            "?"
        } else {
            ""
        }
    }

    fun toJsonExpression(): String {
        val type = type
        val name = name
        //从json里取值的名称
        val getJsonName = getValueByName("name") ?: name
        val thisKey = "entity.$name"
        val isEnum = getValueByName<Boolean>("isEnum") == true
        when {
            typeNodeInfo.isList() || typeNodeInfo.isSet() -> {
                //1判断是否是基础数据类型
                //1.1拿到List的泛型
                val listSubType = typeNodeInfo.genericityChildType?.primaryType ?: "dynamic"
                //1.2判断是否是基础数据类型
                val value = if (isEnum) {
                    "$thisKey${nullString(typeNodeInfo.nullable)}.map((v) => v${nullString(typeNodeInfo.genericityChildType?.nullable)}.name).to${typeNodeInfo.primaryType}()"
                } else if (isBaseType(listSubType)) {
                    if (listSubType == "DateTime") {
                        "$thisKey${nullString(typeNodeInfo.nullable)}.map((v) => v${nullString(typeNodeInfo.genericityChildType?.nullable)}.toIso8601String()).to${typeNodeInfo.primaryType}()"
                    } else {
                        thisKey
                    }

                } else {
                    //类名
                    "$thisKey${nullString(typeNodeInfo.nullable)}.map((v) => v${nullString(typeNodeInfo.genericityChildType?.nullable)}.toJson()).to${typeNodeInfo.primaryType}()"
                }

                // class list
                return "data['$getJsonName'] =  $value;"
            }
            //是否是枚举
            isEnum -> {
                return "data['$getJsonName'] = $thisKey${nullString(typeNodeInfo.nullable)}.name;"
            }
            //是否是基础数据类型
            isBaseType(type) -> {
                return when (type) {
                    "DateTime" -> {
                        "data['${getJsonName}'] = ${thisKey}${nullString(typeNodeInfo.nullable)}.toIso8601String();"
                    }

                    else -> "data['$getJsonName'] = $thisKey;"
                }
            }
            //是map或者set
            typeNodeInfo.isMap() || typeNodeInfo.isSet() -> {
                return "data['$getJsonName'] = $thisKey;"
            }
            // class
            else -> {
                return "data['$getJsonName'] = ${thisKey}${nullString(typeNodeInfo.nullable)}.toJson();"
            }
        }
    }

}

@Suppress("UNCHECKED_CAST")
class AnnotationValue(val name: String, private val value: Any) {
    fun <T> getValueByName(): T {
        return value as T
    }
}
