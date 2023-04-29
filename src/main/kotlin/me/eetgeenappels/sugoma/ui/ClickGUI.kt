package me.eetgeenappels.sugoma.ui

import me.eetgeenappels.sugoma.Sugoma
import me.eetgeenappels.sugoma.module.Category
import me.eetgeenappels.sugoma.module.Module
import me.eetgeenappels.sugoma.module.modules.render.ClickGuiModule
import me.eetgeenappels.sugoma.util.Reference
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import java.io.IOException
import java.util.function.Consumer

class ClickGUI(private val clickGuiModule: ClickGuiModule) : GuiScreen() {
    private val categoryViews: MutableList<CategoryView>

    init {
        categoryViews = ArrayList()
        categoryViews.add(CategoryView(Category.Combat, 5, 5))
        categoryViews.add(CategoryView(Category.Player, 5 + 140, 5))
        categoryViews.add(CategoryView(Category.Render, 5 + 140 * 2, 5))
        categoryViews.add(CategoryView(Category.World, 5 + 140 * 3, 5))
    }

    override fun initGui() {}
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        for (categoryView in categoryViews) {
            categoryView.updatePos(mouseX, mouseY)
        }
        for (categoryView in categoryViews) {
            categoryView.onRender()
        }
        for (categoryView in categoryViews) {
            categoryView.onPostRender()
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (view in categoryViews) {
            view.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        if (keyCode == Keyboard.KEY_COMMA) {
            Companion.mc.displayGuiScreen(null as GuiScreen?)
            if (Companion.mc.currentScreen == null) {
                Companion.mc.setIngameFocus()
            }
        }
        for (view in categoryViews) {
            view.keyTyped(typedChar, keyCode)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (view in categoryViews) {
            view.mouseReleased(mouseX, mouseY, state)
        }
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            // Do something when the button is clicked
            Sugoma.Companion.logger.info("The button was clicked!")
        }
    }

    override fun onGuiClosed() {
        clickGuiModule.toggled = false
    }

    // front-end components to view categories and modules and be able to toggle different modules.
    internal class CategoryView(var category: Category, var offsetX: Int, var offsetY: Int) {
        var width: Int
        var height = 0
        var grappled = false
        var mouseOffsetX = 0
        var mouseOffsetY = 0
        private val moduleViews: MutableList<ModuleView>
        private var mouseX = 0
        private var mouseY = 0

        init {
            width = viewWidth
            moduleViews = ArrayList()
            var y = (1 + fr.FONT_HEIGHT) * 2
            for (module in Sugoma.moduleManager.getModulesByCategory(
                category
            )) {
                moduleViews.add(ModuleView(module, offsetX, offsetY + y))
                y += fr.FONT_HEIGHT + 1
            }
        }

        // update position based on mouse movement
        fun updatePos(mouseX: Int, mouseY: Int) {
            if (grappled) {
                offsetX = mouseX + mouseOffsetX
                offsetY = mouseY + mouseOffsetY
            } else {
                // TODO: Check of mouse is over module setting.
                // TODO: Maybe add options for clarification of module settings later.
                this.mouseX = mouseX
                this.mouseY = mouseY
            }
            moduleViews.forEach(Consumer { moduleView: ModuleView -> moduleView.updatePos(mouseX, mouseY, offsetX) })
        }

        fun onRender() {
            calculateHeight()
            var y = 0
            // draw rectangles
            drawRectangle(
                offsetX, offsetY,
                width, height, 0f, 0f, 0f, 0.5f
            )
            drawRectangle(
                offsetX, offsetY,
                width, fr.FONT_HEIGHT, 0f, 0f, 0f, 1f
            )
            fr.drawString(category.categoryName, offsetX + 2, offsetY + y, -0x1)
            y += fr.FONT_HEIGHT + 1

            // draw modules and settings
            for (i in Sugoma.moduleManager.getModulesByCategory(category).indices) {
                val module: Module = Sugoma.moduleManager.getModulesByCategory(
                    category
                ).get(i)
                if (module.name.equals("ClickGui", ignoreCase = true)) continue
                if (module.toggled) drawRectangle(
                    offsetX, offsetY + y,
                    width, fr.FONT_HEIGHT, 0f, 1f, 1f, 1f
                )
                fr.drawString(module.name, offsetX + 2, offsetY + y, -0x1)
                val moduleView = moduleViews[i]
                if (moduleView.expanded) {
                    moduleView.onRender()
                    y += moduleViews[i].calculateHeight()
                }
                y += fr.FONT_HEIGHT + 1
            }
        }

        // calculates the height of the view (useful for drawing the original rectangles)
        private fun calculateHeight() {
            var y = 1 + fr.FONT_HEIGHT
            for (i in Sugoma.moduleManager.getModulesByCategory(category).indices) {
                if (Sugoma.moduleManager.getModulesByCategory(category)[i].name == "ClickGui")
                moduleViews[i].offsetX = offsetX
                moduleViews[i].offsetY = offsetY + y + fr.FONT_HEIGHT
                y += fr.FONT_HEIGHT + 1
                if (moduleViews[i].expanded) y += moduleViews[i].calculateHeight()
            }
            height = y
        }

        fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
            if (mouseButton == 0) {
                if (offsetX < mouseX && mouseX < offsetX + width) {
                    var y = fr.FONT_HEIGHT + 1
                    if (offsetY < mouseY && mouseY < offsetY + fr.FONT_HEIGHT + 1) {
                        mouseOffsetX = offsetX - mouseX
                        mouseOffsetY = offsetY - mouseY
                        grappled = true
                        return
                    }
                    for (i in Sugoma.moduleManager.getModulesByCategory(category).indices) {
                        if (offsetY + y < mouseY && mouseY < offsetY + y + fr.FONT_HEIGHT + 1) {
                            Sugoma.moduleManager.getModulesByCategory(category).get(i).toggle()
                            return
                        }
                        y += fr.FONT_HEIGHT + 1
                        if (moduleViews[i].expanded) y += moduleViews[i].calculateHeight()
                    }
                }
            }
            if (mouseButton == 1) {
                if (offsetX < mouseX && mouseX < offsetX + width) {
                    var y = fr.FONT_HEIGHT + 1
                    for (i in Sugoma.moduleManager.getModulesByCategory(category).indices) {
                        if (offsetY + y < mouseY && mouseY < offsetY + y + fr.FONT_HEIGHT + 1) {
                            moduleViews[i].expanded = !moduleViews[i].expanded
                            return
                        }
                        y += fr.FONT_HEIGHT + 1
                        if (moduleViews[i].expanded) y += moduleViews[i].calculateHeight()
                    }
                }
            }
            for (moduleView in moduleViews) {
                moduleView.mouseClicked(mouseX, mouseY, mouseButton)
            }
        }

        fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
            if (state == 0) {
                grappled = false
            }
            moduleViews.forEach(Consumer { moduleView: ModuleView -> moduleView.onMouseRelease(mouseX, mouseY, state) })
        }

