package com.ruiyu.beanfactory

import com.ruiyu.setting.GenerateCode
import com.ruiyu.utils.toUpperCaseFirstOne
import java.io.File


fun generateBeanFactory(
    generateCode: GenerateCode,
    factoryFile: File,
    content: List<Pair<String, String>>
) {
    if (factoryFile.exists()) {
        factoryFile.delete()
    }
    factoryFile.createNewFile()
    factoryFile.writeText(
        """
    ${
        content.map {
            "import '${it.second}';${System.lineSeparator()}"
        }.reduceRight { s, acc ->
            s + acc
        }
        }
class ${generateCode.scanName.toUpperCaseFirstOne()}Factory {
  ${generateCode.methodLine}
    if (1 == 0) {
      return null;${
        content.map {
            "${System.lineSeparator()}    } else if (T.toString() == \"${it.first}\") {\n" +
                    "      return ${it.first}${generateCode.classNameLine}"
        }.reduceRight { s, acc ->
            s + acc
        }
        }
    } else {
      return null;
    }
  }
}
    """.trim()
    )


}