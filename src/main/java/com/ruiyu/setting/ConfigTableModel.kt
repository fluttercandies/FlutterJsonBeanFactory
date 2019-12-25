package com.ruiyu.setting

import javax.swing.table.AbstractTableModel

class ConfigTableModel(private val settingState: Settings) : AbstractTableModel() {
    private val columnNames = arrayOf("Scan suffix file name", "Method", "Behind the name of the class")
    val data = settingState.scanFileSetting

    override fun getColumnCount(): Int {
        return columnNames.size
    }

    override fun getRowCount(): Int {
        return data.size
    }

    override fun getColumnName(col: Int): String {
        return columnNames[col]
    }

    override fun getValueAt(row: Int, col: Int): Any {
        return data[row][col]
    }

    override fun getColumnClass(c: Int): Class<*> {
        return getValueAt(0, c)::class.java
    }

    override fun setValueAt(value: Any, row: Int, col: Int) {

        data[row][col] = value as String
        fireTableCellUpdated(row, col)

    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return true
    }
}