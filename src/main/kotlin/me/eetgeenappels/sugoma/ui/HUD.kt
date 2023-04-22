package me.eetgeenappels.sugoma.ui

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.util.Reference
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class HUD : Gui() {
    private val watermark = ResourceLocation(Reference.MODID, "textures/sugoma.png")
    private val mc = Minecraft.getMinecraft()
    @SubscribeEvent
    fun RenderOverlay(event: RenderGameOverlayEvent) {
        val sr = ScaledResolution(mc)
        val fontRenderer = mc.fontRenderer
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {

            //watermark
            GlStateManager.color(1f, 1f, 1f, 1f)
            mc.renderEngine.bindTexture(watermark)
            drawScaledCustomSizeModalRect(0, 0, 0f, 0f, 50, 50, 50, 50, 50f, 50f)
            drawRect(10, 10, 10, 10, -0x1)
            fontRenderer.drawStringWithShadow(
                Reference.NAME + " " + Reference.VERSION,
                2f, 1f,
                0x0000ffff
            )
            // render arraylist
            var height = 2
            for (module in Sugoma.moduleManager?.modules!!) if (!module.name
                    .equals("ClickGUI", ignoreCase = true) && module.toggled
            ) {
                fontRenderer.drawStringWithShadow(
                    module.name,
                    (
                            sr.scaledWidth - fontRenderer.getStringWidth(module.name) - 2).toFloat(),
                    height.toFloat(),
                    -0x1
                )
                height += fontRenderer.FONT_HEIGHT
            }
        }
    }
}
