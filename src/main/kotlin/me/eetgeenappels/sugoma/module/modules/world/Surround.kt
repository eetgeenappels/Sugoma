package me.eetgeenappels.sugoma.module.modules.world

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import me.eetgeenappels.sugoma.util.BlockUtil
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos

class Surround : Module("Surround", "Surrounds you with obsidian", Category.World) {

    private val disableWhenOutsideHole = ToggleSetting("DisableWhenOutsideHole", true)

    private var initialIsInHole = false
    private var initialBlockPos = BlockPos(0,0,0)


    override fun onEnable () {
        // check if neighbour all blocks around player are obsidian
        initialIsInHole = checkHole()
        initialBlockPos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
    }

    override fun onTick (){
        if (disableWhenOutsideHole.value) {
            if (initialIsInHole) {
                if (mc.player.posX.toInt() != initialBlockPos.x ||
                    mc.player.posY.toInt() != initialBlockPos.y ||
                    mc.player.posZ.toInt() != initialBlockPos.z
                ) {
                    this.toggle()
                    return
                }
            } else {
                if (checkHole()) {
                    initialIsInHole = true
                    initialBlockPos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
                }
            }
        }
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
                    BlockUtil.placeSpecificBlock(pos, facing, Blocks.OBSIDIAN)
                    return true
                }
                return false
            }
            //val placeBlock = neighbours[0].position
            val facing = neighbours[0].placeFace
            BlockUtil.placeSpecificBlock(pos, facing, Blocks.OBSIDIAN)

            return true
        }
        return false
    }

    fun checkHole() : Boolean {
        var inHole = true
        for (pos in BlockUtil.getNeighbours(BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ))){
            if (mc.world.getBlockState(pos).block != Blocks.OBSIDIAN) {
                inHole = false
            }
        }
        return inHole
    }
}
