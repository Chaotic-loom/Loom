package com.chaotic_loom.loom.mixin.natives;

import com.chaotic_loom.loom.platform.Services;
import com.mojang.text2speech.Narrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Narrator.class, remap = false)
public interface NarratorMixin {

    /**
     This disables the narrator on the dev environment, since it spams the console with errors on Linux. It is just annoying man...
     */
    @Inject(method = "getNarrator", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelNarrator(CallbackInfoReturnable<Narrator> cir) {
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            cir.setReturnValue(Narrator.EMPTY);
            cir.cancel();
        }
    }
}
