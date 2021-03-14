package com.ruiyu.ui

import com.google.gson.*
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBEmptyBorder
import com.ruiyu.jsontodart.CollectInfo
import com.ruiyu.setting.Settings
import com.ruiyu.utils.addComponentIntoVerticalBoxAlignmentLeft
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.text.JTextComponent

/**
 * Dialog widget relative
 * Created by Seal.wu on 2017/9/21.
 */


class MyInputValidator : InputValidator {

    lateinit var classNameField: JTextField
    override fun checkInput(inputString: String): Boolean {
        return try {
            val classNameLegal = classNameField.text.trim().isNotBlank()
            val jsonElement = JsonParser().parse(inputString)

            (jsonElement.isJsonObject || jsonElement.isJsonArray) && classNameLegal
        } catch (e: JsonSyntaxException) {
            false
        }

    }

    override fun canClose(inputString: String): Boolean {
        return true
    }
}

val myInputValidator = MyInputValidator()

/**
 * Json input Dialog
 */
open class JsonInputDialog(
    project: Project,
    val inputModelBlock: (inputModel: CollectInfo) -> Boolean
) : Messages.InputDialog(
    project,
    "Please input the class name and JSON String for generating dart bean class",
    "Make Dart bean Class Code",
    Messages.getInformationIcon(),
    "",
    myInputValidator
) {

    private lateinit var classNameInput: JTextField

    private val prettyGson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()

    init {
        setOKButtonText("Make")
    }

    override fun createMessagePanel(): JPanel {
        val messagePanel = JPanel(BorderLayout())
        if (myMessage != null) {
            val textComponent = createTextComponent()
            messagePanel.add(textComponent, BorderLayout.NORTH)
        }
        myField = createTextFieldComponent()


        val classNameInputContainer = createLinearLayoutVertical()
        val classNameTitle = JBLabel("Class Name: ")
        classNameTitle.border = JBEmptyBorder(5, 0, 5, 0)
        classNameInputContainer.addComponentIntoVerticalBoxAlignmentLeft(classNameTitle)
        classNameInput = JTextField()
        classNameInput.preferredSize = JBDimension(400, 40)
        myInputValidator.classNameField = classNameInput

        classNameInput.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                okAction.isEnabled = myInputValidator.checkInput(myField.text)
            }
        })

        classNameInputContainer.addComponentIntoVerticalBoxAlignmentLeft(classNameInput)
        classNameInputContainer.preferredSize = JBDimension(500, 56)


        val createScrollableTextComponent = createMyScrollableTextComponent()
        val jsonInputContainer = createLinearLayoutVertical()
        jsonInputContainer.preferredSize = JBDimension(700, 400)
        jsonInputContainer.border = JBEmptyBorder(5, 0, 5, 5)
        val jsonTitle = JBLabel("JSON Text:")
        jsonTitle.border = JBEmptyBorder(5, 0, 5, 0)
        jsonInputContainer.addComponentIntoVerticalBoxAlignmentLeft(jsonTitle)
        jsonInputContainer.addComponentIntoVerticalBoxAlignmentLeft(createScrollableTextComponent)


        val centerContainer = JPanel()
        val centerBoxLayout = BoxLayout(centerContainer, BoxLayout.PAGE_AXIS)
        centerContainer.layout = centerBoxLayout
        centerContainer.addComponentIntoVerticalBoxAlignmentLeft(classNameInputContainer)
        centerContainer.addComponentIntoVerticalBoxAlignmentLeft(jsonInputContainer)
        messagePanel.add(centerContainer, BorderLayout.CENTER)
        val formatButton = JButton("Format")
        formatButton.horizontalAlignment = SwingConstants.CENTER
        formatButton.addActionListener(object : AbstractAction() {
            override fun actionPerformed(p0: ActionEvent?) {
                handleFormatJSONString()
            }

        })
        val settingContainer = JPanel()
        settingContainer.border = JBEmptyBorder(0, 5, 5, 7)
        val boxLayout = BoxLayout(settingContainer, BoxLayout.LINE_AXIS)
        settingContainer.layout = boxLayout
        settingContainer.add(Box.createHorizontalGlue())
        settingContainer.add(formatButton)
        messagePanel.add(settingContainer, BorderLayout.SOUTH)
        messagePanel.add(createCheckBox(), BorderLayout.SOUTH)

        return messagePanel
    }

    override fun createTextFieldComponent(): JTextComponent {
        val jTextArea = JTextArea(15, 50)
        jTextArea.minimumSize = JBDimension(750, 400)
//        jTextArea.lineWrap = true
//        jTextArea.wrapStyleWord = true
//        jTextArea.autoscrolls = true
        return jTextArea
    }


    private fun createMyScrollableTextComponent(): JComponent {
        val jbScrollPane = JBScrollPane(myField)
        jbScrollPane.preferredSize = JBDimension(700, 350)
        jbScrollPane.autoscrolls = true
        jbScrollPane.horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        jbScrollPane.verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        return jbScrollPane
    }


    override fun getPreferredFocusedComponent(): JComponent? {
        return if (classNameInput.text?.isEmpty() == false) {
            myField
        } else {
            classNameInput
        }
    }

    fun handleFormatJSONString() {
        val currentText = myField.text ?: ""
        if (currentText.isNotEmpty()) {
            try {
                val jsonElement = prettyGson.fromJson<JsonElement>(currentText, JsonElement::class.java)
                val formatJSON = prettyGson.toJson(jsonElement)
                myField.text = formatJSON
            } catch (e: Exception) {
            }
        }

        feedBackFormatJSONActionInfo()

    }

    private fun feedBackFormatJSONActionInfo() {
//        Thread { sendActionInfo(prettyGson.toJson(FormatJSONAction())) }.start()
    }

    override fun doOKAction() {

        val collectInfo = CollectInfo().apply {
            userInputClassName = classNameInput.text
            userInputJson = myField.text
        }
        if (collectInfo.userInputClassName.isEmpty()) {
            throw Exception("className must not null or empty")
        }
        if (collectInfo.userInputJson.isEmpty()) {
            throw Exception("json must not null or empty")
        }

        if (inputModelBlock(collectInfo)) {
            super.doOKAction()
        }
    }
}


