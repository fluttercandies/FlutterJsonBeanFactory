package com.github.zhangruiyu.flutterjsonbeanfactory.utils

import com.github.zhangruiyu.flutterjsonbeanfactory.file.FileHelpers
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import io.flutter.pub.PubRoot
import io.flutter.utils.FlutterModuleUtils
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream


object YamlHelper {


    fun getPubSpecConfig(project: Project): PubSpecConfig? {
        PubRoot.forFile(FileHelpers.getProjectIdeaFile(project))?.let { pubRoot ->
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
        return pubSpecConfig?.pubRoot?.declaresFlutter() ?: false
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


private fun optionString(map: Map<*, *>?, name: String, default: String): String {
    return map?.get(name)?.toString() ?: default
}


private const val PUBSPEC_KEY = "flutter_json"
private const val PROJECT_NAME = "name"
private const val PUBSPEC_ENABLE_PLUGIN_KEY = "enable"
private const val PUBSPEC_GENERATED_PATH_KEY = "generated_path"
private const val PUBSPEC_DART_ENABLED_KEY = "enable-for-dart"

///默认值
const val GENERATED_PATH_DEFAULT = "generated/json"

data class PubSpecConfig(
    val project: Project,
    val pubRoot: PubRoot,
    val map: Map<String, Any>,
    //项目名称,导包需要
    val name: String = ((if (map[PROJECT_NAME] == "null") null else map[PROJECT_NAME]) ?: project.name).toString(),
    val flutterJsonMap: Map<*, *>? = map[PUBSPEC_KEY] as? Map<*, *>,
    val isFlutterModule: Boolean = FlutterModuleUtils.hasFlutterModule(project),
//    val isEnabled: Boolean = isOptionTrue(flutterJsonMap, PUBSPEC_ENABLE_PLUGIN_KEY),
    val generatedPath: String = optionString(flutterJsonMap, PUBSPEC_GENERATED_PATH_KEY, GENERATED_PATH_DEFAULT),
)
