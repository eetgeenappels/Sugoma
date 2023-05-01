package me.eetgeenappels.sugoma.module.modules.combat

import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.settings.ModeSetting
import me.eetgeenappels.sugoma.module.modules.settings.SliderSetting
import me.eetgeenappels.sugoma.module.modules.settings.ToggleSetting
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand

object AutoTotem : Module("AutoTotem", "Automatically places totems in your hand", Category.Combat) {
    val mode: ModeSetting =  ModeSetting("Mode", arrayOf("Totem", "Gapple", "Crystal"))
    val hp_for_totem: SliderSetting = SliderSetting("HpForTotem", 1f, 36f, 16f, 0)
    val swordGapple: ToggleSetting = ToggleSetting("SwordGapple", false)

    override fun onTick() {
        if (mode.currentModeIndex == 0) {
            if (mc.player.health + mc.player.absorptionAmount < hp_for_totem.value) {
                if (noTotem()) setTotem()
            } else {
                if (isSwordInMainHand && swordGapple.value) {
                    if (noGapple()) setGapple()
                } else {
                    if (noTotem()) setTotem()
                }
            }
        }
        if (mode.currentModeIndex == 1) {
            if (mc.player.health + mc.player.absorptionAmount < hp_for_totem.value) {
                if (noTotem()) setTotem()
            } else {
                if (noGapple()) setGapple()
            }
        }
        if (mode.currentModeIndex == 2) {
            if (mc.player.health + mc.player.absorptionAmount < hp_for_totem.value) {
                if (noTotem()) setTotem()
            } else {
                if (isSwordInMainHand && swordGapple.value) {
                    if (noGapple()) setGapple()
                } else {
                    if (noCrystal()) setCrystal()
                }
            }
        }
    }

    private fun noCrystal(): Boolean {
        val offhandStack = mc.player.heldItemOffhand
        return offhandStack.isEmpty || offhandStack.item !== Items.END_CRYSTAL
    }

    private fun noTotem(): Boolean {
        val offhandStack = mc.player.heldItemOffhand
        return offhandStack.isEmpty || offhandStack.item !== Items.TOTEM_OF_UNDYING
    }

    private fun noGapple(): Boolean {
        val offhandStack = mc.player.heldItemOffhand
        return offhandStack.isEmpty || offhandStack.item !== Items.GOLDEN_APPLE
    }

    private val isSwordInMainHand: Boolean
        private get() {
            val mainhandStack = mc.player.heldItemMainhand
            return !mainhandStack.isEmpty && mainhandStack.item === Items.DIAMOND_SWORD || mainhandStack.item === Items.IRON_SWORD || mainhandStack.item === Items.WOODEN_SWORD || mainhandStack.item === Items.GOLDEN_SWORD || mainhandStack.item === Items.STONE_SWORD
        }

    private fun SwitchOffHand(item: Item) {
        if (mc.player.heldItemOffhand.item !== item) {
            val slot = getItemSlot(item)
            if (slot != -1) {
                mc.playerController.windowClick(
                    mc.player.inventoryContainer.windowId, slot, 0,
                    ClickType.PICKUP, mc.player
                )
                mc.playerController.windowClick(
                    mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP,
                    mc.player
                )
                mc.playerController.windowClick(
                    mc.player.inventoryContainer.windowId, slot, 0,
                    ClickType.PICKUP, mc.player
                )
                mc.playerController.updateController()
            }
        }
    }

    fun getItemSlot(input: Item): Int {
        if (mc.player == null) return 0
        for (i in mc.player.inventoryContainer.inventory.indices) {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8) continue
            val s = mc.player.inventoryContainer.inventory[i]
            if (s.isEmpty) continue
            if (s.item === input) {
                return i
            }
        }
        return -1
    }

    fun setTotem() {
        if (noTotem()) {
            for (i in 0 until mc.player.inventory.sizeInventory) {
                val itemStack = mc.player.inventory.getStackInSlot(i)
                if (!itemStack.isEmpty && itemStack.item === Items.TOTEM_OF_UNDYING) {
                    SwitchOffHand(Items.TOTEM_OF_UNDYING)
                    return
                }
            }
            for (i in 0..8) {
                val itemStack = mc.player.inventory.getStackInSlot(i)
                if (!itemStack.isEmpty && itemStack.item === Items.TOTEM_OF_UNDYING) {
                    mc.player.inventory.setInventorySlotContents(i, ItemStack.EMPTY)
                    mc.player.setHeldItem(EnumHand.OFF_HAND, itemStack)
                    return
                }
            }
        }
    }

    fun setGapple() {
        for (i in 0 until mc.player.inventory.sizeInventory) {
            val itemStack = mc.player.inventory.getStackInSlot(i)
            if (!itemStack.isEmpty && itemStack.item === Items.GOLDEN_APPLE) {
                SwitchOffHand(Items.GOLDEN_APPLE)
                return
            }
        }
        for (i in 0..8) {
            val itemStack = mc.player.inventory.getStackInSlot(i)
            if (!itemStack.isEmpty && itemStack.item === Items.GOLDEN_APPLE) {
                mc.player.inventory.setInventorySlotContents(i, ItemStack.EMPTY)
                mc.player.setHeldItem(EnumHand.OFF_HAND, itemStack)
                return
            }
        }
    }

    fun setCrystal() {
        for (i in 0 until mc.player.inventory.sizeInventory) {
            val itemStack = mc.player.inventory.getStackInSlot(i)
            if (!itemStack.isEmpty && itemStack.item === Items.END_CRYSTAL) {
                SwitchOffHand(Items.END_CRYSTAL)
                return
            }
        }
        for (i in 0..8) {
            val itemStack = mc.player.inventory.getStackInSlot(i)
            if (!itemStack.isEmpty && itemStack.item === Items.END_CRYSTAL) {
                mc.player.inventory.setInventorySlotContents(i, ItemStack.EMPTY)
                mc.player.setHeldItem(EnumHand.OFF_HAND, itemStack)
                return
            }
        }
    }
}
