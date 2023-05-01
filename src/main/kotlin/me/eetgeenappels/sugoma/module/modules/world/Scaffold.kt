package me.eetgeenappels.sugoma.module.modules.world

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.util.BlockUtil
import net.minecraft.util.math.BlockPos

object Scaffold : Module("Scaffold", "places blocks under you", Category.World) {
    override fun onTick() {
        val playerPos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
        if (BlockUtil.emptyBlocks.contains(mc.world.getBlockState(playerPos.down()).block)) {
            var neighbours = BlockUtil.findNeighborBlocks(playerPos.down())
            if (neighbours.isEmpty()) {
                // no neighbours check for more blocks
                for (blockPos in BlockUtil.getNeighbours(playerPos.down())){
                    neighbours = BlockUtil.findNeighborBlocks(blockPos)
                    if (neighbours.isEmpty()) continue
                    val facing = neighbours[0].placeFace
                    BlockUtil.place(playerPos.down(), facing)
                    return
                }
                return
            }
            //val placeBlock = neighbours[0].position
            val facing = neighbours[0].placeFace
            BlockUtil.place(playerPos.down(), facing)
        }
    }

    object {
        private const val delay = 50 // The delay between block placements
    }
}
