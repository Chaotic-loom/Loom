package com.chaotic_loom.loom.mixin.events;

import com.chaotic_loom.loom.builtin.events.ServerEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Shadow @Final private Level level;

    @Inject(method = "setLoaded", at = @At("HEAD"))
    private void loaded(boolean state, CallbackInfo ci) {
        if (this.level instanceof ServerLevel serverLevel) {
            if (state) {
                ServerEvents.CHUNK_LOAD.invoker().onEvent(serverLevel, (LevelChunk) (Object) this);
            } else {
                ServerEvents.CHUNK_UNLOAD.invoker().onEvent(serverLevel, (LevelChunk) (Object) this);
            }
        } else if (this.level.isClientSide()) {
            // TODO: To implement clientsided chunk events
        }
    }
}
