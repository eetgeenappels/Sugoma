package me.eetgeenappels.sugoma.module.modules.player

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer

class Blink: Module("Blink", "cancels movement packets", Category.Player) {
    override fun onPacket(packet: Packet<*>): Boolean {
        return packet is CPacketPlayer
    }
}
