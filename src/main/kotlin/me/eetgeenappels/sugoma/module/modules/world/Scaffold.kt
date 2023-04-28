package me.eetgeenappels.sugoma.module.modules.world

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.util.BlockUtil

class Scaffold : Module("Scaffold", "places blocks under you", Category.World) {
    override fun onTick() {
        if (BlockUtil.emptyBlocks.contains(mc.world.getBlockState(mc.player.position.down()).block)) {
            val neighbours = BlockUtil.findNeighborBlocks(mc.player.position.down())
            if (neighbours.isEmpty()) return
            val placeBlock = neighbours[0].position
            val facing = neighbours[0].placeFace
            BlockUtil.place(placeBlock, facing, true)
        }
    }

    companion object {
        private const val delay = 50 // The delay between block placements
    }
}
