package me.eetgeenappels.sugoma.module.modules.combat

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.SliderSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityTameable
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand

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
        val closestEntity = mc.world.loadedEntityList.stream()
            .filter { entity: Entity -> entity !== mc.player }
            .filter { entity: Entity -> mc.player.getDistance(entity) <= reach.value }
            .filter { entity: Entity -> !entity.isDead }
            .filter { entity: Entity -> attackCheck(entity) }
            .min { o1, o2 -> o1!!.getDistance(mc.player).compareTo(o2!!.getDistance(mc.player)) }
            .orElse(null)
        if (closestEntity != null) attack(closestEntity)
    }

    private fun attack(e: Entity) {
        val player = mc.player
        val blockPos = e.position

        // Calculate the angle between the player's position and the target block position
        val dx = blockPos.x + 0.5 - player.posX
        val dy = blockPos.y + 0.5 - (player.posY + player.getEyeHeight())
        val dz = blockPos.z + 0.5 - player.posZ
        val distance = Math.sqrt(dx * dx + dy * dy + dz * dz)
        val yaw = Math.toDegrees(Math.atan2(dz, dx)).toFloat() - 90
        val pitch = -Math.toDegrees(Math.atan2(dy, distance)).toFloat()

        // Send a packet to the server to update the player's rotation
        player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, player.onGround))
        if (mc.player.getCooledAttackStrength(0f) >= 1) {
            mc.playerController.attackEntity(mc.player, e)
            if (!(getSetting("NoSwing") as ToggleSetting).value) mc.player.swingArm(EnumHand.MAIN_HAND)
        }
    }

    private fun attackCheck(entity: Entity): Boolean {
        if (entity is EntityPlayer && targetPlayers.value) {
            if (entity.health > 0) {
                return true
            }
        }
        if (entity is EntityMob && targetMobs.value) return true
        return if (entity is EntityAnimal && targetAnimals.value) {
            if (entity is EntityTameable) false else true
        } else false
    }
}
