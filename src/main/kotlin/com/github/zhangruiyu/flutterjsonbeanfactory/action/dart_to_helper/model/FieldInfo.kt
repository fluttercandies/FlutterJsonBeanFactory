package com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.model

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.psi.psiUtil.children


data class FieldClassTypeInfo(
    ///主类型,List,int,String,Map,dynamic等
    val primaryType: String,
    val nullable: Boolean,
    ///泛型类型
    val genericityType: FieldClassTypeInfo? = null,
) {
    fun isMap(): Boolean {
        return false
    }

    fun isList(): Boolean {
        return false
    }

    companion object {
        fun parseFieldClassTypeInfo(typeList: List<ASTNode>): FieldClassTypeInfo? {
            val filterTypeList = typeList.filterIsInstance<CompositeElement>()
            println("泛型的文案 ${filterTypeList.first().text}")
            if (filterTypeList.isEmpty() || filterTypeList.size == 1) {
                val firstChild = filterTypeList.first()
                val firstChildChild = firstChild.children().toList()
                if(firstChildChild.size > 1){
                    return parseFieldClassTypeInfo(firstChildChild)
                }else{
                    println("泛型的文案个数1 ${firstChild.text}")
                    return FieldClassTypeInfo(firstChild.text, false)
                }
            } else if (filterTypeList.size == 2) {
//                println("泛型的文案 ${toList[1].text}")
//                println("\t\t泛型的文案size $size")
//                println("\t\t泛型的文案size ${toList.joinToString { it.text }}")
                return FieldClassTypeInfo(
                    filterTypeList.first().text,
                    false,
                    genericityType = parseFieldClassTypeInfo(filterTypeList[1].children().toList())
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
