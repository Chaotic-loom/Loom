package com.chaotic_loom.loom.mixin.experiments;

import com.chaotic_loom.loom.builtin.packets.TestPacket;
import com.chaotic_loom.loom.core.networking.NetworkRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "swing", at = @At("HEAD"))
    private void onClientSwing(InteractionHand hand, CallbackInfo ci) {
        // We only want to send this once per swing, let's just use the main hand
        if (hand == InteractionHand.MAIN_HAND) {
            // Sends to the server!
            NetworkRegistry.sendToServer(new TestPacket(42));
        }
    }
}
