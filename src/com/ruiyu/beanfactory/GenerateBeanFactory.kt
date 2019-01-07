package com.ruiyu.beanfactory

import java.io.File


fun generateBeanFactory(
    name: String,
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
class ${name}Factory {
  static T generateOBJ<T>(json) {
    if (1 == 0) {
      return null;${
        content.map {
            "${System.lineSeparator()}    } else if (T.toString() == \"${it.first}\") {\n" +
                    "      return ${it.first}.fromJson(json) as T;"
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