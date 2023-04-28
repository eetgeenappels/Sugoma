package me.eetgeenappels.sugoma.module.modules.world

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.util.BlockUtil
import net.minecraft.util.math.BlockPos

class Surround : Module("Surround", "", Category.World) {

    // private disableWhenOutsideHole
    private var isInHole = false

    override fun onEnable () {
        // check if neighbour blocks around player are
    }

    override fun onTick (){
        val playerPos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
        for (blockPos in BlockUtil.getNeighbours(playerPos)){
            checkAndPlaceBlock(blockPos)
        }
    }

    private fun checkAndPlaceBlock(pos: BlockPos) : Boolean{
        if (BlockUtil.emptyBlocks.contains(mc.world.getBlockState(pos).block)) {
            var neighbours = BlockUtil.findNeighborBlocks(pos)
            if (neighbours.isEmpty()) {
                // no neighbours check for more blocks
                for (blockPos in BlockUtil.getNeighbours(pos)){
                    neighbours = BlockUtil.findNeighborBlocks(blockPos)
                    if (neighbours.isEmpty()) continue
                    val facing = neighbours[0].placeFace
                    BlockUtil.place(pos, facing, true)
                    return true
                }
                return false
            }
            //val placeBlock = neighbours[0].position
            val facing = neighbours[0].placeFace
            BlockUtil.place(pos, facing, true)

            return true
        }

        return false
    }

}
