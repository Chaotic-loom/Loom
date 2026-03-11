package com.chaotic_loom.loom.builtin.packets;

import com.chaotic_loom.loom.core.networking.NetworkRegistry;

public class LoomBuiltInPackets {
    public static void init() {
        NetworkRegistry.INSTANCE.register(
                TestPacket.ID,
                TestPacket::new, // Decoder
                TestPacket::handle // Handler
        );

        NetworkRegistry.INSTANCE.register(
                SyncEntityDataPacket.ID,
                SyncEntityDataPacket::new,
                SyncEntityDataPacket::handle
        );
    }
}
