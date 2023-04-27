package me.eetgeenappels.sugoma.module.modules.combat

import com.apple.laf.AquaButtonBorder.Toggle
import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ModeSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

class AutoEZ : Module(name = "AutoEZ",  description = "Automatically says EZ", category = Category.Combat) {

    private val message : ModeSetting = ModeSetting("Message", arrayOf("Sugoma on Top", "EZ", "ez", "eetgeenappels is best!"))
    private var onlyPlayers : ToggleSetting = ToggleSetting("OnlyPlayers", true)
    private var targetList : MutableList<Target> = ArrayList()
    private val delay = 20
    private var delayCounter = 0

    init {
        settings.add(message)
        settings.add(onlyPlayers)
    }

    override fun onTick() {
        // delayCounter update
        delayCounter += 1

        val removeList : MutableList<Target> = ArrayList()
        for (target in targetList){
            if (!target.onTick()) {
                removeList.add(target)
            }
            if (target.entity.isDead) {
                // check for delay
                if (delayCounter > delay) {
                    // send chat message
                    mc.player.sendChatMessage(message.currentModeString)
                    delayCounter = 0
                }
                removeList.add(target)
            }
        }
        targetList.removeAll(removeList)
    }

    fun addTarget(target : Entity) {
        if (onlyPlayers.value)
            if (target !is EntityPlayer)
                return
        if (targetList.any { it.entity == target }) {
            // reset time
            targetList.first { it.entity == target }.tickDuration = 20 * 5
            return
        }
        targetList.add(Target(target))
    }

    class Target(val entity: Entity){

        var tickDuration = 20 * 5

        fun onTick() : Boolean{
            tickDuration -= 1
            return tickDuration > 0
        }

    }

}
