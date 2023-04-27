package me.eetgeenappels.sugoma.module.modules.combat

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.SliderSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import me.eetgeenappels.sugoma.util.CombatUtil
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand
import kotlin.math.atan2
import kotlin.math.sqrt

class KillAura : Module("KillAura", "An aura that kills stuff", Category.Combat) {
    private val reach: SliderSetting
    private val targetPlayers: ToggleSetting
    private val targetMobs: ToggleSetting
    private val targetAnimals: ToggleSetting

    init {
        reach = SliderSetting("Reach", 3f, 5f, 4f)
        settings.add(reach)
        targetPlayers = ToggleSetting("TargetPlayers", true)
        settings.add(targetPlayers)
        targetMobs = ToggleSetting("TargetMobs", true)
        settings.add(targetMobs)
        targetAnimals = ToggleSetting("TargetAnimals", true)
        settings.add(targetAnimals)
        settings.add(ToggleSetting("NoSwing", false))
    }

    override fun onTick() {
        super.onTick()
        val closestEntity: Entity = CombatUtil.findTarget(
            targetMobs.value,
            targetAnimals.value,
            targetPlayers.value,
            false,
            0,
            reach.value)
            ?: return
        (Sugoma.moduleManager.getModule("AutoEZ") as AutoEZ).addTarget(closestEntity)
        attack(closestEntity)
    }

    private fun attack(e: Entity) {
        val player = mc.player
        val blockPos = e.position

        println(e.name)

        // Calculate the angle between the player's position and the target block position
        val dx = blockPos.x + 0.5 - player.posX
        val dy = blockPos.y + 0.5 - (player.posY + player.getEyeHeight())
        val dz = blockPos.z + 0.5 - player.posZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        val yaw = Math.toDegrees(atan2(dz, dx)).toFloat() - 90
        val pitch = -Math.toDegrees(atan2(dy, distance)).toFloat()

        // Send a packet to the server to update the player's rotation
        player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, player.onGround))
        if (mc.player.getCooledAttackStrength(0f) >= 1) {
            mc.playerController.attackEntity(mc.player, e)
            if (!(getSetting("NoSwing") as ToggleSetting).value) mc.player.swingArm(EnumHand.MAIN_HAND)
        }
    }
}
