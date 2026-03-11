package com.chaotic_loom.loom.core.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomPacket {
    // Writes the packet data to the buffer
    void write(FriendlyByteBuf buf);

    // The unique identifier for this packet
    ResourceLocation getId();
}