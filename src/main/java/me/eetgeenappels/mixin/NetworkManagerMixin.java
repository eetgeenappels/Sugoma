package me.eetgeenappels.mixin;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import me.eetgeenappels.sugoma.Sugoma;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
    public void onPacketSend(Packet<?> packet, CallbackInfo ci) {
        if (Sugoma.Companion.getEvents().onPacket(packet)) ci.cancel();
    }
}
