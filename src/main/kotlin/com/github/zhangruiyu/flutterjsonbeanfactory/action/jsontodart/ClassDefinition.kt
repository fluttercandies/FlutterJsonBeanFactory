package com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart

import com.intellij.openapi.application.ApplicationManager
import com.github.zhangruiyu.flutterjsonbeanfactory.action.jsontodart.utils.*
import com.github.zhangruiyu.flutterjsonbeanfactory.setting.Settings
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.FieldUtils
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toLowerCaseFirstOne
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.toUpperCaseFirstOne

class ClassDefinition(private val name: String, private val privateFields: Boolean = false) {
    ///map的key是json的key,没有转成dart的命名方式
    val fields = mutableMapOf<String, TypeDefinition>()
    val dependencies: List<Dependency>
        get() {
            val dependenciesList = mutableListOf<Dependency>()
            fields.forEach {
                ///如不不是主类型
                if (it.value.isPrimitive.not()) {
                    dependenciesList.add(Dependency(it.key, it.value))
                }
            }
            return dependenciesList;
        }

    fun addField(key: String, typeDef: TypeDefinition) {
        fields[key] = typeDef
    }

    fun hasField(otherField: TypeDefinition): Boolean {
        return fields.keys.firstOrNull { k -> fields[k] == otherField } != null
    }

    override operator fun equals(other: Any?): Boolean {
        if (other is ClassDefinition) {
            if (name != other.name) {
                return false;
            }
            return fields.keys.firstOrNull { k ->
                other.fields.keys.firstOrNull { ok ->
                    fields[k] == other.fields[ok]
                } == null
            } == null
        }
        return false
    }

    private fun _addTypeDef(typeDef: TypeDefinition, sb: StringBuffer, prefix: String, suffix: String) {
        if (typeDef.name == "Null") {
            sb.append("dynamic")
        } else {
            sb.append(prefix)
            sb.append(typeDef.name)
            if (typeDef.subtype != null) {
                //如果是list,就把名字修改成单数
                sb.append("<${typeDef.subtype!!}>")
            }
            sb.append(suffix)
        }
    }

    private fun _addCopyWithTypeDef(typeDef: TypeDefinition, sb: StringBuffer, suffix: String) {
        if (typeDef.name == "Null") {
            sb.append("dynamic")
        } else {
            sb.append(typeDef.name)
            if (typeDef.subtype != null) {
                //如果是list,就把名字修改成单数
                sb.append("<${typeDef.subtype!!}>")
            }
            sb.append(suffix)
        }
    }

    //字段的集合
    private val _fieldList: String
        get() {
            val settings = ApplicationManager.getApplication().getService(Settings::class.java)
            val isOpenNullAble = settings.isOpenNullAble == true
            val setDefault = settings.setDefault == true

            val suffix = if (isOpenNullAble) "?" else ""
            return fields.keys.map { key ->
                val f = fields[key]!!
                ///如果不是 主类型或着List类型,那就当类判断
                val isClass = (f.isPrimitive || isListType(f.name)).not()
                ///如果是类
                val prefix = if (isClass) {
                    ///如果没开可空
                    if (!isOpenNullAble) {
                        "late "
                    } else {
                        ""
                    }
                } else {
                    ///这里是正常字段
                    ///没有开启可空,没有设置默认值,并且不是Primitive
                    if ((!isOpenNullAble && !setDefault)) "late " else ""
                }

                ///给key转成dart写法
                val fieldName = FieldUtils.toFieldTypeName(key).toLowerCaseFirstOne()
                val sb = StringBuffer();
                //如果驼峰命名后不一致,才这样
                if (fieldName != key) {
                    sb.append('\t')
                    sb.append("@JSONField(name: \'${key}\')\n")
                }
                sb.append('\t')
                _addTypeDef(f, sb, prefix, suffix)
                sb.append(" $fieldName")
                if (settings.setDefault == true) {
                    if (isListType(f.name)) {
                        if (settings.listFieldDefaultValue()
                                ?.isNotEmpty() == true
                        ) {
                            sb.append(" = ${settings.listFieldDefaultValue()}")
                        }
                    } else if (f.subtype == null) {
                        if (f.name == "String" && settings.stringFieldDefaultValue()?.isNotEmpty() == true) {
                            sb.append(" = ${settings.stringFieldDefaultValue()}")
                        } else if (f.name == "bool" && settings.boolFieldDefaultValue()?.isNotEmpty() == true) {
                            sb.append(" = ${settings.boolFieldDefaultValue()}")
                        } else if (f.name == "int" && settings.intFieldDefaultValue()?.isNotEmpty() == true) {
                            sb.append(" = ${settings.intFieldDefaultValue()}")
                        } else if (f.name == "double" && settings.doubleFieldDefaultValue()?.isNotEmpty() == true) {
                            sb.append(" = ${settings.doubleFieldDefaultValue()}")
                        }
                    }
                }
                sb.append(";")
                return@map sb.toString()
            }.joinToString("\n")
        }


