package me.eetgeenappels.sugoma.module

import me.eetgeenappels.sugoma.module.modules.settings.Setting
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import net.minecraftforge.common.MinecraftForge

open class Module(var name: String, var description: String, var category: Category) {
    var key = 0
    var toggled = false
    var settings: MutableList<Setting>
    protected val mc: Minecraft

    init {
        mc = Minecraft.getMinecraft()
        settings = ArrayList()
    }

    open fun onTick() {}
    fun isToggled(): Boolean {
        return toggled
    }

    fun toggle() {
        toggled = !toggled
        if (toggled) onEnable() else onDisable()
    }

    open fun onEnable() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    open fun onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    fun getSetting(name: String?): Setting? {
        for (setting in settings) if (setting.name.equals(name, ignoreCase = true)) return setting
        return null
    }

    open fun onPacket(packet: Packet<*>): Boolean{
        return false
    }
}
