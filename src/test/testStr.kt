package test

import com.ruiyu.jsontodart.CollectInfo
import com.ruiyu.utils.upperCharToUnderLine

private fun generateUnsafeDart(rawJson: String) {

    println(rawJson.upperCharToUnderLine())
}
private fun generateUnsafeDart(collectInfo : CollectInfo) {

    println(collectInfo.transformInputClassNameToFileName())
    println(collectInfo.firstClassEntityName())
}

fun main(args: Array<String>) {
    generateUnsafeDart("ServerConfigEntity")
    generateUnsafeDart(CollectInfo().apply {
     userInputClassName  = "ServerConfig"
    })
    println("-------")
    generateUnsafeDart(CollectInfo().apply {
     userInputClassName  = "server_config"
    })
}