    override fun toString(): String {
        return if (privateFields) {
//            "class $name {\n$_fieldList\n\n$_defaultPrivateConstructor\n\n$_gettersSetters\n\n$_jsonParseFunc\n\n$_jsonGenFunc\n}\n";
            ""
        } else {
            val sb = StringBuffer()
            sb.append("@JsonSerializable()")
            sb.append("\n")
            sb.append("class $name {")
            sb.append("\n")
            sb.append(_fieldList)
            sb.append("\n\n")
            sb.append("\t${name}();")
            sb.append("\n\n")
            sb.append("\tfactory ${name}.fromJson(Map<String, dynamic> json) => \$${name}FromJson(json);")
            sb.append("\n\n")
            sb.append("\tMap<String, dynamic> toJson() => \$${name}ToJson(this);")
            sb.append("\n")
            sb.append("\n")
            sb.append("\t@override")
            sb.append("\n")
            sb.append("\tString toString() {")
            sb.append("\n")
            sb.append("\t\treturn jsonEncode(this);")
            sb.append("\n")
            sb.append("\t}")
            sb.append("\n")
            sb.append("}")
            sb.toString()
        }
    }
}


class Dependency(var name: String, var typeDef: TypeDefinition) {
    val className: String
        get() {
            return FieldUtils.toFieldTypeName(name)
        }

    override fun toString(): String {
        return "name = ${name} ,typeDef = ${typeDef}"
    }
}

class TypeDefinition(var name: String, var subtype: String? = null) {


    val isPrimitive: Boolean = if (subtype == null) {
        isPrimitiveType(name)
    } else {
        isPrimitiveType("$name<${subtype!!.toUpperCaseFirstOne()}>")
    }
    private val isPrimitiveList: Boolean = isPrimitive && name == "List"

    companion object {
        fun fromDynamic(obj: Any?): TypeDefinition {
            val type = getTypeName(obj)
            if (type == "List") {
                val list = obj as List<*>
                val firstElementType = if (list.isNotEmpty()) {
                    getTypeName(list[0])
                } else {
                    // when array is empty insert Null just to warn the user
                    "dynamic"
                }
                return TypeDefinition(type, firstElementType)
            }
            return TypeDefinition(type)
        }
    }


    override operator fun equals(other: Any?): Boolean {
        if (other is TypeDefinition) {
            return name == other.name && subtype == other.subtype;
        }
        return false;
    }


    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (subtype?.hashCode() ?: 0)
        result = 31 * result + isPrimitive.hashCode()
        result = 31 * result + isPrimitiveList.hashCode()
        return result
    }

    override fun toString(): String {
        return "TypeDefinition(name='$name', subtype=$subtype, isPrimitive=$isPrimitive, isPrimitiveList=$isPrimitiveList)"
    }


}