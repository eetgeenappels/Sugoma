package me.eetgeenappels.sugoma.events

import me.eetgeenappels.sugoma.Sugoma
import net.minecraft.client.Minecraft
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent

@EventBusSubscriber
class Events {
    private val mc = Minecraft.getMinecraft()
    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        if (mc.player != null) {
            for (module in Sugoma.moduleManager?.modules!!) {
                if (module.toggled) module.onTick()
            }
        }
    }

    @SubscribeEvent
    fun onPacketSending(event: ClientCustomPacketEvent) {
        var packet: Packet<*>? = event.packet // Get the packet being sent
        // Modify the packet here
        if (mc.player != null) {
            for (module in Sugoma.moduleManager?.modules!!) {
                if (module.toggled) packet = module.onPacketSending(packet)
            }
        }
        Sugoma.logger.info("Packet")
    }
}
