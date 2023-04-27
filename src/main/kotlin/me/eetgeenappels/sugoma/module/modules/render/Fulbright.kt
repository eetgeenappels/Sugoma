package me.eetgeenappels.sugoma.module.modules.render

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect

class Fulbright : Module ("Fulbright", "discord light mode", Category.Render) {

    override fun onDisable() {
        Potion.getPotionById(16)?.let { mc.player.removePotionEffect(it) }
    }

    override fun onEnable() {
        Potion.getPotionById(16)?.let { PotionEffect(it, Int.MAX_VALUE) }?.let { mc.player.addPotionEffect(it) };
    }



}
