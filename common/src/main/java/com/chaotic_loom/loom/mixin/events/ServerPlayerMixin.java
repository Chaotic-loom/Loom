package com.chaotic_loom.loom.mixin.events;

import com.chaotic_loom.loom.builtin.events.ServerEvents;
import com.chaotic_loom.loom.core.events.EventResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void notifyDeath(DamageSource source, CallbackInfo ci) {
        if (ServerEvents.ENTITY_DIE.invoker().onEvent((LivingEntity) (Object) this, source) == EventResult.CANCEL) {
            ci.cancel();
        }
    }
}
