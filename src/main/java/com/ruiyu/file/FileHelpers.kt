package com.ruiyu.file

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.search.FilenameIndex
import com.jetbrains.lang.dart.DartTokenTypes
import com.ruiyu.jsontodart.AnnotationValue
import com.ruiyu.jsontodart.HelperClassGeneratorInfo
import com.ruiyu.setting.Settings
import io.flutter.pub.PubRoot
import io.flutter.utils.FlutterModuleUtils
//import org.jetbrains.kotlin.idea.core.util.toPsiFile
//import org.jetbrains.kotlin.idea.refactoring.toPsiFile
import org.jetbrains.kotlin.psi.psiUtil.children
import org.yaml.snakeyaml.Yaml
import wu.seal.jsontokotlin.utils.showErrorMessage
import java.io.File
import java.io.FileInputStream

//import io.flutter.utils.FlutterModuleUtils

object FileHelpers {
    @JvmStatic
    fun getResourceFolder(project: Project): VirtualFile {
        return project.baseDir.findChild("res")
                ?: project.baseDir.createChildDirectory(this, "res")
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
            callback(generated.findOrCreateChildData(this, "json_filed.dart"))
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
    private fun getProjectIdeaFile(project: Project): VirtualFile? {
        val ideaFile = project.projectFile ?: project.workspaceFile
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
    private fun generateDartEntityHelper(project: Project, packageName: String, helperClassGeneratorInfos: MutableList<HelperClassGeneratorInfo>?) {
        val pubSpecConfig = getPubSpecConfig(project)
        val content = StringBuilder()
        //导包
        //辅助主类的包名
        content.append(packageName)
        content.append("\n")
        content.append("import 'package:${pubSpecConfig?.name}/generated/json/base/json_filed.dart';")
        content.append("\n")
        content.append("import 'package:intl/intl.dart';")
        content.append("\n")
        content.append(helperClassGeneratorInfos?.joinToString("\n"))
        //创建文件
        getEntityHelperFile(project, "${File(packageName).nameWithoutExtension}_helper.dart") { file ->
            file.commitContent(project, content.toString())
        }
    }

    /**
     * 自动生成所有文件的辅助文件
     */
    fun generateAllDartEntityHelper(project: Project, allClass: List<Pair<MutableList<HelperClassGeneratorInfo>, String>>) {
        allClass.forEach {
            generateDartEntityHelper(project, it.second, it.first)
        }
    }

    /**
     * 获取所有符合生成的file
     */
    fun getAllEntityFiles(project: Project): List<Pair<MutableList<HelperClassGeneratorInfo>, String>> {
        val pubSpecConfig = getPubSpecConfig(project)
        val psiManager = PsiManager.getInstance(project)
        return FilenameIndex.getAllFilesByExt(project, "dart").filter {
            it.path.endsWith("_${ServiceManager.getService(Settings::class.java).state.modelSuffix.toLowerCase()}.dart") && it.path.contains("${project.name}/lib/")
        }.mapNotNull {
            val dartFileHelperClassGeneratorInfo = FileHelpers.getDartFileHelperClassGeneratorInfo(psiManager.findFile(it)!!)
            //包名
            val packageName = (it.path).substringAfter("${project.name}/lib/")
            //导包
            if (dartFileHelperClassGeneratorInfo == null) {
                null
            } else {
                dartFileHelperClassGeneratorInfo to "import 'package:${pubSpecConfig?.name}/${packageName}';"
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

    @Suppress("DuplicatedCode")
    @JvmStatic
    fun getPubSpecConfig(project: Project): PubSpecConfig? {
        PubRoot.forFile(getProjectIdeaFile(project))?.let { pubRoot ->
            FileInputStream(pubRoot.pubspec.path).use { inputStream ->
                (Yaml().load(inputStream) as? Map<String, Any>)?.let { map ->
                    return PubSpecConfig(project, pubRoot, map)
                }
            }
        }
        return null
    }

    @Suppress("DuplicatedCode")
    @JvmStatic
    fun shouldActivateFor(project: Project): Boolean = shouldActivateWith(getPubSpecConfig(project))

    @Suppress("DuplicatedCode")
    @JvmStatic
    fun shouldActivateWith(pubSpecConfig: PubSpecConfig?): Boolean {
        pubSpecConfig?.let {
            // Did the user deactivate for this project?
            // Automatically activated for Flutter projects.
            return it.isEnabled && it.pubRoot.declaresFlutter()
        }
        return pubSpecConfig?.pubRoot?.declaresFlutter() ?: false
    }

    fun getDartFileHelperClassGeneratorInfo(file: PsiFile): MutableList<HelperClassGeneratorInfo>? {
        //不包含JsonConvert 那么就不转
        if (file.text.contains("JsonConvert").not()) {
            return null
        }
        val mutableMapOf = mutableListOf<HelperClassGeneratorInfo>()
        file.children.forEach {
            val text = it.text

            val classNode = it?.node
            //是类
            if (classNode?.elementType == DartTokenTypes.CLASS_DEFINITION) {
                if (classNode is CompositeElement) {
                    val helperClassGeneratorInfo = HelperClassGeneratorInfo()
                    for (filedAndMethodNode in classNode.children()) {
                        val toBinaryName = filedAndMethodNode.elementType.toString()
                        val nodeName = filedAndMethodNode.text
                        //是类里字段
                        if (filedAndMethodNode.elementType == DartTokenTypes.CLASS_BODY) {
                            filedAndMethodNode.children().forEach { itemFile ->
                                itemFile.children().forEach { itemFileNode ->
                                    //itemFileNode text : int code
                                    if (itemFileNode.elementType == DartTokenTypes.VAR_DECLARATION_LIST) {

                                        if (itemFileNode.text.contains("JSONField")) {
                                            val strs = itemFileNode.text.substringAfter(")").split(" ").filter { item ->
                                                item != "\n" && item != "\t" && item.trim().trimIndent().isNotEmpty()
                                            }
                                            val nameNode = strs.last().trimIndent().trim()
//                                            if (strs.size == 2) {
//                                                val typeNode = strs[strs.size - 2]
//                                                val annotationValue = if (itemFileNode.text.contains("\"")) {
//                                                    itemFileNode.text.split("\"")[1]
//                                                } else {
//                                                    itemFileNode.text.split("'")[1]
//                                                }
//                                                helperClassGeneratorInfo.addFiled(strs[0].split("\n\t")[1], strs[1], annotationValue)
//                                            } else {
                                            val typeNode = strs.first().trimIndent().trim().replace("\t", "").replace("\n", "")
                                            val annotationString = itemFileNode.text.substringAfter("(").substringBefore(")").split(",").filterNot { itemAnnotationString ->
                                                itemAnnotationString.isEmpty()
                                            }
                                            val annotationValues = annotationString.map { itemAnnotationString ->
                                                val annotationNameOriginal = itemAnnotationString.substringBefore(":").trimIndent().trim()
                                                //注解的值
                                                val annotationValue: Any = when (val annotationValueOriginal = itemAnnotationString.substringAfter(":").trimIndent().trim()) {
                                                    "true" -> true
                                                    "false" -> false
                                                    else -> annotationValueOriginal.replace("\"", "").replace("\'", "")
                                                }

                                                AnnotationValue(annotationNameOriginal, annotationValue)
                                            }
//                                                val annotationValue = if (itemFileNode.text.contains("\"")) {
//                                                    itemFileNode.text.split("\"")[1]
//                                                } else {
//                                                    itemFileNode.text.split("'")[1]
//                                                }
                                            helperClassGeneratorInfo.addFiled(typeNode, nameNode, annotationValues)
//                                            }

                                        } else {
                                            val nameNode = itemFileNode.text.split(" ")[1]
                                            val typeNode = itemFileNode.text.split(" ")[0]
                                            helperClassGeneratorInfo.addFiled(typeNode, nameNode, null)
                                        }

                                    }
                                    var text4 = itemFileNode.text
                                    var text5 = itemFileNode.text
                                }
                                val text2 = itemFile.text
                                val text3 = itemFile.text
                            }
                        } else if (filedAndMethodNode.elementType == DartTokenTypes.COMPONENT_NAME) {
                            helperClassGeneratorInfo.className = (nodeName)
                        } else if (filedAndMethodNode.elementType == DartTokenTypes.MIXINS) {
                            //不包含JsonConvert 那么就不转
                            if (nodeName.contains("JsonConvert").not()) {
                                continue
                            }
                        }

                    }
                    mutableMapOf.add(helperClassGeneratorInfo)
//                    classNode.children() {filedAndMethodNode->
//                        val text1 = filedAndMethodNode.text
//                    }
                }
            }

            /* it?.node?.children()?.forEach {
                 val toString = it?.firstChildNode?.toString()
                 val toString33 = it?.lastChildNode?.toString()
            }*/
        }
        return mutableMapOf
    }
}

@Suppress("SameParameterValue")
/**
 * 如果没有配置,那么默认是true
 */
private fun isOptionTrue(map: Map<*, *>?, name: String): Boolean {
    val value = map?.get(name)?.toString()?.toLowerCase() ?: "true"
    return "true" == value
}

@Suppress("SameParameterValue")
private fun isOptionFalse(map: Map<*, *>?, name: String): Boolean {
    val value = map?.get(name)?.toString()?.toLowerCase()
    return "false" == value
}


/**
 *判断文件内容是否一致 不一致则覆盖
 */
fun VirtualFile?.commitContent(project: Project, content: String) {
    val documentManager = PsiDocumentManager.getInstance(project)
    val psiManager = PsiManager.getInstance(project)
    this?.let { file ->
        psiManager.findFile(file)?.let { dartFile ->
            documentManager.getDocument(dartFile)?.let { document ->
                if (document.text != content) {
                    document.setText(content)
                    documentManager.commitDocument(document)
                }
            }
        }
    }
}


private const val PUBSPEC_KEY = "flutter-json"
private const val PROJECT_NAME = "name"
private const val PUBSPEC_ENABLE_PLUGIN_KEY = "enable"
private const val PUBSPEC_DART_ENABLED_KEY = "enable-for-dart"

data class PubSpecConfig(
        val project: Project,
        val pubRoot: PubRoot,
        val map: Map<String, Any>,
        //项目名称,导包需要
        val name: String = ((if (map[PROJECT_NAME] == "null") null else map[PROJECT_NAME]) ?: project.name).toString(),
        val flutterJsonMap: Map<*, *>? = map[PUBSPEC_KEY] as? Map<*, *>,
        val isFlutterModule: Boolean = FlutterModuleUtils.hasFlutterModule(project),
        val isEnabled: Boolean = isOptionTrue(flutterJsonMap, PUBSPEC_ENABLE_PLUGIN_KEY)
)
