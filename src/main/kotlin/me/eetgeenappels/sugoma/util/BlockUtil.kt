package me.eetgeenappels.sugoma.util

import me.eetgeenappels.sugoma.Sugoma
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.sqrt

object BlockUtil {
    private val mc = Minecraft.getMinecraft()
    val emptyBlocks: List<Block> = listOf(
        Blocks.AIR,
        Blocks.FLOWING_LAVA,
        Blocks.LAVA,
        Blocks.FLOWING_WATER,
        Blocks.WATER,
        Blocks.VINE,
        Blocks.SNOW_LAYER,
        Blocks.TALLGRASS,
        Blocks.FIRE
    )

    fun findNeighborBlocks(pos: BlockPos): List<Neighbour> {
        val neighbours: MutableList<Neighbour> = ArrayList()
        if (!emptyBlocks.contains(mc.world.getBlockState(pos.down()).block)) neighbours.add(
            Neighbour(
                pos.down(),
                EnumFacing.UP
            )
        )
        if (!emptyBlocks.contains(mc.world.getBlockState(pos.north()).block)) neighbours.add(
            Neighbour(
                pos.north(),
                EnumFacing.SOUTH
            )
        )
        if (!emptyBlocks.contains(mc.world.getBlockState(pos.east()).block)) neighbours.add(
            Neighbour(
                pos.east(),
                EnumFacing.WEST
            )
        )
        if (!emptyBlocks.contains(mc.world.getBlockState(pos.south()).block)) neighbours.add(
            Neighbour(
                pos.south(),
                EnumFacing.NORTH
            )
        )
        if (!emptyBlocks.contains(mc.world.getBlockState(pos.west()).block)) neighbours.add(
            Neighbour(
                pos.west(),
                EnumFacing.EAST
            )
        )
        return neighbours
    }
    fun getNeighbours(pos: BlockPos): List<BlockPos> {
        return listOf(
            pos.north(),
            pos.east(),
            pos.south(),
            pos.west(),
            pos.down())
    }

    fun place(posI: BlockPos, face: EnumFacing) {
        var pos = posI
        when (face) {
            EnumFacing.UP -> {
                pos = pos.down()
            }
            EnumFacing.NORTH -> {
                pos = pos.south()
            }
            EnumFacing.SOUTH -> {
                pos = pos.north()
            }
            EnumFacing.EAST -> {
                pos = pos.west()
            }
            EnumFacing.WEST -> {
                pos = pos.east()
            }

            else -> {}
        }
        val oldSlot = mc.player.inventory.currentItem
        var newSlot = -1
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack.item is ItemBlock) {
                if (!(stack.item as ItemBlock).block.defaultState.isFullBlock) {
                    continue
                }
            }
            if (stack.isEmpty || stack.item !is ItemBlock) continue
            newSlot = i
            break
        }
        if (newSlot == -1) {
            return
        }
        var crouched = false
        if (!mc.player.isSneaking && emptyBlocks.contains(mc.world.getBlockState(pos).block)) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            crouched = true
        }
        if (mc.player.heldItemMainhand.item !is ItemBlock) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(newSlot))
            mc.player.inventory.currentItem = newSlot
            mc.playerController.updateController()
        }
        val player = mc.player

        // Calculate the angle between the player's position and the target block position
        val dx = pos.x + 0.5 - player.posX
        val dy = pos.y + 0.5 - (player.posY + player.getEyeHeight())
        val dz = pos.z + 0.5 - player.posZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        val yaw = Math.toDegrees(atan2(dz, dx)).toFloat() - 90
        val pitch = -Math.toDegrees(atan2(dy, distance)).toFloat()
        // Send a packet to the server to update the player's rotation
        player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, player.onGround))

        mc.playerController.processRightClickBlock(
            mc.player,
            mc.world,
            pos,
            face,
            Vec3d(0.5, 0.5, 0.5),
            EnumHand.MAIN_HAND
        )
        mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(pos, face, EnumHand.MAIN_HAND,posI.x.toFloat(), posI.y.toFloat(), posI.z.toFloat()))
        mc.player.swingArm(EnumHand.MAIN_HAND)
        mc.player.connection.sendPacket(CPacketHeldItemChange(oldSlot))
        mc.player.inventory.currentItem = oldSlot
        mc.playerController.updateController()
        if (crouched) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
        }
    }

    fun placeSpecificBlock(posI: BlockPos, face: EnumFacing, block: Block) {
        var pos = posI
        when (face) {
            EnumFacing.UP -> {
                pos = pos.down()
            }
            EnumFacing.NORTH -> {
                pos = pos.south()
            }
            EnumFacing.SOUTH -> {
                pos = pos.north()
            }
            EnumFacing.EAST -> {
                pos = pos.west()
            }
            EnumFacing.WEST -> {
                pos = pos.east()
            }

            else -> {}
        }
        val oldSlot = mc.player.inventory.currentItem
        var newSlot = -1
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack.item is ItemBlock) {
                if (!(stack.item as ItemBlock).block.defaultState.isFullBlock) {
                    continue
                }
            }
            if (stack.isEmpty || stack.item !is ItemBlock) continue
            if ((stack.item as ItemBlock).block != block) continue
            newSlot = i
            break
        }
        if (newSlot == -1) {
            return
        }
        var crouched = false
        if (!mc.player.isSneaking && emptyBlocks.contains(mc.world.getBlockState(pos).block)) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            crouched = true
        }
        if (mc.player.heldItemMainhand.item !is ItemBlock) {
            mc.player.connection.sendPacket(CPacketHeldItemChange(newSlot))
            mc.player.inventory.currentItem = newSlot
            mc.playerController.updateController()
        }
        val player = mc.player

        // Calculate the angle between the player's position and the target block position
        val dx = pos.x + 0.5 - player.posX
        val dy = pos.y + 0.5 - (player.posY + player.getEyeHeight())
        val dz = pos.z + 0.5 - player.posZ
        val distance = sqrt(dx * dx + dy * dy + dz * dz)
        val yaw = Math.toDegrees(atan2(dz, dx)).toFloat() - 90
        val pitch = -Math.toDegrees(atan2(dy, distance)).toFloat()
        // Send a packet to the server to update the player's rotation
        player.connection.sendPacket(CPacketPlayer.Rotation(yaw, pitch, player.onGround))

        mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(pos, face, EnumHand.MAIN_HAND,posI.x.toFloat(), posI.y.toFloat(), posI.z.toFloat()))
        mc.player.swingArm(EnumHand.MAIN_HAND)
        mc.player.connection.sendPacket(CPacketHeldItemChange(oldSlot))
        mc.player.inventory.currentItem = oldSlot
        mc.playerController.updateController()
        if (crouched) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
        }
    }

    class Neighbour(var position: BlockPos, var placeFace: EnumFacing)
}
