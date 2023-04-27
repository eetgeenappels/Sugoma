package me.eetgeenappels.sugoma.util

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import kotlin.math.atan2
import kotlin.math.sqrt

object CombatUtil {

    val mc: Minecraft =  Minecraft.getMinecraft();
    fun findTarget(targetMobs:Boolean, targetAnimals:Boolean, targetPlayers:Boolean, targetArmorStand:Boolean, targetSortingType: Int, reach: Float) : Entity? {

        val entities = Minecraft.getMinecraft().world.loadedEntityList

        val possibleEntities = ArrayList<Entity>()

        val player: EntityPlayer = Minecraft.getMinecraft().player

        for (entity in entities) {
            if (checkPossibleTarget(entity, targetMobs, targetAnimals, targetPlayers, targetArmorStand, reach)) {
                possibleEntities.add(entity)
            }
        }

        if (targetSortingType == 0){
            return possibleEntities.stream()
                // use player.getDistance() to check for closest entity
                .min { o1, o2 -> o1!!.getDistance(player).compareTo(o2!!.getDistance(player)) }
                .orElse(null)
        }
        if (targetSortingType == 1){
            var minExposedScore = Float.POSITIVE_INFINITY
            val targets: MutableList<Entity> = ArrayList()
            for (possibleTarget in possibleEntities) {
                val pos = possibleTarget.position
                var score = 0f
                if (mc.world.getBlockState(pos.down()).block === Blocks.OBSIDIAN ||
                    mc.world.getBlockState(pos.down()).block === Blocks.BEDROCK
                ) {
                    score += 2f
                }
                if (mc.world.getBlockState(pos.west()).block === Blocks.OBSIDIAN ||
                    mc.world.getBlockState(pos.west()).block === Blocks.BEDROCK
                ) {
                    score += 1.5.toFloat()
                }
                if (mc.world.getBlockState(pos.east()).block === Blocks.OBSIDIAN ||
                    mc.world.getBlockState(pos.east()).block === Blocks.BEDROCK
                ) {
                    score += 1.5.toFloat()
                }
                if (mc.world.getBlockState(pos.north()).block === Blocks.OBSIDIAN ||
                    mc.world.getBlockState(pos.north()).block === Blocks.BEDROCK
                ) {
                    score += 1.5.toFloat()
                }
                if (mc.world.getBlockState(pos.south()).block === Blocks.OBSIDIAN ||
                    mc.world.getBlockState(pos.south()).block === Blocks.BEDROCK
                ) {
                    score += 1.5.toFloat()
                }
                if (score < minExposedScore) {
                    minExposedScore = score
                    targets.add(possibleTarget)
                }
                if (score == minExposedScore) {
                    targets.add(possibleTarget)
                }
            }
            return targets.stream()
                .min { o1, o2 -> o1!!.getDistance(mc.player).compareTo(o2!!.getDistance(mc.player)) }
                .orElse(null)
        }
        return null
    }

    private fun checkPossibleTarget(target:Entity,  targetMobs:Boolean, targetAnimals:Boolean, targetPlayers:Boolean, targetArmorStand:Boolean, reach: Float) : Boolean {

        if (target == mc.player)
            return false

        if (target.getDistance(mc.player) > reach)
            return false

        if (target.isDead)
            return false

        if (target.isInvulnerable)
            return false

        if (target is EntityMob && targetMobs)
            return true

        if (target is EntityAnimal && targetAnimals)
            return true

        if (target is EntityPlayer && targetPlayers)
            return true

        return target is EntityArmorStand && targetArmorStand

    }

    fun placeCrystal(bestCrystalPos: BlockPos, lookAtCrystal: Boolean, holdingInMainhand: Boolean){
        if (lookAtCrystal) {
            val player = mc.player

            // Calculate the angle between the player's position and the target block position
            val dx = bestCrystalPos.x + 0.5 - player.posX
            val dy = bestCrystalPos.y + 0.5 - (player.posY + player.getEyeHeight())
            val dz = bestCrystalPos.z + 0.5 - player.posZ
            val distance = sqrt(dx * dx + dy * dy + dz * dz)
            val yaw = Math.toDegrees(atan2(dz, dx)).toFloat() - 90
            val pitch = -Math.toDegrees(atan2(dy, distance)).toFloat()

            // Send a packet to the server to update the player's rotation
            player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, player.onGround))
        }
        if (holdingInMainhand) {
            mc.player.connection.sendPacket(
                CPacketPlayerTryUseItemOnBlock(
                    bestCrystalPos.down(),
                    EnumFacing.UP,
                    EnumHand.MAIN_HAND,
                    0f,
                    0f,
                    0f
                )
            )
        } else {
            mc.player.connection.sendPacket(
                CPacketPlayerTryUseItemOnBlock(
                    bestCrystalPos.down(),
                    EnumFacing.UP,
                    EnumHand.OFF_HAND,
                    0f,
                    0f,
                    0f
                )
            )
        }
    }
    fun attack(e: Entity, sendRotationPacket: Boolean) {
        if (sendRotationPacket) {
            val player = mc.player
            val blockPos = e.position

            // Calculate the angle between the player's position and the target block position
            val dx = blockPos.x + 0.5 - player.posX
            val dy = blockPos.y + 0.5 - (player.posY + player.getEyeHeight())
            val dz = blockPos.z + 0.5 - player.posZ
            val distance = sqrt(dx * dx + dy * dy + dz * dz)
            val yaw = Math.toDegrees(atan2(dz, dx)).toFloat() - 90
            val pitch = -Math.toDegrees(atan2(dy, distance)).toFloat()

            // Send a packet to the server to update the player's rotation
            player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, player.onGround))
        }
        if (mc.player.getCooledAttackStrength(0f) >= 1) {
            mc.playerController.attackEntity(mc.player, e)
            mc.player.swingArm(EnumHand.MAIN_HAND)
        }
    }
}