        fun onPostRender() {
            var y = 1 + fr.FONT_HEIGHT
            for (i in Sugoma.moduleManager.getModulesByCategory(category).indices) {
                val module: Module = Sugoma.moduleManager.getModulesByCategory(
                    category
                ).get(i)
                if (offsetY + y < mouseY && mouseY < offsetY + y + fr.FONT_HEIGHT + 1 && offsetX < mouseX && mouseX < offsetX + width) {
                    // render description box
                    drawRectangle(
                        mouseX, mouseY, fr.getStringWidth(module.description) + 4, fr.FONT_HEIGHT + 4,
                        0f, 0f, 0f, 0.5f
                    )
                    fr.drawString(module.description, mouseX + 2, mouseY + 2, -0x1)
                }
                y += fr.FONT_HEIGHT + 1
                if (moduleViews[i].expanded) y += moduleViews[i].calculateHeight()
            }
        }

        fun keyTyped(typedChar: Char, keyCode: Int) {
            for (view in moduleViews) {
                view.keyTyped(typedChar, keyCode)
            }
        }
    }

    // front-end component to view the module settings.
    internal class ModuleView(val module: Module, var offsetX: Int, var offsetY: Int) {
        var width: Int
        var height = 0
        var expanded = false
        fun onRender() {
            calculateHeight()
            var y = 5
            for (setting in module.settings) {
                setting.render(offsetX, offsetY + y)
                y += fr.FONT_HEIGHT + 1
            }
            if (waitOnKey) mc.fontRenderer.drawString(
                "Bind: ...",
                offsetX + 2,
                offsetY + y,
                -0x1
            ) else mc.fontRenderer.drawString(
                "Bind: " + Keyboard.getKeyName(
                    module.key
                ), offsetX + 2, offsetY + y, -0x1
            )
            y += fr.FONT_HEIGHT + 1
        }

        fun updatePos(mouseX: Int, mouseY: Int, offsetX: Int) {
            this.offsetX = offsetX
            for (setting in module.settings) {
                setting.updatePos(mouseX, mouseY)
            }
        }

        // calculates the height of the view (useful for drawing the rectangles of the category view)
        fun calculateHeight(): Int {
            height = 5
            for (ignored in module.settings) {
                height += fr.FONT_HEIGHT + 1
            }
            height += fr.FONT_HEIGHT + 1
            return height
        }

        private var waitOnKey = false

        init {
            width = viewWidth
        }

        fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
            if (module.settings.size == 0) return
            var y = 5
            for (setting in module.settings) {
                setting.onMouseClick(offsetX, offsetY + y, mouseX, mouseY, mouseButton)
                y += fr.FONT_HEIGHT + 1
            }
            if (mouseButton == 0) {
                if (offsetX < mouseX && mouseX < offsetX + viewWidth && offsetY + y < mouseY && mouseY < offsetY + y + mc.fontRenderer.FONT_HEIGHT) {
                    waitOnKey = true
                }
                y += fr.FONT_HEIGHT + 1
            }
        }

        fun onMouseRelease(mouseX: Int, mouseY: Int, state: Int) {
            if (module.settings.size == 0) return
            var y = 5
            for (setting in module.settings) {
                setting.onMouseRelease(offsetX, offsetY + y, mouseX, mouseY, state)
                y += fr.FONT_HEIGHT + 1
            }
        }

        fun keyTyped(typedChar: Char, keycode: Int) {
            if (waitOnKey) {
                when (keycode) {
                    Keyboard.KEY_BACK -> {
                        module.key = 0
                        waitOnKey = false
                        Sugoma.moduleManager.save()
                    }
                    Keyboard.KEY_ESCAPE -> {
                        module.key = 0
                        waitOnKey = false
                        Sugoma.moduleManager.save()
                    }
                    Keyboard.KEY_COMMA -> {
                        waitOnKey = false
                    }
                    else -> {
                        waitOnKey = false
                        module.key = keycode
                        Sugoma.moduleManager.save()
                    }
                }
            }
        }
    }

    companion object {
        const val viewWidth = 120
        val mc: Minecraft = Minecraft.getMinecraft()
        val fr: FontRenderer = mc.fontRenderer
        private val rounded_rectangle = ResourceLocation(Reference.MODID, "textures/rounded_rectangle.png")
        fun drawRectangle(
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            red: Float,
            green: Float,
            blue: Float,
            alpha: Float
        ) {
            GlStateManager.color(red, green, blue, alpha) // set color to red
            mc.renderEngine.bindTexture(rounded_rectangle)
            drawScaledCustomSizeModalRect(x, y, 0f, 0f, width, height, width, height, width.toFloat(), height.toFloat())
        }
    }
}
