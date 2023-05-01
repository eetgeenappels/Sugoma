package me.eetgeenappels.mixin;

import me.eetgeenappels.sugoma.Sugoma;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerControllerMP.class)
public class PlayerControllerMPMixin {
    @Inject(method = "attackEntity(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void onPlayerAttack(EntityPlayer player, Entity entity, CallbackInfo ci) {

        Sugoma.Companion.getEvents().onPlayerAttack(player, entity);

    }

}
