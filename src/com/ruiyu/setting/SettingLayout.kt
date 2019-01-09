package com.ruiyu.setting

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBDimension
import wu.seal.jsontokotlin.utils.addComponentIntoVerticalBoxAlignmentLeft
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.border.EmptyBorder


class SettingLayout(settingState: Settings) {
    private val panel: JPanel = JPanel(BorderLayout())
    private val beanNameTextField: JBTextField
    val configTableModel = ConfigTableModel(settingState)

    init {

        val beanNameLayout = createLinearLayoutVertical()
        val beanName = JBLabel()
        beanName.border = EmptyBorder(5, 0, 5, 0)
        beanName.text = "model suffix"
        beanNameLayout.addComponentIntoVerticalBoxAlignmentLeft(beanName)
        beanNameTextField = JBTextField(settingState.modelSuffix)
        beanNameTextField.preferredSize = JBDimension(400, 40)
        beanNameLayout.addComponentIntoVerticalBoxAlignmentLeft(beanNameTextField)

        panel.add(beanNameLayout, BorderLayout.NORTH)

        val label1 = JBLabel()
        label1.border = EmptyBorder(5, 0, 5, 0)
        label1.text = "Configure scan suffix files(Please separate them with commas)"
        beanNameLayout.addComponentIntoVerticalBoxAlignmentLeft(
            label1
        )


        val jbTable = JBTable(configTableModel)
        jbTable.rowSelectionAllowed = true
        jbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
//        jbTable.preferredSize = JBDimension(400, 400)
        val jScrollPane = JBScrollPane(jbTable)
//        jScrollPane.layout = BorderLayout()
        beanNameLayout.addComponentIntoVerticalBoxAlignmentLeft(
            jScrollPane
        )
//        beanNameLayout.preferredSize = JBDimension(500, 512)
//        beanNameLayout.preferredSize = JBDimension(500, 56)
//        panel.add(suffixListTextFieldLayout,BorderLayout.CENTER)


//        panel.preferredSize = JBDimension(500, 56*2)
        panel.add(createLinearLayoutVertical(), BorderLayout.AFTER_LAST_LINE)
    }

    fun getRootComponent(): JComponent {
        return this.panel
    }

    fun getSuffixFiles(): List<Array<String>> {
        return configTableModel.data
    }

    fun getModelSuffix(): String {
        return beanNameTextField.text
    }
}

fun createLinearLayoutVertical(): JPanel {
    val container = JPanel()
    val boxLayout = BoxLayout(container, BoxLayout.PAGE_AXIS)
    container.layout = boxLayout
    return container
}