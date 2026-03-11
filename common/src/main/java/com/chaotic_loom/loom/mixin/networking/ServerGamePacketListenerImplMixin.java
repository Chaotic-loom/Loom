package com.chaotic_loom.loom.mixin.networking;

import com.chaotic_loom.loom.core.networking.NetworkRegistry;
import com.chaotic_loom.loom.core.networking.PacketContext;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        PacketContext context = new PacketContext(listener.player, false);

        // packet.getIdentifier() and packet.getData() are 1.20.1 MojMaps
        if (NetworkRegistry.INSTANCE.handleIncoming(packet.getIdentifier(), packet.getData(), context)) {
            ci.cancel(); // We handled it, stop vanilla processing
        }
    }
}