fun createLinearLayoutVertical(): JPanel {
    val container = JPanel()
    val boxLayout = BoxLayout(container, BoxLayout.PAGE_AXIS)
    container.layout = boxLayout
    return container
}

fun createCheckBox(): DialogPanel {
    val listCheckBox = mutableListOf<CellBuilder<JBCheckBox>?>(null, null, null)
    return panel {
        row {
            checkBoxGroup(null) {
                listCheckBox[0] =
                    checkBox("default", ServiceManager.getService(Settings::class.java).filedSelectIndex == 1).apply {
//                        component.isSelected = true
                        component.addItemListener {
                            listCheckBox.forEachIndexed { index, cellBuilder ->
                                if (component.isSelected) {
                                    ServiceManager.getService(Settings::class.java).filedSelectIndex = 1
                                    listCheckBox[1]?.component?.isSelected = false
                                    listCheckBox[2]?.component?.isSelected = false
                                }
                            }
                        }
                    }
                listCheckBox[1] =
                    checkBox("late", ServiceManager.getService(Settings::class.java).filedSelectIndex == 2).apply {
//                    component.isSelected = true
                        component.addItemListener {
                            listCheckBox.forEachIndexed { index, cellBuilder ->
                                if (component.isSelected) {
                                    ServiceManager.getService(Settings::class.java).filedSelectIndex = 2
                                    listCheckBox[0]?.component?.isSelected = false
                                    listCheckBox[2]?.component?.isSelected = false
                                }
                            }
                        }
                    }
                listCheckBox[2] =
                    checkBox("null", ServiceManager.getService(Settings::class.java).filedSelectIndex == 3).apply {
//                    component.isSelected = true
                        component.addItemListener {
                            listCheckBox.forEachIndexed { index, cellBuilder ->
                                if (component.isSelected) {
                                    ServiceManager.getService(Settings::class.java).filedSelectIndex = 3
                                    listCheckBox[0]?.component?.isSelected = false
                                    listCheckBox[1]?.component?.isSelected = false
                                }
                            }
                        }
                    }
            }
        }
    }
}