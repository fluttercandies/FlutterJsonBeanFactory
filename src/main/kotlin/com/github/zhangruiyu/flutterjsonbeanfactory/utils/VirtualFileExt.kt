package com.github.zhangruiyu.flutterjsonbeanfactory.utils

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager

/**
 *判断文件内容是否一致 不一致则覆盖
 */
fun VirtualFile?.commitContent(project: Project, content: String) {
    if (this != null) {
        val documentManager = PsiDocumentManager.getInstance(project)
        val psiManager = PsiManager.getInstance(project)
        psiManager.findFile(this)?.run {
            documentManager.getDocument(this)?.let { document ->
                if (document.text != content) {
                    document.setText(content)
                    documentManager.commitDocument(document)
                    ///格式化下代码,异步执行防止卡顿
                    CommandProcessor.getInstance().runUndoTransparentAction {
                        CodeStyleManager.getInstance(project).reformat(this)
                    }
                } else {
                    LogUtil.i("${this.name}内容一致,无需修改和格式化")
                }
            }
        }
    }

}
