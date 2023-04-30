package me.eetgeenappels.sugoma.module.modules.player

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import net.minecraft.network.play.client.CPacketPlayer
class NoFall : Module("NoFall", "You don't take fall damage", Category.Player) {
    override fun onTick() {
        super.onTick()
        if (mc.player.fallDistance > 2) {
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true))
        }
    }
}
