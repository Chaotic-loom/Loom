package com.chaotic_loom.loom.mixin.events;

import com.chaotic_loom.loom.builtin.events.ServerEvents;
import com.chaotic_loom.loom.core.events.EventResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/level/ServerLevel$EntityCallbacks")
public class ServerLevelEntityCallbacksMixin {
    @Unique private ServerLevel loom_capturedLevel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void captureServerLevel(ServerLevel level, CallbackInfo ci) {
        this.loom_capturedLevel = level;
    }

    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
        if (ServerEvents.ENTITY_LOAD.invoker().onEvent(this.loom_capturedLevel, entity) == EventResult.CANCEL) {
            ci.cancel();
        }
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void invokeEntityUnloadEvent(Entity entity, CallbackInfo ci) {
        if (ServerEvents.ENTITY_UNLOAD.invoker().onEvent(this.loom_capturedLevel, entity) == EventResult.CANCEL) {
            ci.cancel();
        }
    }
}
