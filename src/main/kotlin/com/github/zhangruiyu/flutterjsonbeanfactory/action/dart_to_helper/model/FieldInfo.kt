package com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.model

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.psiUtil.children


data class FieldClassTypeInfo(
    ///主类型,List,int,String,Map,dynamic等
    val primaryType: String,
    val nullable: Boolean,
    ///泛型类型
    val genericityChildType: FieldClassTypeInfo? = null,
    val genericityString: String? = null,
) {
    fun isMap(): Boolean {
        return primaryType == "Map"
    }

    fun isList(): Boolean {
        return primaryType == "List"
    }

    companion object {
        fun parseFieldClassTypeInfo(typeList: List<ASTNode>): FieldClassTypeInfo? {
            println("泛型的文案之前 ${typeList.joinToString { it.text }}")
            val filterTypeList = typeList.filterIsInstance<CompositeElement>()
            ///是否可以是null
            val canNull = typeList.filterIsInstance<LeafPsiElement>().firstOrNull()?.text == "?"
            println("泛型的文案去除不需要的后 ${filterTypeList.joinToString { it.text }}")
            if (filterTypeList.isEmpty() || filterTypeList.size == 1) {
                val firstChild = filterTypeList.first()
                val firstChildChild = firstChild.children().toList()
                if(firstChildChild.size > 1){
                    return parseFieldClassTypeInfo(firstChildChild)
                }else{
                    println("泛型的文案个数1 ${firstChild.text}")
                    return FieldClassTypeInfo(firstChild.text, canNull)
                }
            } else if (filterTypeList.size == 2) {
//                println("泛型的文案 ${toList[1].text}")
//                println("\t\t泛型的文案size $size")
//                println("\t\t泛型的文案size ${toList.joinToString { it.text }}")
                return FieldClassTypeInfo(
                    filterTypeList.first().text,
                    canNull,
                    genericityChildType = parseFieldClassTypeInfo(filterTypeList[1].children().toList()),
                    genericityString = typeList[1].text
                )
            } else if (filterTypeList.size == 3) {
                ///这里只会是2 父类型和泛型
//                println("泛型的文案 ${toList[1].text}")
                println("\t\t泛型的文案size ${filterTypeList.joinToString { it.text }}")
                println("\t\t泛型的文案size ${filterTypeList.joinToString { it::class.java.name }}")
                return null
            } else {
                return null
            }
        }
    }
    fun ASTNode.typeChild(): List<CompositeElement> {
        return children().filterIsInstance<CompositeElement>().toList()
    }
}
