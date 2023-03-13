package com.github.zhangruiyu.flutterjsonbeanfactory.action.json_generate

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

open class DartGenerateToFromJsonFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {

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

    private fun buildFunctionsText(
        templateManager: TemplateManager,
        elementsToProcess: Set<DartComponent>
    ): Template {
        val template = templateManager.createTemplate(this.javaClass.name, "toFromJson")
        template.isToReformat = true
        template.addVariable(TextExpression("\tfactory ${this.myDartClass.name!!}.fromJson(Map<String, dynamic> json) => $${this.myDartClass.name!!}FromJson(json);"), true)
        template.addTextSegment("\n")
        template.addVariable(TextExpression("\tMap<String, dynamic> toJson() => $${this.myDartClass.name!!}ToJson(this);"), true)
        template.addTextSegment("\n")
        return template
    }

    override fun buildFunctionsText(templateManager: TemplateManager, e: DartComponent): Template? {
        return null
    }
}
