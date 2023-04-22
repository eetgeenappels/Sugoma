package me.eetgeenappels.sugoma.module.modules.combat

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ModeSetting
import me.eetgeenappels.sugoma.module.modules.settings.SliderSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.potion.Potion
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.*
import net.minecraft.world.Explosion
import java.util.*
import java.util.stream.Collectors

class AutoCrystal : Module("AutoCrystal", "Automaticly Places and Detonates end crystals ", Category.Combat) {
    private val range: SliderSetting
    private val reach: SliderSetting
    private val targetingMode: ModeSetting
    private val minDamageBreak: SliderSetting
    private val maxSelfDamageBreak: SliderSetting
    private val minDamagePlace: SliderSetting
    private val maxSelfDamagePlace: SliderSetting
    private val antiSuicide: ToggleSetting
    private val breakDelay: SliderSetting
    private val placeDelay: SliderSetting
    private val multiplace: ToggleSetting
    private val lookAtCrystal: ToggleSetting
    private val killThatOneVeryAnnoyyingCrystalStrat: ToggleSetting
    private var delay = 0
    private var time = 0

    init {
        range = SliderSetting("Range", 10f, 20f, 15f)
        reach = SliderSetting("Reach", 3f, 5f, 3f)
        settings.add(range)
        settings.add(reach)
        targetingMode = ModeSetting("Targeting", arrayOf("Nearest", "Exposedness"))
        settings.add(targetingMode)
        minDamageBreak = SliderSetting("minDamageBreak", 0f, 18f, 6f)
        maxSelfDamageBreak = SliderSetting("maxDamageSelfBreak", 0f, 18f, 10f)
        minDamagePlace = SliderSetting("minDamagePlace", 0f, 18f, 6f)
        maxSelfDamagePlace = SliderSetting("maxDamageSelfPlace", 0f, 18f, 10f)
        settings.add(minDamageBreak)
        settings.add(maxSelfDamageBreak)
        settings.add(minDamagePlace)
        settings.add(maxSelfDamagePlace)
        antiSuicide = ToggleSetting("AntiSuicide", true)
        settings.add(antiSuicide)
        breakDelay = SliderSetting("BreakDelay", 0f, 5f, 1f, 0)
        placeDelay = SliderSetting("PlaceDelay", 0f, 5f, 3f, 0)
        multiplace = ToggleSetting("Multiplace", false)
        settings.add(breakDelay)
        settings.add(placeDelay)
        settings.add(multiplace)
        lookAtCrystal = ToggleSetting("LookAtCrystal", false)
        settings.add(lookAtCrystal)
        killThatOneVeryAnnoyyingCrystalStrat = ToggleSetting("killThatOneVeryAnnoyyingCrystalStrat", true)
        settings.add(killThatOneVeryAnnoyyingCrystalStrat)
    }

