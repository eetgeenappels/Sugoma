package me.eetgeenappels.sugoma.module

import me.eetgeenappels.sugoma.module.modules.settings.Setting
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraftforge.common.MinecraftForge

abstract class Module(var name: String, var description: String, var category: Category) {
    var key = 0
    var toggled = false
        set(value) {
            if (value && !toggled) onEnable()
            if (!value && toggled) onDisable()
            field = value
        }
    var settings: MutableList<Setting>
    protected val mc: Minecraft

    init {
        mc = Minecraft.getMinecraft()
        settings = ArrayList()
    }

    abstract fun onTick()
    fun toggle() {
        toggled = !toggled
    }

    fun addSetting(setting: Setting) {
        settings.add(setting)
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
    open fun onConstTick(){}
    open fun onPlayerAttack(player: EntityPlayer, entity: Entity) {}
}
