package com.github.zhangruiyu.flutterjsonbeanfactory.file

//import org.jetbrains.kotlin.idea.core.util.toPsiFile
//import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.node.GeneratorDartClassNodeToHelperInfo
import com.github.zhangruiyu.flutterjsonbeanfactory.action.dart_to_helper.node.HelperFileGeneratorInfo
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.YamlHelper
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.YamlHelper.getPubSpecConfig
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.commitContent
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.showErrorMessage
import com.github.zhangruiyu.flutterjsonbeanfactory.utils.showNotify
import com.intellij.psi.search.GlobalSearchScope
import io.flutter.pub.PubRoot
import java.io.File

//import io.flutter.utils.FlutterModuleUtils

object FileHelpers {
    @JvmStatic
    fun getResourceFolder(project: Project): VirtualFile {
        val guessProjectDir = project.guessProjectDir()
        return guessProjectDir?.findChild("res")
            ?: guessProjectDir!!.createChildDirectory(this, "res")
    }

    @JvmStatic
    fun getValuesFolder(project: Project): VirtualFile {
        val resFolder = getResourceFolder(project)
        return resFolder.findChild("values")
            ?: resFolder.createChildDirectory(this, "values")
    }

    /**
     * 获取json_convert_content目录
     */
    fun getJsonConvertContentFile(project: Project, callback: (file: VirtualFile) -> Unit) {
        ApplicationManager.getApplication().runWriteAction {
            val generated = getJsonConvertBaseFile(project)
            callback(generated.findOrCreateChildData(this, "json_convert_content.dart"))
        }
    }

    /**
     * 获取jsonfiled.dart
     */
    fun getJsonConvertJsonFiledFile(project: Project, callback: (file: VirtualFile) -> Unit) {
        ApplicationManager.getApplication().runWriteAction {
            val generated = getJsonConvertBaseFile(project)
            callback(generated.findOrCreateChildData(this, "json_field.dart"))
        }
    }

    /**
     * 获取generated/json/base目录
     */
    private fun getJsonConvertBaseFile(project: Project): VirtualFile {
        return getGeneratedFile(project).let { json ->
            json.findChild("base")
                ?: json.createChildDirectory(this, "base")
        }
    }

    /**
     *
     */
    private fun getEntityHelperFile(project: Project, fileName: String, callback: (file: VirtualFile) -> Unit) {
        ApplicationManager.getApplication().runWriteAction {
            val generated = getGeneratedFile(project)
            callback(generated.findOrCreateChildData(this, fileName))
        }
    }


    /**
     * 获取generated/json自动生成目录
     */
    private fun getGeneratedFile(project: Project): VirtualFile {
        return PubRoot.forFile(getProjectIdeaFile(project))?.lib?.let { lib ->
            return@let (lib.findChild("generated")
                ?: lib.createChildDirectory(this, "generated")).run {
                return@run (findChild("json")
                    ?: createChildDirectory(this, "json"))
            }
        }!!
    }

    /**
     * 获取项目.idea目录的一个文件
     */
    fun getProjectIdeaFile(project: Project): VirtualFile? {
        val ideaFile = project.projectFile ?: project.workspaceFile ?: project.guessProjectDir()?.children?.first()
        if (ideaFile == null) {
            project.showErrorMessage("Missing .idea/misc.xml or .idea/workspace.xml file")
        }
        return ideaFile
    }

    /**
     * 获取generated/json自动生成目录
     */
    fun getGeneratedFileRun(project: Project, callback: (file: VirtualFile) -> Unit) {
        ApplicationManager.getApplication().runWriteAction {
            callback(getGeneratedFile(project))
        }
    }

    /**
     * 自动生成单个文件的辅助文件
     */
    private fun generateDartEntityHelper(
        project: Project,
        packageName: String,
        helperClassGeneratorInfos: HelperFileGeneratorInfo?
    ) {
//        val pubSpecConfig = getPubSpecConfig(project)
        val content = StringBuilder()
        //导包
        val pubSpecConfig = YamlHelper.getPubSpecConfig(project)
        //辅助主类的包名
        content.append("import 'package:${pubSpecConfig?.name}/generated/json/base/json_convert_content.dart';\n")
        content.append(packageName)
        content.append("\n")
        //所有字段
        /* val allFields = helperClassGeneratorInfos?.classes?.flatMap {
             it.fields.mapNotNull { itemFiled ->
                 itemFiled.annotationValue
             }.flatMap { annotationList ->
                 annotationList.asIterable()
             }
         }*/
        helperClassGeneratorInfos?.imports?.filterNot {
            it.endsWith("json_field.dart';") || it.contains("dart:convert") || it.endsWith(
                ".g.dart';"
            )
        }?.forEach { itemImport ->
            content.append(itemImport)
            content.append("\n\n")
        }
        content.append(helperClassGeneratorInfos?.classes?.joinToString("\n"))
        //创建文件
        getEntityHelperFile(project, "${File(packageName).nameWithoutExtension}.g.dart") { file ->
            file.commitContent(project, content.toString())
        }
    }

    /**
     * 自动生成所有文件的辅助文件
     */
    fun generateAllDartEntityHelper(project: Project, allClass: List<Pair<HelperFileGeneratorInfo, String>>) {
        allClass.forEach {
            generateDartEntityHelper(project, it.second, it.first)
        }
    }

    /**
     * 获取所有符合生成的file
     */
    fun getAllEntityFiles(project: Project): List<Pair<HelperFileGeneratorInfo, String>> {
        val pubSpecConfig = getPubSpecConfig(project)
        val psiManager = PsiManager.getInstance(project)
        return FilenameIndex.getAllFilesByExt(project, "dart", GlobalSearchScope.projectScope(project)).filter {
            //不过滤entity结尾了
            it.path.contains("${project.name}/lib/") || it.path.contains("${pubSpecConfig?.name}/lib/")
        }.sortedBy {
            it.path
        }.mapNotNull {
            try {
                val dartFileHelperClassGeneratorInfo =
                    GeneratorDartClassNodeToHelperInfo.getDartFileHelperClassGeneratorInfo(psiManager.findFile(it)!!)
                //导包
                if (dartFileHelperClassGeneratorInfo == null) {
                    null
                } else {
                    //包名
                    val packageName = (it.path).substringAfter("/lib/")
                    dartFileHelperClassGeneratorInfo to "import 'package:${pubSpecConfig?.name}/${packageName}';"
                }
            } catch (e: Exception) {
                val errorString = "error file: ${it},stackTrace: ${e.stackTraceToString()}"
                println(errorString)
                project.showNotify(errorString)
                null
            }
        }
    }

    /**
     * 判断项目中是否包含这个file
     */
    fun containsProjectFile(project: Project, fileName: String): Boolean {
        return FilenameIndex.getAllFilesByExt(project, "dart").firstOrNull {
            it.path.endsWith(fileName)
        } != null
    }

    /**
     * 判断Directory中是否包含这个file
     */
    fun containsDirectoryFile(directory: PsiDirectory, fileName: String): Boolean {
        return directory.files.filter { it.name.endsWith(".dart") }
            .firstOrNull { it.name.contains(fileName) } != null
    }

}
