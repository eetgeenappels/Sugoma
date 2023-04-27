package me.eetgeenappels.sugoma.module.modules.combat

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ModeSetting
import me.eetgeenappels.sugoma.module.modules.settings.SliderSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import me.eetgeenappels.sugoma.util.CombatUtil
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.potion.Potion
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.math.*
import net.minecraft.world.Explosion
import java.util.*
import java.util.stream.Collectors

class AutoCrystal : Module("AutoCrystal", "Automaticly Places and Detonates end crystals ", Category.Combat) {
    private val range = SliderSetting("Range", 10f, 20f, 15f)
    private val reach = SliderSetting("Reach", 3f, 5f, 3f)
    private val targetingMode = ModeSetting("Targeting", arrayOf("Nearest", "Exposedness"))
    private val minDamageBreak = SliderSetting("minDamageBreak", 0f, 18f, 6f)
    private val maxSelfDamageBreak = SliderSetting("maxDamageSelfBreak", 0f, 18f, 10f)
    private val minDamagePlace = SliderSetting("minDamagePlace", 0f, 18f, 6f)
    private val maxSelfDamagePlace = SliderSetting("maxDamageSelfPlace", 0f, 18f, 10f)
    private val antiSuicide = ToggleSetting("AntiSuicide", true)
    private val breakDelay = SliderSetting("BreakDelay", 0f, 5f, 1f, 0)
    private val placeDelay = SliderSetting("PlaceDelay", 0f, 5f, 3f, 0)
    private val multiplace = ToggleSetting("Multiplace", false)
    private val lookAtCrystal = ToggleSetting("LookAtCrystal", false)
    private val killThatOneVeryAnnoyingCrystalStrat = ToggleSetting("killThatOneVeryAnnoyyingCrystalStrat", true)
    private val targetArmorStands = ToggleSetting("TargetArmorStands", false)
    private var placeTime = 0
    private var breakTime = 0

    init {
        settings.add(range)
        settings.add(reach)
        settings.add(targetingMode)
        settings.add(minDamageBreak)
        settings.add(maxSelfDamageBreak)
        settings.add(minDamagePlace)
        settings.add(maxSelfDamagePlace)
        settings.add(antiSuicide)
        settings.add(breakDelay)
        settings.add(placeDelay)
        settings.add(multiplace)
        settings.add(lookAtCrystal)
        settings.add(killThatOneVeryAnnoyingCrystalStrat)
        settings.add(targetArmorStands)
    }

    override fun onTick() {
        val target: Entity = CombatUtil.findTarget(
            targetMobs = false,
            targetAnimals =  false,
            targetPlayers =  true,
            targetArmorStand = true,
            targetSortingType = this.targetingMode.currentModeIndex,
            reach = reach.value
        ) ?: return
        // add target to AutoEZ registry
        breakTime += 1
        if (crystalBreak(target)) {
            breakTime = -breakDelay.value.toInt()
            (Sugoma.moduleManager.getModule("AutoEZ")as AutoEZ).addTarget(target)
        }
        placeTime += 1
        if (crystalPlace(target)) {
            breakTime = -breakDelay.value.toInt()

            // check multiplace
            if (multiplace.value) {
                for (i in 0..1) {
                    crystalPlace(target)
                }
            }
        }
    }

    private fun crystalBreak(target: Entity): Boolean {
        // break delay
        if (breakTime < breakDelay.value.toInt()) return false
        val endCrystals = mc.world.loadedEntityList.stream()
            .filter { entity: Entity? -> mc.player.getDistance(entity!!) <= reach.value }
            .filter { entity: Entity? -> entity is EntityEnderCrystal }
            .collect(Collectors.toList())
        var neoTarget : Entity = target
        if (killThatOneVeryAnnoyingCrystalStrat.value) {
            for (crystal in endCrystals) {
                if (crystal.position === mc.player.position.add(Vec3i(0, 3, 0))) {
                    if (mc.world.getBlockState(mc.player.position.add(Vec3i(0, 2, 0))).block === Blocks.OBSIDIAN) {
                        neoTarget = crystal
                    }
                }
            }
        }

        var maxDamage = -1000000.0
        var bestCrystal: Entity? = null
        for (crystal in endCrystals) {
            val pos = crystal.position
            val damage = calculateDamage(pos, neoTarget).toDouble()
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
        CombatUtil.attack(bestCrystal, lookAtCrystal.value)
        return true
    }

    private fun crystalPlace(target: Entity): Boolean {

        // place delay
        if (placeTime < placeDelay.value.toInt()) return false

        // place stuff
        var holdingCrystal = false
        var holdingInMainhand = false

        // Check if the player is holding end crystals in their main hand or off-hand
        if (mc.player.heldItemMainhand.item === Items.END_CRYSTAL) {
            holdingCrystal = true
            holdingInMainhand = true
        }
        if (mc.player.heldItemOffhand.item === Items.END_CRYSTAL) {
            holdingCrystal = true
        }
        if (holdingCrystal) {
            // Get the range value
            val reach = this.reach.value.toInt()
            val possiblePositions: MutableList<BlockPos> = ArrayList()

            // Get the player's position
            val x = mc.player.posX
            val y = mc.player.posY
            val z = mc.player.posZ

            // Loop through all the blocks within the range
            for (i in (x - reach).toInt()..(x + reach).toInt()) {
                for (j in (y - reach).toInt()..(y + reach).toInt()) {
                    for (k in (z - reach).toInt()..(z + reach).toInt()) {
                        val pos = BlockPos(i, j, k)
                        if (canPlaceCrystal(pos)) possiblePositions.add(pos.up())
                    }
                }
            }
            if (possiblePositions.size == 0) {
                return false
            }

            val bestCrystalPos: BlockPos  = findBestCrystal(possiblePositions, target) ?: return false

            CombatUtil.placeCrystal(bestCrystalPos,lookAtCrystal.value,holdingInMainhand)

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

    private fun findBestCrystal(possiblePositions: List<BlockPos>,  target: Entity): BlockPos? {
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
        return bestCrystalPos
    }

    private fun calculateDamage(pos: BlockPos, entity: Entity): Float {
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

    private fun getBlastReduction(entity: EntityLivingBase, damage: Float, explosion: Explosion?): Float {
        var damage = damage
        if (entity is EntityPlayer) {
            val ds = DamageSource.causeExplosionDamage(explosion)
            damage = CombatRules.getDamageAfterAbsorb(
                damage,
                entity.totalArmorValue.toFloat(),
                entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
            )
            val k = EnchantmentHelper.getEnchantmentModifierDamage(entity.armorInventoryList, ds)
            val f = MathHelper.clamp(k.toFloat(), 0.0f, 20.0f)
            damage *= 1.0f - f / 25.0f
            if (Objects.requireNonNull(Potion.getPotionById(11))?.let { entity.isPotionActive(it) } == true) {
                damage -= damage / 4
            }
            damage = damage.coerceAtLeast(0.0f)
            return damage
        }
        damage = CombatRules.getDamageAfterAbsorb(
            damage,
            entity.totalArmorValue.toFloat(),
            entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
        )
        return damage
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
