package me.eetgeenappels.sugoma.module.modules.combat

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ModeSetting
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer

object Criticals: Module("Criticals", "makes your attacks crit", Category.Combat) {

    private val mode: ModeSetting = ModeSetting("Mode", arrayOf("Packet", "StrictPacket"))

    private var moveTick = 0

    override fun onTick() {

        if (++moveTick < 0) {
            mc.player.motionX = .0
            mc.player.motionY = .0
            mc.player.motionZ = .0
        }

    }

    override fun onPacket(packet: Packet<*>): Boolean {

        moveTick += 1

        when (mode.currentModeIndex){

            0-> {

            }
            1 ->{
                if (moveTick < 0) return true
            }

        }
        return false
    }

    override fun onPlayerAttack(player: EntityPlayer, entity: Entity) {
        if (player != mc.player) return
        when (mode.currentModeIndex){
            0 -> {
                mc.player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.1, player.posZ, false))
                mc.player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY, player.posZ, false))
            }
            1 -> {
                moveTick -3

                mc.player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.11, player.posZ, false))
                mc.player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 0.1100013579, player.posZ, false))
                mc.player.connection.sendPacket(CPacketPlayer.Position(player.posX, player.posY + 1.3579E-6, player.posZ, false))
            }
        }
    }


}
