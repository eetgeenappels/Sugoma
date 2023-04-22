package me.eetgeenappels.sugoma.module.modules.settings

import net.minecraft.client.Minecraft

abstract class Setting(var name: String) {
    protected var mc = Minecraft.getMinecraft()
    abstract fun render(renderX: Int, renderY: Int)
    abstract fun onMouseClick(renderX: Int, renderY: Int, mouseX: Int, mouseY: Int, button: Int)
    open fun onMouseRelease(renderX: Int, renderY: Int, mouseX: Int, mouseY: Int, button: Int) {}
    open fun updatePos(mouseX: Int, mouseY: Int) {}
}
