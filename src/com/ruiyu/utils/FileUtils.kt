package com.ruiyu.utils

import com.ruiyu.setting.GenerateCode
import java.io.File


fun ergodicDartFile(
    projectName: String,
    file: File,
    resultFileName: MutableMap<GenerateCode, MutableList<Pair<String, String>>>,
    needDartNames: List<Array<String>>
): MutableMap<GenerateCode, MutableList<Pair<String, String>>> {
    // 判断目录下是不是空的
    val files = file.listFiles() ?: return resultFileName
    for (f in files) {
        if (f.isDirectory) {// 判断是否文件夹
            ergodicDartFile(projectName, f, resultFileName, needDartNames)// 调用自身,查找子目录
        } else
            if (f.path.toString().endsWith(".dart")) {
//                println(f.name)
                needDartNames.singleOrNull {
                    f.name.contains("${it[0]}.dart", true)
                }?.run {
                    val generateCode = GenerateCode(this[0],this[1],this[2])
                    if (resultFileName[generateCode] == null) {
                        resultFileName[generateCode] = mutableListOf()
                    }
                    resultFileName[generateCode]?.add(projectName to f.path)
                }
            }
    }
    return resultFileName
}