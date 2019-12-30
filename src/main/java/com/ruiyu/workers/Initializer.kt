package com.ruiyu.workers

import com.intellij.json.psi.JsonElementGenerator
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.ruiyu.Log
//import com.jetbrains.lang.dart.psi.*
//import com.jetbrains.lang.dart.util.DartElementGenerator
import com.ruiyu.file.FileHelpers
import io.flutter.pub.PubRoot
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.scheduleAtFixedRate

/**
 * User: zhangruiyu
 * Date: 2019/12/22
 * Time: 15:30
 */
class Initializer : StartupActivity, DocumentListener {
    private lateinit var documentManager: PsiDocumentManager

    override fun runActivity(project: Project) {
        documentManager = PsiDocumentManager.getInstance(project)
/*
        Timer().scheduleAtFixedRate(0, 1000) {
            if (FileHelpers.shouldActivateFor(project)) {
                ApplicationManager.getApplication().invokeLater {
                    runWriteAction {
                        FileGenerator(project).generate()
                    }
                }
            }
        }*/

    }

    /*   override fun documentChanged(event: DocumentEvent) {
           documentManager.commitDocument(event.document)
           val changedFile = documentManager.getPsiFile(event.document) as? JsonFile ?: return
           val undoManager = UndoManager.getInstance(changedFile.project)
           if (undoManager.isUndoInProgress || undoManager.isRedoInProgress) return

           if (PsiTreeUtil.findChildOfType(changedFile, PsiErrorElement::class.java) == null) {
               val lang = changedFile.virtualFile.nameWithoutExtension.substringAfter("_")
               val project = changedFile.project

               FileHelpers.getI18nFile(project) { dartFile ->
                   @Suppress("NAME_SHADOWING")
                   dartFile?.let { dartFile ->
                       PsiManager.getInstance(project).findFile(dartFile)?.let { dartPsi ->
                           PsiTreeUtil.findChildrenOfType(dartPsi, DartClass::class.java).firstOrNull {
                               it.name == lang
                           }?.let { replaceClass ->
                               val methodItem = getMethodItem(event, changedFile, event.offset, replaceClass)
                               methodItem.dartMethod?.delete()
                               methodItem.method.isNullOrBlank().let {
                                   DartElementGenerator.createDummyFile(project, "class Dummy {\n${methodItem.method}}")?.let { dummyFile ->
                                       val methods = PsiTreeUtil.findChildrenOfAnyType(
                                               dummyFile,
                                               DartMethodDeclaration::class.java,
                                               DartGetterDeclaration::class.java
                                       )

                                       methods.forEach {
                                           replaceClass.methods.last().parent.add(it)
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
           }
       }

       private fun getMethodItem(event: DocumentEvent, file: JsonFile, offset: Int, clazz: DartClass): MethodItem {
           val property = PsiTreeUtil.findElementOfClassAtOffset(file, offset, JsonProperty::class.java, false)
                   ?: return deleteMethod(event, file, clazz)

           val jsonValue = property.value
           val method: DartComponent?
           if (jsonValue == null) {
               PLURAL_MATCHER.reset(property.name)
               val find = PLURAL_MATCHER.find()
               method = when {
                   find -> clazz.findMethodByName(PLURAL_MATCHER.group(1))
                   else -> clazz.findMethodByName(property.name)
               }
               return MethodItem(null, method)
           }


           val value = getStringFromExpression(jsonValue)
           val id = if (property.name.isBlank()) return MethodItem.empty else property.name

           val generator = I18nFileGenerator(file.project)


           PLURAL_MATCHER.reset(id)
           val find = PLURAL_MATCHER.find()

           val builder = StringBuilder()
           println(clazz.name)
           when {
               find -> {
                   val methodName = PLURAL_MATCHER.group(1)
                   val count = PLURAL_MATCHER.group(2)

                   method = clazz.findMethodByName(methodName)
                   when (method) {
                       null -> generator.appendStringMethod(id, value, builder, shouldOverride(clazz))
                       is DartGetterDeclaration -> {
                           PLURAL_MATCHER.reset(method.name)
                           val isPlural = PLURAL_MATCHER.find()
                           if (isPlural && !PLURAL_MATCHER.group(2).endsWith("other", true)) {
                               val counts = arrayListOf(count)
                               counts.add(PLURAL_MATCHER.group(2))

                               val map = hashMapOf(id to value)
                               method.name?.let { methodName2 ->
                                   method.stringLiteralExpression?.let { literalExpression ->
                                       map[methodName2] = literalExpression.text.drop(1).dropLast(1)
                                   }
                               }

                               generator.appendPluralMethod(id, counts, map, builder, shouldOverride(clazz))
                           } else generator.appendStringMethod(id, value, builder, shouldOverride(clazz))
                       }
                       is DartMethodDeclaration -> {
                           val countsList = arrayListOf(count)
                           val valuesMap = hashMapOf(id to value)

                           PsiTreeUtil.findChildOfType(method, DartSwitchStatement::class.java)?.let { dartSwitch ->
                               dartSwitch.switchCaseList.forEach { dartSwitchCase ->
                                   PsiTreeUtil.findChildOfType(
                                           dartSwitchCase.statements,
                                           DartStringLiteralExpression::class.java
                                   )?.let { expression ->
                                       val caseValue = getStringFromExpression(expression)
                                       val thisExpression = dartSwitchCase.expression ?: return@forEach
                                       val caseCount = generator.getCountFromValue(getStringFromExpression(thisExpression))

                                       if (count != caseCount) {
                                           countsList.add(caseCount)
                                           valuesMap["$methodName$caseCount"] = caseValue
                                       }
                                   }
                               }

                               val dartSwitchCase = dartSwitch.defaultCase ?: return@let
                               val defaultExpression = PsiTreeUtil.findChildOfType(
                                       dartSwitchCase.statements,
                                       DartStringLiteralExpression::class.java
                               ) ?: return@let

                               if (!count.equals("other", true)) {
                                   countsList.add("other")
                                   valuesMap["${methodName}other"] = getStringFromExpression(defaultExpression)
                               }

                               generator.appendPluralMethod(methodName, countsList, valuesMap, builder, shouldOverride(clazz))
                           }
                       }
                   }
               }
               value.contains("$") -> {
                   method = clazz.findMethodByName(id)
                   generator.appendParametrizedMethod(id, value, builder, shouldOverride(clazz))
               }
               else -> {
                   method = clazz.findMethodByName(id)
                   generator.appendStringMethod(id, value, builder, shouldOverride(clazz))
               }
           }

           return MethodItem(builder.toString(), method)
       }

       private fun deleteMethod(event: DocumentEvent, file: JsonFile, clazz: DartClass): MethodItem {
           println(event.oldFragment)
           if (event.oldFragment.isEmpty()) return MethodItem.empty

           val json = JsonElementGenerator(file.project).createDummyFile("{${event.oldFragment.trim()}}")

           val property = PsiTreeUtil.findChildOfType(json, JsonProperty::class.java) ?: return MethodItem.empty
           val id = property.name

           PLURAL_MATCHER.reset(id)
           val find = PLURAL_MATCHER.find()

           val generator = I18nFileGenerator(file.project)
           var method: DartComponent?
           val builder = StringBuilder()
           when {
               find -> {
                   val methodName = PLURAL_MATCHER.group(1)
                   val count = PLURAL_MATCHER.group(2)

                   method = clazz.findMethodByName(methodName)
                   when (method) {
                       is DartGetterDeclaration -> method = clazz.findMethodByName(id)
                       is DartMethodDeclaration -> {
                           if (count.equals("other", true)) {
                               PsiTreeUtil.findChildOfType(method, DartSwitchStatement::class.java)?.let { dartSwitch ->
                                   dartSwitch.switchCaseList.forEach {
                                       val expression = PsiTreeUtil.findChildOfType(
                                               it.statements,
                                               DartStringLiteralExpression::class.java
                                       ) ?: return@forEach
                                       val thisExpression = it.expression ?: return@forEach
                                       val caseCount = generator.getCountFromValue(getStringFromExpression(thisExpression)) ?: return@forEach

                                       generator.appendStringMethod(
                                               "$methodName$caseCount",
                                               getStringFromExpression(expression),
                                               builder,
                                               shouldOverride(clazz)
                                       )
                                   }
                               }
                           } else {
                               val countsList = arrayListOf<String>()
                               val valuesMap = hashMapOf<String, String>()

                               PsiTreeUtil.findChildOfType(method, DartSwitchStatement::class.java)?.let { dartSwitch ->
                                   dartSwitch.switchCaseList.forEach { dartSwitchCase ->
                                       val expression = PsiTreeUtil.findChildOfType(
                                               dartSwitchCase.statements,
                                               DartStringLiteralExpression::class.java
                                       ) ?: return@forEach
                                       val thisExpression = dartSwitchCase.expression ?: return@forEach
                                       val caseCount = generator.getCountFromValue(getStringFromExpression(thisExpression)) ?: return@forEach

                                       if ("$methodName$caseCount" != id) {
                                           countsList.add(caseCount)
                                           valuesMap["$methodName$caseCount"] = getStringFromExpression(expression)
                                       }
                                   }

                                   val dartSwitchCase = dartSwitch.defaultCase ?: return@let
                                   val defaultExpression = PsiTreeUtil.findChildOfType(
                                           dartSwitchCase.statements,
                                           DartStringLiteralExpression::class.java
                                   ) ?: return@let
                                   val defaultValue = getStringFromExpression(defaultExpression)
                                   countsList.add("other")
                                   valuesMap["${methodName}other"] = defaultValue

                                   generator.appendPluralMethod(
                                           methodName,
                                           countsList,
                                           valuesMap,
                                           builder,
                                           shouldOverride(clazz)
                                   )
                               }
                           }
                       }
                   }
               }
               else -> method = clazz.findMethodByName(id)
           }

           return MethodItem(builder.toString(), method)
       }

       private fun shouldOverride(clazz: DartClass) = clazz.name != "en"*/

    companion object {
        val log = Log()
        private val PLURAL_MATCHER =
                Pattern.compile("(.*)(zero|one|two|few|many|other)", Pattern.CASE_INSENSITIVE).matcher("")

        fun getStringFromExpression(expression: PsiElement?): String = expression?.text?.drop(1)?.dropLast(1) ?: ""
    }
}