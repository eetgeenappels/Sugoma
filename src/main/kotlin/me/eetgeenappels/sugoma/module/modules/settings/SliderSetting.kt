package me.eetgeenappels.sugoma.module.modules.settings

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.ui.ClickGUI
import java.text.DecimalFormat

class SliderSetting : Setting {
    val min: Float
    val max: Float
    var value: Float
    var doClickStuff: Boolean
    var rounding: Int

    constructor(name: String, min: Float, max: Float, value: Float) : super(name) {
        this.min = min
        this.max = max
        this.value = value
        doClickStuff = false
        rounding = 1
    }

    constructor(name: String, min: Float, max: Float, value: Float, rounding: Int) : super(name) {
        this.min = min
        this.max = max
        this.value = value
        doClickStuff = false
        this.rounding = rounding
    }

    override fun render(renderX: Int, renderY: Int) {
        ClickGUI.Companion.drawRectangle(
            renderX,
            renderY,
            ((value - min) / (max - min) * ClickGUI.Companion.viewWidth).toInt(),
            mc.fontRenderer.FONT_HEIGHT,
            0f,
            1f,
            1f,
            1f
        )
        mc.fontRenderer.drawString("$name: $value", renderX + 2, renderY, -0x1)
    }

    private var renderX = 0
    override fun onMouseClick(renderX: Int, renderY: Int, mouseX: Int, mouseY: Int, button: Int) {
        if (button == 0) {
            if (renderX < mouseX && mouseX < renderX + ClickGUI.Companion.viewWidth && renderY < mouseY && mouseY < renderY + mc.fontRenderer.FONT_HEIGHT) {
                doClickStuff = true
                val x = (mouseX - renderX).toFloat()
                val `val`: Float = x / ClickGUI.Companion.viewWidth * (max - min) + min
                value = roundToNDecimalPlaces(`val`.toDouble(), rounding)
                this.renderX = renderX
                Sugoma.Companion.moduleManager!!.save()
            }
        }
    }

    override fun onMouseRelease(renderX: Int, renderY: Int, mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) doClickStuff = false
        Sugoma.moduleManager!!.save()
    }

    override fun updatePos(mouseX: Int, mouseY: Int) {
        if (doClickStuff) {
            if (renderX < mouseX && mouseX < renderX + ClickGUI.Companion.viewWidth) {
                val y = (mouseX - renderX).toFloat()
                value =
                    roundToNDecimalPlaces((y / ClickGUI.Companion.viewWidth * (max - min) + min).toDouble(), rounding)
            }
            if (mouseX >= renderX + ClickGUI.Companion.viewWidth) {
                value = max
            }
            if (mouseX <= renderX) {
                value = min
            }
        }
    }

    companion object {
        fun roundToNDecimalPlaces(number: Double, n: Int): Float {
            val pattern = StringBuilder("#.")
            for (i in 0 until n) {
                pattern.append("#")
            }
            val df = DecimalFormat(pattern.toString())
            val formattedNumber = df.format(number)
            return formattedNumber.toFloat()
        }
    }
}
