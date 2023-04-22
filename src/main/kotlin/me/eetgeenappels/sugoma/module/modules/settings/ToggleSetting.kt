package me.eetgeenappels.sugoma.module.modules.settings

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.ui.ClickGUI

class ToggleSetting(name: String, var value: Boolean) : Setting(name) {
    override fun render(renderX: Int, renderY: Int) {
        ClickGUI.Companion.drawRectangle(renderX + 2, renderY + 2, 5, 5, 1f, 1f, 1f, 1f)
        if (value) {
            ClickGUI.Companion.drawRectangle(renderX + 3, renderY + 3, 3, 3, 0f, 1f, 1f, 1f)
        }
        mc.fontRenderer.drawString(name, renderX + 9, renderY, -0x1)
    }

    override fun onMouseClick(renderX: Int, renderY: Int, mouseX: Int, mouseY: Int, button: Int) {
        if (button == 0) {
            if (renderX < mouseX && mouseX < renderX + 60 && renderY < mouseY && mouseY < renderY + mc.fontRenderer.FONT_HEIGHT) {
                value = !value
                Sugoma.Companion.moduleManager!!.save()
            }
        }
    }
}
