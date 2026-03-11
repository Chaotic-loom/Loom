package com.chaotic_loom.loom.mixin.networking;

import com.chaotic_loom.loom.core.networking.NetworkRegistry;
import com.chaotic_loom.loom.core.networking.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        // Minecraft.getInstance().player might be null during very early login phases,
        // but for standard PLAY phase packets, it's safe.
        PacketContext context = new PacketContext(Minecraft.getInstance().player, true);

        if (NetworkRegistry.INSTANCE.handleIncoming(packet.getIdentifier(), packet.getData(), context)) {
            ci.cancel();
        }
    }
}
