package me.eetgeenappels.sugoma.module.modules.render

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.ui.ClickGUI
import org.lwjgl.input.Keyboard

object ClickGuiModule : Module("ClickGui", "A gui where you click", Category.Render) {
    private var clickGUI: ClickGUI? = null

    init {
        key = Keyboard.KEY_COMMA
    }

    override fun onTick() {
    }

    override fun onEnable() {
        if (clickGUI == null) clickGUI = ClickGUI(this)
        mc.displayGuiScreen(clickGUI)
    }

    override fun onDisable() {}
}
