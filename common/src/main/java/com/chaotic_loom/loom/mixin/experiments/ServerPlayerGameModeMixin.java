package com.chaotic_loom.loom.mixin.experiments;

import com.chaotic_loom.loom.builtin.packets.TestPacket;
import com.chaotic_loom.loom.core.networking.NetworkRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void onServerBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerGameMode gameMode = (ServerPlayerGameMode) (Object) this;

        // Broadcast to everyone on the server!
        NetworkRegistry.sendToAllPlayers(gameMode.level.getServer(), new TestPacket(99));
    }
}
