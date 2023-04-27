package me.eetgeenappels.sugoma.events

import me.eetgeenappels.sugoma.Sugoma
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent


class Events {
    private val mc = Minecraft.getMinecraft()


    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        if (mc.player != null) {
            for (module in Sugoma.moduleManager.modules) {
                if (module.toggled) module.onTick()
            }
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

}
