package com.chaotic_loom.loom.core.networking;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.Minecraft;

public class PacketContext {
    private final Player player;
    private final boolean isClientSide;

    public PacketContext(Player player, boolean isClientSide) {
        this.player = player;
        this.isClientSide = isClientSide;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isClientSide() {
        return isClientSide;
    }

    // CRITICAL: Packets arrive on the Netty IO thread.
    // World modifications must happen on the main thread.
    public void enqueueWork(Runnable runnable) {
        if (isClientSide) {
            Minecraft.getInstance().execute(runnable);
        } else {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.server.execute(runnable);
            }
        }
    }
}