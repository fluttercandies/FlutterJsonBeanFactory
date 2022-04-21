package com.github.zhangruiyu.flutterjsonbeanfactory.utils
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.github.zhangruiyu.flutterjsonbeanfactory.file.FileHelpers
import io.flutter.pub.PubRoot
import io.flutter.utils.FlutterModuleUtils
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream

object YamlHelper {


    @Suppress("DuplicatedCode")
    @JvmStatic
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

    @Suppress("DuplicatedCode")
    @JvmStatic
    fun hasLibJsonAnnotation(project: Project) = hasLibJsonAnnotation(getPubSpecConfig(project))

    @Suppress("DuplicatedCode")
    @JvmStatic
    fun hasLibJsonAnnotation(pubSpecConfig: PubSpecConfig?) =
        pubSpecConfig?.hasLibJsonAnnotation ?: false
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


//private const val PUBSPEC_KEY = "flutter-json"
private const val PROJECT_NAME = "name"
private const val PUBSPEC_ENABLE_PLUGIN_KEY = "enable"
private const val PUBSPEC_DART_ENABLED_KEY = "enable-for-dart"
private const val PUBSPEC_DEPENDENCIES = "dependencies"
private const val PUBSPEC_LIB_JSON_ANNOTATION = "json_annotation"

data class PubSpecConfig(
    val project: Project,
    val pubRoot: PubRoot,
    val map: Map<String, Any>,
        //项目名称,导包需要
    val name: String = ((if (map[PROJECT_NAME] == "null") null else map[PROJECT_NAME]) ?: project.name).toString(),
//    val flutterJsonMap: Map<*, *>? = map[PUBSPEC_KEY] as? Map<*, *>,
    val isFlutterModule: Boolean = FlutterModuleUtils.hasFlutterModule(project),
    // 是否依赖 json_annotation
    val hasLibJsonAnnotation: Boolean = (map[PUBSPEC_DEPENDENCIES] as? Map<*, *>)
        ?.containsKey(PUBSPEC_LIB_JSON_ANNOTATION) ?: false,
//    val isEnabled: Boolean = isOptionTrue(flutterJsonMap, PUBSPEC_ENABLE_PLUGIN_KEY),
)
