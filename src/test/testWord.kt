package test

import com.ruiyu.utils.Inflector


fun main(args: Array<String>) {
    //单数转复数
    println(Inflector.getInstance().pluralize("woman"))
    println(Inflector.getInstance().pluralize("box"))
    println(Inflector.getInstance().pluralize("tomato"))
    //复数转单数
    println(Inflector.getInstance().singularize("women"))
    println(Inflector.getInstance().singularize("Items"))
    println(Inflector.getInstance().singularize(""))

}