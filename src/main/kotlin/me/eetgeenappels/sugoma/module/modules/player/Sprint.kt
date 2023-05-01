package me.eetgeenappels.sugoma.module.modules.player

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module

object Sprint : Module("Sprint", "you sprint", Category.Player) {
    override fun onTick() {
        if (mc.player.movementInput.moveForward > 0 && !mc.player.isSneaking && !mc.player.collidedHorizontally) {
            mc.player.isSprinting = true
        }
    }
}
