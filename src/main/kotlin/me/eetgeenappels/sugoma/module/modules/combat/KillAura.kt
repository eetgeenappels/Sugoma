package me.eetgeenappels.sugoma.module.modules.combat

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ModeSetting
import me.eetgeenappels.sugoma.module.modules.settings.SliderSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import me.eetgeenappels.sugoma.util.CombatUtil
import net.minecraft.entity.Entity

object KillAura : Module("KillAura", "An aura that kills stuff", Category.Combat) {
    val targetMode : ModeSetting = ModeSetting("TargetMode", arrayOf("Closest", "Health"))
    val reach: SliderSetting = SliderSetting("Reach", 3f, 5f, 4f)
    val targetPlayers: ToggleSetting = ToggleSetting("TargetPlayers", true)
    val targetMobs: ToggleSetting = ToggleSetting("TargetMobs", true)
    val targetAnimals: ToggleSetting = ToggleSetting("TargetAnimals", true)


    override fun onTick() {

        val closestEntity: Entity = CombatUtil.findTarget(
            targetMobs.value,
            targetAnimals.value,
            targetPlayers.value,
            false,
            when (targetMode.currentModeString) {

                "Closest" -> {0}
                "Health" -> {2}

                else -> {0}
            },
            reach.value
        ) ?: return
        (Sugoma.moduleManager.getModule("AutoEZ") as AutoEZ).addTarget(closestEntity)
        CombatUtil.attack(closestEntity, true)
    }
}
