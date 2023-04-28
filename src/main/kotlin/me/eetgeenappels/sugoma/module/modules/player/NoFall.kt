package me.eetgeenappels.sugoma.module.modules.player

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
// currently doesn't work because i haven't gotten mixins working yet
class NoFall : Module("NoFall", "You don't take fall damage", Category.Player) {
    override fun onPacket(packet: Packet<*>): Boolean {
        class SugomaPacketPlayer : CPacketPlayer() {
            fun onGroundISTRUE(){
                this.onGround = true
            }
        }

        //if (mc.player.fallDistance >= 3.0f && packet is CPacketPlayer) {
        if (packet is CPacketPlayer) (packet as SugomaPacketPlayer).onGroundISTRUE()
        //}
        return false
    }
}
