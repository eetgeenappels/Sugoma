package me.eetgeenappels.sugoma.events

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.module.ModuleManager
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent


class Events {
    private val mc = Minecraft.getMinecraft()

    private var tickInit:  Boolean = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        if (mc.player != null) {
            for (module in Sugoma.moduleManager.modules) {
                if (module.toggled) module.onTick()
                module.onConstTick()
            }
        }
        if (!tickInit && mc.player != null) {
            ModuleManager.load()
            tickInit = true
        }
    }

    fun onPacket(packet: Packet<*>): Boolean {

        var cancel = false

        for (module in Sugoma.moduleManager.modules) {
            if (module.toggled) {
                if (module.onPacket(packet)){
                    cancel = true
                }
            }
        }

        return cancel
    }

    fun onPlayerAttack(player: EntityPlayer, entity: Entity) {
        for (module in Sugoma.moduleManager.modules) {
            if (module.toggled) {
                module.onPlayerAttack(player, entity)
            }
        }
    }

}
