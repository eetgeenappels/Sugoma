package me.eetgeenappels.sugoma

import me.eetgeenappels.sugoma.events.Events
import me.eetgeenappels.sugoma.module.ModuleManager
import me.eetgeenappels.sugoma.proxy.ClientProxy
import me.eetgeenappels.sugoma.ui.HUD
import me.eetgeenappels.sugoma.util.Reference
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import java.util.logging.Logger

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
class Sugoma {
    @Mod.Instance
    var instance: Sugoma? = null

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        MinecraftForge.EVENT_BUS.register(instance)
        FMLCommonHandler.instance().bus().register(Events())
        hud = HUD()
        MinecraftForge.EVENT_BUS.register(hud)
        moduleManager = ModuleManager()
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent?) {
    }

    @SubscribeEvent
    fun key(event: KeyInputEvent?) {
        if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) return
        try {
            if (Keyboard.isCreated()) {
                if (Keyboard.getEventKeyState()) {
                    val keycode = Keyboard.getEventKey()
                    if (keycode <= 0) return
                    for (m in moduleManager?.moduleList!!) {
                        if (m.key == keycode) m.toggle()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var moduleManager: ModuleManager? = null
        var logger = Logger.getLogger("Amogus Client")
        var hud: HUD? = null

        //@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
        //var proxy: ClientProxy? = null
    }
}
