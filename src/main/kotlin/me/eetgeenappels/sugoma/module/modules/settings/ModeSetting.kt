package me.eetgeenappels.sugoma.module.modules.settings

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.ui.ClickGUI

class ModeSetting(name: String, var modes: Array<String>) : Setting(name) {
    var currentModeIndex = 0

    override fun render(renderX: Int, renderY: Int) {
        mc.fontRenderer.drawString(name + ": " + modes[currentModeIndex], renderX + 2, renderY, -0x1)
    }

    override fun onMouseClick(renderX: Int, renderY: Int, mouseX: Int, mouseY: Int, button: Int) {
        if (button == 0) {
            if (renderX < mouseX && mouseX < renderX + ClickGUI.Companion.viewWidth && renderY < mouseY && mouseY < renderY + mc.fontRenderer.FONT_HEIGHT) {
                currentModeIndex += 1
                if (currentModeIndex == modes.size) {
                    currentModeIndex = 0
                }
                Sugoma.Companion.moduleManager!!.save()
            }
        }
    }

    val currentModeString: String
        get() = modes[currentModeIndex]
}
