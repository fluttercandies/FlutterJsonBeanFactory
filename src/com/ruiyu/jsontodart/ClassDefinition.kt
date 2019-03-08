package com.ruiyu.jsontodart

import com.ruiyu.jsontodart.utils.*
import com.ruiyu.utils.Inflector
import com.ruiyu.utils.toUpperCaseFirstOne

class ClassDefinition(private val name: String, private val privateFields: Boolean = false) {
    val fields = mutableMapOf<String, TypeDefinition>()
    val dependencies: List<Dependency>
        get() {
            val dependenciesList = mutableListOf<Dependency>()
            val keys = fields.keys
            keys.forEach { k ->
                if (fields[k]!!.isPrimitive.not()) {
                    dependenciesList.add(Dependency(k, fields[k]!!))
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

    fun _addTypeDef(typeDef: TypeDefinition, sb: StringBuffer) {
        if(typeDef.name == "Null"){
            sb.append("dynamic")
        }else {
            sb.append(typeDef.name)
        }

        if (typeDef.subtype != null) {
            //如果是list,就把名字修改成单数
            sb.append("<${Inflector.getInstance().singularize(typeDef.subtype!!)}>")
        }
    }

    val _fieldList: String
        get () {
            return fields.keys.map { key ->
                val f = fields[key];
                val fieldName = fixFieldName(key, f, privateFields)
                val sb = StringBuffer();
                sb.append('\t')
                _addTypeDef(f!!, sb)
                sb.append(" $fieldName;")
                return@map sb.toString()
            }.joinToString ( "\n" )
        }

    val _gettersSetters: String
        get() {
            return fields.keys.map { key ->
                val f = fields[key];
                val publicFieldName = fixFieldName(key, f, false);
                val privateFieldName = fixFieldName(key, f, true);
                val sb = StringBuffer();
                sb.append('\t');
                _addTypeDef(f!!, sb);
                sb.append(" get $publicFieldName => $privateFieldName;\n\tset $publicFieldName(")
                _addTypeDef(f, sb)
                sb.append(" $publicFieldName) => $privateFieldName = $publicFieldName;")
                return@map sb.toString()
            }.joinToString ( "\n" )
        }


    val _defaultPrivateConstructor: String
        get () {
            val sb = StringBuffer()
            sb.append("\t$name({")
            var i = 0;
            var len = fields.keys.size - 1;
            fields.keys.forEach { key ->
                val f = fields[key];
                val publicFieldName = fixFieldName(key, f, false);
                val privateFieldName = fixFieldName(key, f, true);
                _addTypeDef(f!!, sb);
                sb.append(" $publicFieldName")
                if (i != len) {
                    sb.append(", ")
                }
                i++;
            }
            sb.append("}) {\n")
            fields.keys.forEach { key ->
                val f = fields[key];
                val publicFieldName = fixFieldName(key, f, false);
                val privateFieldName = fixFieldName(key, f, true);
                sb.append("this.$privateFieldName = $publicFieldName;\n")
            }
            sb.append('}');
            return sb.toString();
        }

    val _defaultConstructor: String
        get() {
            val sb = StringBuffer();
            sb.append("\t$name({");
            var i = 0;
            val len = fields.keys.size - 1;
            fields.keys.forEach { key ->
                val f = fields[key];
                val fieldName = fixFieldName(key, f, privateFields);
                sb.append("this.$fieldName");
                if (i != len) {
                    sb.append(", ")
                }
                i++;
            }
            sb.append("});");
            return sb.toString();
        }

    val _jsonParseFunc: String
        get() {
            val sb = StringBuffer();
            sb.append("\t$name")
            sb.append(".fromJson(Map<String, dynamic> json) {\n")
            fields.keys.forEach { k ->
                sb.append("\t\t${fields[k]?.jsonParseExpression(k, privateFields)}\n")
            }
            sb.append("\t}");
            return sb.toString();
        }

    val _jsonGenFunc: String
        get () {
            val sb = StringBuffer();
            sb.append("\tMap<String, dynamic> toJson() {\n\t\tfinal Map<String, dynamic> data = new Map<String, dynamic>();\n");
            fields.keys.forEach { k ->
                sb.append("\t\t${fields[k]?.toJsonExpression(k, privateFields)}\n");
            }
            sb.append("\t\treturn data;\n");
            sb.append("\t}");
            return sb.toString();
        }

    override fun toString(): String {
        if (privateFields) {
            return "class $name {\n$_fieldList\n\n$_defaultPrivateConstructor\n\n$_gettersSetters\n\n$_jsonParseFunc\n\n$_jsonGenFunc\n}\n";
        } else {
            return "class $name {\n$_fieldList\n\n$_defaultConstructor\n\n$_jsonParseFunc\n\n$_jsonGenFunc\n}\n";
        }
    }
}


class Dependency(var name: String, var typeDef: TypeDefinition) {
    val className: String
        get() {
            return camelCase(name)
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
    private val isPrimitiveList: Boolean

    companion object {
        fun fromDynamic(obj: Any?): TypeDefinition {
            val type = getTypeName(obj)
            if (type == "List") {
                val list = obj as List<*>
                val firstElementType = if (list.isNotEmpty()) {
                    getTypeName(list[0])
                } else {
                    // when array is empty insert Null just to warn the user
                    "Null"
                }
                return TypeDefinition(type, firstElementType)
            }
            return TypeDefinition(type)
        }
    }

    init {
        isPrimitiveList = isPrimitive && name == "List"
    }


    override operator fun equals(other: Any?): Boolean {
        if (other is TypeDefinition) {
            return name == other.name && subtype == other.subtype;
        }
        return false;
    }

    fun _buildParseClass(expression: String): String {
        val properType = if (subtype != null) subtype else name
        return "new $properType.fromJson($expression)"
    }

    fun _buildToJsonClass(expression: String): String {
        return "$expression.toJson()"
    }

    fun jsonParseExpression(key: String, privateField: Boolean): String {
        val jsonKey = "json['$key']"
        val fieldKey = fixFieldName(key, this, privateField)
        when {
            isPrimitive -> {
                if (name == "List") {
                    return "$fieldKey = json['$key'].cast<$subtype>();";
                }
                return "$fieldKey = json['$key'];"
            }
            name == "List" -> // list of class  //如果是list,就把名字修改成单数
                return "if (json['$key'] != null) {\n\t\t\t$fieldKey = new List<${Inflector.getInstance().singularize(subtype!!)}>();\n\t\t\t(json['$key'] as List).forEach((v) { $fieldKey.add(new ${Inflector.getInstance().singularize(subtype!!)}.fromJson(v)); });\n\t\t}"
            else -> // class
                return "$fieldKey = json['$key'] != null ? ${_buildParseClass(jsonKey)} : null;"
        }
    }


    fun toJsonExpression(key: String, privateField: Boolean): String {
        val fieldKey = fixFieldName(key, this, privateField)
        val thisKey = "this.$fieldKey"
        if (isPrimitive) {
            return "data['$key'] = $thisKey;"
        } else if (name == "List") {
            // class list
            return """if ($thisKey != null) {
      data['$key'] = $thisKey.map((v) => ${_buildToJsonClass("v")}).toList();
    }"""
        } else {
            // class
            return """if ($thisKey != null) {
      data['$key'] = ${_buildToJsonClass(thisKey)};
    }"""
        }
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