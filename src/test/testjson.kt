package test

import com.alibaba.fastjson.JSON
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ruiyu.jsontodart.MapDeserializerDoubleAsIntFix
import com.ruiyu.utils.JsonUtils


private fun generateUnsafeDart(rawJson: String) {
    val jsonRawData = Gson().fromJson<Map<String, Any>>(rawJson, HashMap::class.java)
    JsonUtils.jsonMapMCompletion(jsonRawData)
    print(jsonRawData)
}

fun main(args: Array<String>) {
//    generateUnsafeDart(
//        "{\n" +
//                "    \"name\": \"BeJson\",\n" +
//                "    \"url\": \"http://www.bejson.com\",\n" +
//                "    \"page\": 88,\n" +
//                "    \"isNonProfit\": true,\n" +
//                "    \"address\": {\n" +
//                "        \"street\": \"科技园路.\",\n" +
//                "        \"city\": \"江苏苏州\",\n" +
//                "        \"country\": \"中国\"\n" +
//                "    },\n" +
//                "    \"links\": [\n" +
//                "        {\n" +
//                "            \"name\": \"Google\",\n" +
//                "            \"url\": \"http://www.google.com\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"name\": \"Baidu\",\n" +
//                "            \"url\": \"http://www.baidu.com\"\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"name\": \"SoSo\",\n" +
//                "            \"url\": \"http://www.SoSo.com\",\n" +
//                "            \"haha\": [\n" +
//                "                {\n" +
//                "                    \"name\": \"Google\",\n" +
//                "                    \"url\": \"http://www.google.com\"\n" +
//                "                },\n" +
//                "                {\n" +
//                "                    \"name\": \"Baidu\",\n" +
//                "                    \"url\": \"http://www.baidu.com\"\n" +
//                "                }\n" +
//                "            ]\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"name\": \"SoSo\",\n" +
//                "            \"url\": \"http://www.SoSo.com\",\n" +
//                "            \"gege\": {\n" +
//                "                \"name\": \"Google\",\n" +
//                "                \"url\": \"http://www.google.com\"\n" +
//                "            }\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}"
//    )
    testType()
}

fun testType() {
    MapDeserializerDoubleAsIntFix.test2()
    val json = "{\"testarrayobj\": [\n" +
            "\n" +
            "  {\"e\":1},  {\"d\":3.0}\n" +
            "  ]}"
    val gsonBuilder = GsonBuilder()
    gsonBuilder.registerTypeAdapter(object : TypeToken<Map<String, Any>>() {

    }.getType(), MapDeserializerDoubleAsIntFix())
    val gson = gsonBuilder.create()
    val map = JSON.parseObject(json)
    print(map)
}
