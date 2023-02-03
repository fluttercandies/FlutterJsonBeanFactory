package com.github.zhangruiyu.flutterjsonbeanfactory.action.with

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartType
import com.jetbrains.lang.dart.psi.DartVarAccessDeclaration

open class DartGenerateCopyFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {

    override fun processElements(project: Project, editor: Editor, elementsToProcess: Set<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        this.anchor = this.doAddMethodsForOne(
            editor,
            templateManager,
            this.buildFunctionsText(templateManager, elementsToProcess),
            this.anchor
        )
    }

    override fun getNothingFoundMessage(): String {
        return ""
    }

    protected fun buildFunctionsText(
        templateManager: TemplateManager,
        elementsToProcess: Set<DartComponent>
    ): Template {
        val template = templateManager.createTemplate(this.javaClass.name, "copyWith")
        template.isToReformat = true
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment(" ")
        template.addVariable(TextExpression("copyWith"), true)
        template.addTextSegment(if (elementsToProcess.isEmpty()) "(" else "({")

        elementsToProcess.forEach {
            if (it is DartVarAccessDeclaration) {
                var type: DartType? = it.type
                if (null == type) {
                    template.addTextSegment("var " + it.name!!)
                } else {
                    template.addTextSegment(it.type!!.text.replace("?", "") + "? " + it.name!!)
                }
                template.addTextSegment(",")
            }
        }

        template.addTextSegment(if (elementsToProcess.isEmpty()) ");" else "}){")
        template.addTextSegment("return ")
        template.addTextSegment("${this.myDartClass.name!!}()")

        elementsToProcess.forEach {
            template.addTextSegment("..${it.name!!} = ${it.name!!} ?? this.${it.name!!}")
        }

        template.addTextSegment(";")
        template.addEndVariable()
        template.addTextSegment(" }")
        template.addTextSegment(" ")
        return template
    }

    override fun buildFunctionsText(templateManager: TemplateManager, e: DartComponent): Template? {
        return null
    }
}
