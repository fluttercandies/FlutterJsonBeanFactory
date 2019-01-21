package com.ruiyu.jsontodart

import com.google.gson.Gson
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.ruiyu.jsontodart.filetype.DartFileType
import com.ruiyu.jsontodart.utils.camelCase
import com.ruiyu.utils.JsonUtils
import com.ruiyu.utils.executeCouldRollBackAction
import wu.seal.jsontokotlin.utils.showNotify


class ModelGenerator(
    val rootClassName: String,
    val project: Project?,
    val psiFileFactory: PsiFileFactory?,
    val directory: PsiDirectory?,
    val generateSuccessfulBlock: (() -> Unit)?
) {

    var allClasses = mutableListOf<ClassDefinition>()

    private fun generateClassDefinition(className: String, jsonRawData: Any) {

        if (jsonRawData is List<*>) {
            // if first element is an array, start in the first element.
            generateClassDefinition(className, jsonRawData[0]!!)
        } else if (jsonRawData is Map<*, *>) {
            val keys = jsonRawData.keys
            val classDefinition = ClassDefinition(className);
            keys.forEach { key ->
                val typeDef = TypeDefinition.fromDynamic(jsonRawData[key])
                if (typeDef.name == "Class") {
                    typeDef.name = camelCase(key as String)
                }
                if (typeDef.subtype != null && typeDef.subtype == "Class") {
                    typeDef.subtype = camelCase(key as String)
                }
                classDefinition.addField(key as String, typeDef)
            }
            if (allClasses.firstOrNull { cd -> cd == classDefinition } == null) {
                allClasses.add(classDefinition)
            }
            val dependencies = classDefinition.dependencies
            dependencies.forEach { dependency ->
                if (dependency.typeDef.name == "List") {
                    if (((jsonRawData[dependency.name]) as? List<*>)?.isNotEmpty() == true) {
                        val names = (jsonRawData[dependency.name] as List<*>)
                        generateClassDefinition(dependency.className, names[0]!!)
                    }
                } else {
                    generateClassDefinition(dependency.className, jsonRawData[dependency.name]!!)
                }
            }
        }
    }

    private fun generateUnsafeDart(rawJson: String): String {
        val jsonRawData = Gson().fromJson<Map<String, Any>>(rawJson, HashMap::class.java)
        JsonUtils.jsonMapMCompletion(jsonRawData)
        generateClassDefinition(camelCase(rootClassName), jsonRawData)
        return allClasses.joinToString("\n")
    }


    fun generateDartClasses(rawJson: String): String {
        val unsafeDart = generateUnsafeDart(rawJson);
        println(unsafeDart)
//        final formatter = new DartFormatter();
//        return formatter.format(unsafeDart);
        var fileName = rootClassName
        psiFileFactory?.run {
            fileName = changeDartFileNameIfCurrentDirectoryExistTheSameFileNameWithoutSuffix(fileName, directory!!)
            generateDartDataClassFile(
                fileName,
                unsafeDart,
                project,
                psiFileFactory,
                directory
            )
            val notifyMessage = "Dart Data Class file generated successful"
            showNotify(notifyMessage, project)
        }

        return unsafeDart
    }

    private fun generateDartDataClassFile(
        fileName: String,
        classCodeContent: String,
        project: Project?,
        psiFileFactory: PsiFileFactory,
        directory: PsiDirectory
    ) {

        executeCouldRollBackAction(project) {
            val file = psiFileFactory.createFileFromText("$fileName.dart", DartFileType(), classCodeContent)
            directory.add(file)
            generateSuccessfulBlock?.invoke()
        }
    }

    private fun changeDartFileNameIfCurrentDirectoryExistTheSameFileNameWithoutSuffix(
        fileName: String,
        directory: PsiDirectory
    ): String {
        var newFileName = fileName
        val dartFileSuffix = ".dart"
        val fileNamesWithoutSuffix =
            directory.files.filter { it.name.endsWith(dartFileSuffix) }
                .map { it.name.dropLast(dartFileSuffix.length) }
        while (fileNamesWithoutSuffix.contains(newFileName)) {
            newFileName += "X"
        }
        return newFileName
    }

}