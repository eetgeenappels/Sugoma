package me.eetgeenappels.sugoma.module.modules.world

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import me.eetgeenappels.sugoma.util.BlockUtil
import net.minecraft.entity.MoverType
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.BlockPos
import org.lwjgl.util.vector.Vector3f
import kotlin.math.abs
import kotlin.math.floor

object Surround : Module("Surround", "Surrounds you with obsidian", Category.World) {


    private val enableInHole = ToggleSetting("EnableInHole", false)
    private val disableWhenOutsideHole = ToggleSetting("DisableWhenOutsideHole", true)
    private val moveToCenter = ToggleSetting("MoveToCenter", false)

    private var initialIsInHole = false
    private var initialBlockPos = BlockPos(0,0,0)

    private var isMovingToCenter = false

    override fun onEnable () {
        // check if neighbour all blocks around player are obsidian
        initialIsInHole = true
        initialBlockPos = BlockPos(mc.player.posX.toInt(), mc.player.posY.toInt(), mc.player.posZ.toInt())
        if (this.moveToCenter.value)
            this.isMovingToCenter = true
    }

    private fun getPlayerCenterPos() : Vector3f{
        return Vector3f(floor(mc.player.posX).toFloat(), floor(mc.player.posY).toFloat(), floor(mc.player.posZ).toFloat()).translate(0.5f, 0f, 0.5f)
    }

    override fun onTick (){

        if (this.isMovingToCenter) {

            val center = getPlayerCenterPos()

            // find delta's
            val deltaX = center.x - mc.player.posX
            val deltaZ = center.z - mc.player.posZ

            var moveX = .0
            var moveZ = .0

            // check if deltaX above 0.05
            if (abs(deltaX) > 0.05) {
                // change x by 0.05 in the direction of center
                moveX =
                    if (deltaX > 0)
                        0.05
                    else
                        -0.05

            } else if (abs(deltaZ) > 0.05) {
                // change z by 0.05 in the direction of center
                moveZ =
                    if (deltaZ > 0)
                        0.05
                    else
                        -0.05

                // check if deltaZ is larger then deltaX
            } else if (abs(deltaZ) > abs(deltaX)) {
                // move set mc.player.posZ to center.z
                moveZ = deltaZ
            } else {
                // move set mc.player.posX to center.x
                moveX= deltaX
            }

            // set motion to zero
            mc.player.motionX = .0
            mc.player.motionZ = .0

            mc.player.move(MoverType.SELF, moveX, .0, moveZ)

            // check if player is within 0.03 blocks of the center
            if (abs(deltaX) < 0.01 && abs(deltaZ) < 0.01) {
                this.isMovingToCenter = false
            }

            // send position packet
            mc.player.connection.sendPacket(
                CPacketPlayer.Position(
                    mc.player.posX,
                    mc.player.posY,
                    mc.player.posZ,
                    mc.player.onGround
                )
            )
        }

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
                    initialBlockPos = BlockPos(mc.player.posX.toInt(), mc.player.posY.toInt(), mc.player.posZ.toInt())
                }
            }
        }
        val playerPos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
        for (blockPos in BlockUtil.getNeighbours(playerPos)) {
            if (checkAndPlaceBlock(blockPos)) {
                return
            }
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
            if (mc.world.getBlockState(pos).block != Blocks.OBSIDIAN ||
                mc.world.getBlockState(pos).block != Blocks.BEDROCK) {
                inHole = false
            }
        }
        return inHole
    }

    override fun onConstTick() {
        if (!toggled) {
            if (checkHole() && enableInHole.value){
                this.toggle()
            }
        }
    }
}
