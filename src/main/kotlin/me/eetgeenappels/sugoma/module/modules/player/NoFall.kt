package me.eetgeenappels.sugoma.module.modules.player

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.common.MinecraftForge

class NoFall : Module("NoFall", "You don't take fall damage", Category.Player) {
    override fun onPacket(packet: Packet<*>): Boolean {
        class SugomaPacketPlayer : CPacketPlayer() {
            fun onGroundISTRUE(){
                this.onGround = true
            }
        }

        //if (mc.player.fallDistance >= 3.0f && packet instanceof CPacketPlayer) {
        if (packet is CPacketPlayer) (packet as SugomaPacketPlayer).onGroundISTRUE()
        //}
        return false
    }

    override fun onEnable() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }
}