    override fun onTick() {
        time += 1
        if (time < delay) return
        var target: Entity? = null
        if (targetingMode.currentModeIndex == 0) {
            // set the target to attack with crystal (Make more options in the future.)
            target = mc.world.loadedEntityList.stream()
                .filter { entity: Entity -> entity !== mc.player }
                .filter { entity: Entity? -> entity is EntityPlayer || entity is EntityArmorStand }
                .filter { entity: Entity? -> mc.player.getDistance(entity) <= range.value }
                .filter { entity: Entity -> !entity.isDead }
                // get enemy closest to mc.player
                .min { o1, o2 -> o1!!.getDistance(mc.player).compareTo(o2!!.getDistance(mc.player)) }
                .orElse(null)
        }
        if (targetingMode.currentModeIndex == 1) {
            var minExposedScore = Float.POSITIVE_INFINITY
            val targets: MutableList<Entity?> = ArrayList()
            for (possibleTarget in mc.world.loadedEntityList.stream()
                .filter { entity: Entity -> entity !== mc.player }
                .filter { entity: Entity? -> entity is EntityPlayer || entity is EntityArmorStand }
                .filter { entity: Entity? -> mc.player.getDistance(entity) <= range.value }
                .filter { entity: Entity -> !entity.isDead }
                .collect(Collectors.toList<Entity>())) {
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
            target = targets.stream()
                .min { o1, o2 -> o1!!.getDistance(mc.player).compareTo(o2!!.getDistance(mc.player)) }
                .orElse(null)
        }
        if (target == null) return
        val broken_crystal = breakCrystal(target)
        if (!broken_crystal) {
            var placedCrystal = false
            if (multiplace.value) {
                for (i in 0..2) {
                    placedCrystal = if (placeCrystal(target)) {
                        true
                    } else {
                        break
                    }
                }
            } else {
                placedCrystal = placeCrystal(target)
            }
            if (placedCrystal) {
                time = 0
                delay = placeDelay.value.toInt()
            }
        } else {
            time = 0
            delay = breakDelay.value.toInt()
        }
    }

    fun breakCrystal(target: Entity): Boolean {
        var target = target
        val end_crystals = mc.world.loadedEntityList.stream()
            .filter { entity: Entity? -> mc.player.getDistance(entity) <= reach.value }
            .filter { entity: Entity? -> entity is EntityEnderCrystal }
            .collect(Collectors.toList())
        if (killThatOneVeryAnnoyyingCrystalStrat.value) {
            for (crystal in end_crystals) {
                if (crystal.position === mc.player.position.add(Vec3i(0, 3, 0))) {
                    if (mc.world.getBlockState(mc.player.position.add(Vec3i(0, 2, 0))).block === Blocks.OBSIDIAN) {
                        target = crystal
                    }
                }
            }
        }
        var maxDamage = -1000000.0
        var bestCrystal: Entity? = null
        for (crystal in end_crystals) {
            val pos = crystal.position
            val damage = calculateDamage(pos, target).toDouble()
            if (damage < minDamageBreak.value) continue
            val selfDamage = calculateDamage(pos, mc.player).toDouble()
            if (selfDamage > maxSelfDamageBreak.value) continue
            if (selfDamage >= mc.player.health + mc.player.absorptionAmount && antiSuicide.value) continue
            if (damage > maxDamage) {
                maxDamage = damage
                bestCrystal = crystal
            }
        }
        if (bestCrystal == null) return false
        attack(bestCrystal)
        return true
    }

    fun placeCrystal(target: Entity): Boolean {

        // place stuff
        var holding_crystal = false
        var holding_in_mainHand = false

        // Check if the player is holding end crystals in their main hand or off-hand
        if (mc.player.heldItemMainhand.item === Items.END_CRYSTAL) {
            holding_crystal = true
            holding_in_mainHand = true
        }
        if (mc.player.heldItemOffhand.item === Items.END_CRYSTAL) {
            holding_crystal = true
        }
        if (holding_crystal) {
            // Get the range value
            val range = reach.value.toInt()
            val possiblePositions: MutableList<BlockPos> = ArrayList()

            // Get the player's position
            val x = mc.player.posX
            val y = mc.player.posY
            val z = mc.player.posZ

            // Loop through all the blocks within the range
            for (i in (x - range).toInt()..(x + range).toInt()) {
                for (j in (y - range).toInt()..(y + range).toInt()) {
                    for (k in (z - range).toInt()..(z + range).toInt()) {
                        val pos = BlockPos(i, j, k)
                        if (canPlaceCrystal(pos)) possiblePositions.add(pos.up())
                    }
                }
            }
            if (possiblePositions.size == 0) {
                return false
            }
            var maxDamage = -1000000.0
            var bestCrystalPos: BlockPos? = null
            for (pos in possiblePositions) {
                val damage = calculateDamage(pos, target).toDouble()
                if (damage < minDamagePlace.value) continue
                val selfDamage = calculateDamage(pos, mc.player).toDouble()
                if (selfDamage > maxSelfDamagePlace.value) continue
                if (selfDamage >= mc.player.health + mc.player.absorptionAmount && antiSuicide.value) continue
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestCrystalPos = pos
                }
            }
            if (bestCrystalPos == null) return false
            if (lookAtCrystal.value) {
                val player = mc.player

                // Calculate the angle between the player's position and the target block position
                val dx = bestCrystalPos.x + 0.5 - player.posX
                val dy = bestCrystalPos.y + 0.5 - (player.posY + player.getEyeHeight())
                val dz = bestCrystalPos.z + 0.5 - player.posZ
                val distance = Math.sqrt(dx * dx + dy * dy + dz * dz)
                val yaw = Math.toDegrees(Math.atan2(dz, dx)).toFloat() - 90
                val pitch = -Math.toDegrees(Math.atan2(dy, distance)).toFloat()

                // Send a packet to the server to update the player's rotation
                player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, player.onGround))
            }
            if (holding_in_mainHand) {
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
            return true
        }
        return false
    }

    private fun getDamageMultiplied(damage: Float): Float {
        val diff: Float = mc.world.difficulty.id.toFloat()
        return damage.times(when (diff){
            0.0f -> 0.0f
            1.0f -> 0.5f
            2.0f ->  1.0f
            else -> 1.5f
        })
    }

    fun calculateDamage(pos: BlockPos, entity: Entity): Float {
        val posX = pos.x.toDouble()
        val posY = pos.y.toDouble()
        val posZ = pos.z.toDouble()
        val doubleExplosionSize = 12.0f
        val distancedSize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize.toDouble()
        val vec3d = Vec3d(posX, posY, posZ)
        val blockDensity = entity.world.getBlockDensity(vec3d, entity.entityBoundingBox).toDouble()
        val v = (1.0 - distancedSize) * blockDensity
        val damage = ((v * v + v) / 2.0 * 7.0 * doubleExplosionSize.toDouble() + 1.0).toInt().toFloat()
        var finalDamage = 1.0
        if (entity is EntityLivingBase) {
            finalDamage = getBlastReduction(
                entity,
                getDamageMultiplied(damage),
                Explosion(mc.world, null, posX, posY, posZ, 6f, false, true)
            ).toDouble()
        }
        return finalDamage.toFloat()
    }

    fun getBlastReduction(entity: EntityLivingBase, damage: Float, explosion: Explosion?): Float {
        var damage = damage
        if (entity is EntityPlayer) {
            val ep = entity
            val ds = DamageSource.causeExplosionDamage(explosion)
            damage = CombatRules.getDamageAfterAbsorb(
                damage,
                ep.totalArmorValue.toFloat(),
                ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
            )
            val k = EnchantmentHelper.getEnchantmentModifierDamage(ep.armorInventoryList, ds)
            val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
            damage *= 1.0f - f / 25.0f
            if (entity.isPotionActive(Objects.requireNonNull(Potion.getPotionById(11)))) {
                damage = damage - damage / 4
            }
            damage = Math.max(damage, 0.0f)
            return damage
        }
        damage = CombatRules.getDamageAfterAbsorb(
            damage,
            entity.totalArmorValue.toFloat(),
            entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
        )
        return damage
    }

    fun attack(e: Entity) {
        if (lookAtCrystal.value) {
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
        }
        if (mc.player.getCooledAttackStrength(0f) >= 1) {
            mc.playerController.attackEntity(mc.player, e)
            mc.player.swingArm(EnumHand.MAIN_HAND)
        }
    }

    private fun canPlaceCrystal(pos: BlockPos): Boolean {
        val block = mc.world.getBlockState(pos).block
        if (block === Blocks.OBSIDIAN || block === Blocks.BEDROCK) {
            val floor = mc.world.getBlockState(pos.add(0, 1, 0)).block
            val ceil = mc.world.getBlockState(pos.add(0, 2, 0)).block
            if (floor === Blocks.AIR && ceil === Blocks.AIR) {
                return mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(pos.add(0, 1, 0))).isEmpty()
            }
        }
        return false
    }
}
