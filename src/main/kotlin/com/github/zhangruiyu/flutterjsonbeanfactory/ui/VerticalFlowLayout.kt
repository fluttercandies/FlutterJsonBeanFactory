package com.github.zhangruiyu.flutterjsonbeanfactory.ui

import java.awt.*
import java.awt.event.ActionEvent
import java.io.Serializable
import java.util.*
import javax.swing.*
import javax.swing.border.LineBorder
import javax.swing.event.ChangeEvent

/**
 * 容器宽度 = 容器左边框宽度 + 容器左边框与组件的间隙 + 组件宽度 ( + 组件间水平间隙 + 组件宽度 ) + 组件与容器右边框的间隙 + 容器右边框宽度
 * 容器高度 = 类似
 */
class VerticalFlowLayout // 折列时, 每列列宽是否相同
// 折列时, 列是否填充满容器
constructor(// 每一列中各组件水平向对齐方式(注意非每一列在容器中的水平向对齐方式, 因为每一列在容器中的水平对齐方式应当由 容器的 componentOrientation 属性 ltr/rtl 来指定)
    var hAlign: Int = LEFT, // 每一列在容器中的垂直向对齐方式(注意无每一列在容器中的水平向对齐方式)
    var vAlign: Int = TOP, // 水平向边框与组件之间的间隙
    var hPadding: Int = 5, // 垂直向边框与组件之间的间隙, TOP:顶边距, BOTTOM:底边距
    var vPadding: Int = 5, // 水平向组件之间的间隙
    var hGap: Int = 5, // 垂直向组件之间的间隙
    var vGap: Int = 5, // 水平向组件是否填满逻辑列的宽度
    var isFill: Boolean = true, // 是否折列, true:折列, false:固定一列
    var isWrap: Boolean = false
) : LayoutManager, Serializable {

    constructor(padding: Int, gap: Int) : this(LEFT, TOP, padding, padding, gap, gap, true, false)
    constructor(padding: Int) : this(LEFT, TOP, padding, padding, 5, 5, true, false)

    override fun addLayoutComponent(name: String, comp: Component) {}
    override fun removeLayoutComponent(comp: Component) {}

    /**
     * 最合适的尺寸, 一列放下全部组件
     */
    override fun preferredLayoutSize(container: Container): Dimension {
        synchronized(container.treeLock) {
            var width = 0
            var height = 0

            // 可见组件的最大宽和累计高
            val components = getVisibleComponents(container)
            for (component in components) {
                val dimension = component.preferredSize
                width = Math.max(width, dimension.width)
                height += dimension.height
            }

            // 累计高添加组件间间隙
            if (0 < components.size) {
                height += vGap * (components.size - 1)
            }

            // 累计宽高添加边框宽高
            val insets = container.insets
            width += insets.left + insets.right
            height += insets.top + insets.bottom

            // 有组件的话, 累计宽高添加边框与组件的间隙和
            if (0 < components.size) {
                width += hPadding * 2
                height += vPadding * 2
            }
            return Dimension(width, height)
        }
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            var width = 0
            var height = 0

            // 可见组件的最大宽和累计高
            val components = getVisibleComponents(parent)
            for (component in components) {
                val dimension = component.minimumSize
                width = Math.max(width, dimension.width)
                height += dimension.height
            }

            // 累计高添加组件间间隙
            if (0 < components.size) {
                height += vGap * (components.size - 1)
            }

            // 累计宽高添加边框宽高
            val insets = parent.insets
            width += insets.left + insets.right
            height += insets.top + insets.bottom

            // 有组件的话, 累计宽高添加边框与组件的间隙和
            if (0 < components.size) {
                width += hPadding * 2
                height += vPadding * 2
            }
            return Dimension(width, height)
        }
    }

    override fun layoutContainer(container: Container) {
        synchronized(container.treeLock) {


            // 容器理想宽高, 即组件加间隙加容器边框后的累积理想宽高, 用于和容器实际宽高做比较
            val idealSize = preferredLayoutSize(container)
            // 容器实际宽高
            val size = container.size
            // 容器实际边框
            val insets = container.insets

            // 容器内可供组件使用的空间大小(排除边框和内边距)
            val availableWidth = size.width - insets.left - insets.right - hPadding * 2
            val availableHeight = size.height - insets.top - insets.bottom - vPadding * 2

            // 容器定义的组件方向, 这里先不管, 默认从左往右
//			ComponentOrientation orientation = container.getComponentOrientation();

            // 容器内所有可见组件
            val components = getVisibleComponents(container)

            // x基点
            var xBase = insets.left + hPadding

            // 缓存当前列中的所有组件
            val list: MutableList<Component> = LinkedList()
            for (component in components) {
                list.add(component)

                // 预算判断
                // 换列标准: 允许换列 且 该列组件数>1 且 该列累积高>容器可用高+vPadding
                // 累积高: 算上当前组件后, 当前列中的组件的累加高度(组件高度+组件间隙)
                if (isWrap && list.size > 1 && availableHeight + vPadding < getPreferredHeight(list)) {

                    // 如果需要换行, 则当前列中得移除当前组件
                    list.remove(component)
                    batch(insets, availableWidth, availableHeight, xBase, list, components)
                    xBase += hGap + getPreferredWidth(list)

                    // 需要换列, 清空上一列中的所有组件
                    list.clear()
                    list.add(component)
                }
            }
            if (list.isNotEmpty()) {
                batch(insets, availableWidth, availableHeight, xBase, list, components)
            }
        }
    }

    private fun batch(
        insets: Insets,
        availableWidth: Int,
        availableHeight: Int,
        xBase: Int,
        list: List<Component>,
        components: List<Component>
    ) {
        val preferredWidth = getPreferredWidth(list)
        val preferredHeight = getPreferredHeight(list)

        // y
        var y: Int
        y = if (vAlign == TOP) {
            insets.top + vPadding
        } else if (vAlign == CENTER) {
            (availableHeight - preferredHeight) / 2 + insets.top + vPadding
        } else if (vAlign == BOTTOM) {
            availableHeight - preferredHeight + insets.top + vPadding
        } else {
            insets.top + vPadding
        }
        for (i in list.indices) {
            val item = list[i]

            // x
            val x: Int = if (isFill) {
                xBase
            } else {
                when (hAlign) {
                    LEFT -> {
                        xBase
                    }

                    CENTER -> {
                        xBase + (preferredWidth - item.preferredSize.width) / 2
                    }

                    RIGHT -> {
                        xBase + preferredWidth - item.preferredSize.width
                    }

                    else -> {
                        xBase
                    }
                }
            }

            // width
            var width: Int
            if (isFill) {
                width = if (isWrap) preferredWidth else availableWidth
                // 下面这个判断的效果: 允许填充 且 允许折列 且 只有1列时, 填充全部可用区域
                // 或许可以来一个 开关 专门设置是否开启这个配置
                if (list.size == components.size) {
                    width = availableWidth
                }
            } else {
                width = item.preferredSize.width
            }

            // y
            if (i != 0) {
                y += vGap
            }

            // 组件调整
            item.setBounds(x, y, width, item.preferredSize.height)

            // y
            y += item.height
        }
    }

    private fun getVisibleComponents(container: Container): List<Component> {
        val list: MutableList<Component> = ArrayList()
        for (component in container.components) {
            if (component.isVisible) {
                list.add(component)
            }
        }
        return list
    }

    private fun getPreferredWidth(components: List<Component>): Int {
        var width = 0
        for (component in components) {
            width = Math.max(width, component.preferredSize.width)
        }
        return width
    }

    private fun getPreferredHeight(components: List<Component>): Int {
        var height = 0
        // 可见组件的最大宽和累计高
        for (component in components) {
            height += component.preferredSize.height
        }
        // 累计高添加组件间间隙
        if (0 < components.size) {
            height += vGap * (components.size - 1)
        }
        return height
    }

    override fun toString(): String {
        return "VerticalFlowLayout{" +
                "hAlign=" + hAlign +
                ", vAlign=" + vAlign +
                ", hPadding=" + hPadding +
                ", vPadding=" + vPadding +
                ", hGap=" + hGap +
                ", vGap=" + vGap +
                ", fill=" + isFill +
                ", wrap=" + isWrap +
                '}'
    }

    companion object {
        private const val serialVersionUID = 1L
        const val CENTER = 0 // 垂直对齐/水平对齐
        const val TOP = 1 // 垂直对齐
        const val BOTTOM = 2 // 垂直对齐
        const val LEFT = 3 // 水平对齐
        const val RIGHT = 4 // 水平对齐

        @JvmStatic
        fun main(args: Array<String>) {

            // 推荐一套超级漂亮的UI
            // FlatLaf：干净、优雅、扁平化的现代开源跨平台外观
            // https://weibo.com/ttarticle/p/show?id=2309404704477499490781
            // 官方: https://www.formdev.com/flatlaf/
            // 分两个包, 核心包和扩展主题包, 核心包自带4个主题, 使用如下代码启用对应主题
//		FlatIntelliJLaf.setup();
//		FlatLightLaf.setup();
//		FlatDarculaLaf.setup();
//		FlatDarkLaf.setup();
            val frame = JFrame("VerticalFlowLayout Test")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            val panel = JPanel()
            frame.contentPane.add(panel, BorderLayout.CENTER)
            panel.border = LineBorder(Color.white, 10)
            val layout = VerticalFlowLayout()
            panel.layout = layout
            panel.add(JButton("00000000000000000000000000000000000000000000000000"))
            panel.add(JButton("1"))
            panel.add(JButton("22"))
            panel.add(JButton("333"))
            panel.add(JButton("4444"))
            panel.add(JButton("55555"))
            panel.add(JButton("666666"))
            panel.add(JButton("7777777"))
            panel.add(JButton("88888888"))
            panel.add(JButton("999999999999999999999999999999999999999999999"))
            val border = LineBorder(Color.gray, 1)
            val label = JLabel("hello world")
            label.border = border
            panel.add(label)
            val radioButton = JRadioButton("select me")
            radioButton.border = border
            panel.add(radioButton)
            val checkBox = JCheckBox("select me")
            checkBox.border = border
            panel.add(checkBox)
            val textField = JTextField()
            textField.border = border
            panel.add(textField)
            val label2 = JLabel("hello world")
            label2.border = border
            panel.add(label2)
            val control = JPanel()
            control.layout = VerticalFlowLayout()
            frame.contentPane.add(control, BorderLayout.SOUTH)
            val borderPanel = JPanel()
            control.add(borderPanel)
            borderPanel.layout = FlowLayout(FlowLayout.LEFT)
            borderPanel.add(JLabel("border"))
            val borderSpinner = JSpinner(SpinnerNumberModel(10, 0, 100, 5))
            borderPanel.add(borderSpinner)
            borderSpinner.addChangeListener { e: ChangeEvent? ->
                panel.border = LineBorder(Color.white, borderSpinner.value as Int)
                panel.revalidate()
            }
            val hAlignPanel = JPanel()
            control.add(hAlignPanel)
            hAlignPanel.layout = FlowLayout(FlowLayout.LEFT)
            val hAlign = ButtonGroup()
            val hLeft = JRadioButton("Left")
            hLeft.isSelected = true
            hLeft.addActionListener { e: ActionEvent? ->
                layout.hAlign = LEFT
                panel.revalidate()
                println(layout)
            }
            hAlign.add(hLeft)
            val hCenter = JRadioButton("Center")
            hCenter.addActionListener { e: ActionEvent? ->
                layout.hAlign = CENTER
                panel.revalidate()
                println(layout)
            }
            hAlign.add(hCenter)
            val hRight = JRadioButton("Right")
            hRight.addActionListener { e: ActionEvent? ->
                layout.hAlign = RIGHT
                panel.revalidate()
                println(layout)
            }
            hAlign.add(hRight)
            hAlignPanel.add(JLabel("hAlign"))
            hAlignPanel.add(hLeft)
            hAlignPanel.add(hCenter)
            hAlignPanel.add(hRight)
            val vAlignPanel = JPanel()
            control.add(vAlignPanel)
            vAlignPanel.layout = FlowLayout(FlowLayout.LEFT)
            val vAlign = ButtonGroup()
            val vTop = JRadioButton("Top")
            vTop.isSelected = true
            vTop.addActionListener { e: ActionEvent? ->
                layout.vAlign = TOP
                panel.revalidate()
                println(layout)
            }
            vAlign.add(vTop)
            val vCenter = JRadioButton("Center")
            vCenter.addActionListener { e: ActionEvent? ->
                layout.vAlign = CENTER
                panel.revalidate()
                println(layout)
            }
            vAlign.add(vCenter)
            val vBottom = JRadioButton("Bottom")
            vBottom.addActionListener { e: ActionEvent? ->
                layout.vAlign = BOTTOM
                panel.revalidate()
                println(layout)
            }
            vAlign.add(vBottom)
            vAlignPanel.add(JLabel("vAlign"))
            vAlignPanel.add(vTop)
            vAlignPanel.add(vCenter)
            vAlignPanel.add(vBottom)
            val hPaddingPanel = JPanel()
            control.add(hPaddingPanel)
            hPaddingPanel.layout = FlowLayout(FlowLayout.LEFT)
            hPaddingPanel.add(JLabel("hPadding"))
            val hPaddingSpinner = JSpinner(SpinnerNumberModel(5, 0, 100, 5))
            hPaddingPanel.add(hPaddingSpinner)
            hPaddingSpinner.addChangeListener { e: ChangeEvent? ->
                layout.hPadding = hPaddingSpinner.value as Int
                panel.revalidate()
                println(layout)
            }
            val vPaddingPanel = JPanel()
            control.add(vPaddingPanel)
            vPaddingPanel.layout = FlowLayout(FlowLayout.LEFT)
            vPaddingPanel.add(JLabel("vPadding"))
            val vPaddingSpinner = JSpinner(SpinnerNumberModel(5, 0, 100, 5))
            vPaddingPanel.add(vPaddingSpinner)
            vPaddingSpinner.addChangeListener { e: ChangeEvent? ->
                layout.vPadding = vPaddingSpinner.value as Int
                panel.revalidate()
                println(layout)
            }
            val hGapPanel = JPanel()
            control.add(hGapPanel)
            hGapPanel.layout = FlowLayout(FlowLayout.LEFT)
            hGapPanel.add(JLabel("hGap"))
            val hGapSpinner = JSpinner(SpinnerNumberModel(5, 0, 100, 5))
            hGapPanel.add(hGapSpinner)
            hGapSpinner.addChangeListener { e: ChangeEvent? ->
                layout.hGap = hGapSpinner.value as Int
                panel.revalidate()
                println(layout)
            }
            val vGapPanel = JPanel()
            control.add(vGapPanel)
            vGapPanel.layout = FlowLayout(FlowLayout.LEFT)
            vGapPanel.add(JLabel("vGap"))
            val vGapSpinner = JSpinner(SpinnerNumberModel(5, 0, 100, 5))
            vGapPanel.add(vGapSpinner)
            vGapSpinner.addChangeListener { e: ChangeEvent? ->
                layout.vGap = vGapSpinner.value as Int
                panel.revalidate()
                println(layout)
            }
            val fillPanel = JPanel()
            control.add(fillPanel)
            fillPanel.layout = FlowLayout(FlowLayout.LEFT)
            val fillGroup = ButtonGroup()
            val fillTrue = JRadioButton("true")
            fillTrue.isSelected = true
            fillGroup.add(fillTrue)
            fillTrue.addActionListener { e: ActionEvent? ->
                layout.isFill = true
                panel.revalidate()
                println(layout)
            }
            val fillFalse = JRadioButton("false")
            fillGroup.add(fillFalse)
            fillFalse.addActionListener { e: ActionEvent? ->
                layout.isFill = false
                panel.revalidate()
                println(layout)
            }
            fillPanel.add(JLabel("fill"))
            fillPanel.add(fillTrue)
            fillPanel.add(fillFalse)
            val wrapPanel = JPanel()
            control.add(wrapPanel)
            wrapPanel.layout = FlowLayout(FlowLayout.LEFT)
            val wrapGroup = ButtonGroup()
            val wrapTrue = JRadioButton("true")
            wrapGroup.add(wrapTrue)
            wrapTrue.addActionListener { e: ActionEvent? ->
                layout.isWrap = true
                panel.revalidate()
                println(layout)
            }
            val wrapFalse = JRadioButton("false")
            wrapFalse.isSelected = true
            wrapGroup.add(wrapFalse)
            wrapFalse.addActionListener { e: ActionEvent? ->
                layout.isWrap = false
                panel.revalidate()
                println(layout)
            }
            wrapPanel.add(JLabel("wrap"))
            wrapPanel.add(wrapTrue)
            wrapPanel.add(wrapFalse)
            frame.pack()
            frame.setSize(frame.size.width + 1000, frame.size.height + 200)
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
            println(layout)
        }
    }
}
