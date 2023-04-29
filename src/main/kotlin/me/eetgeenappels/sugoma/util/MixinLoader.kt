package me.eetgeenappels.sugoma.util

import me.eetgeenappels.sugoma.Sugoma
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.Mixins

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Sugoma")
@IFMLLoadingPlugin.SortingIndex(9999)
class MixinLoader: IFMLLoadingPlugin {
    init {
        MixinBootstrap.init()
        Mixins.addConfiguration("mixins.sugoma.json")
        println("Sugoma: Loaded Mixin Bootstrap")
    }
    override fun getASMTransformerClass(): Array<String>? = null
    override fun getModContainerClass(): String? = null

    override fun getSetupClass(): String? = null

    override fun injectData(data: Map<String, Any?>) {}

    override fun getAccessTransformerClass(): String? = null


}
