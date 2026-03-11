package com.chaotic_loom.loom.builtin.packets;

import com.chaotic_loom.loom.Constants;
import com.chaotic_loom.loom.core.networking.CustomPacket;
import com.chaotic_loom.loom.core.networking.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TestPacket implements CustomPacket {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "test");
    private final int testValue;

    // 1. Constructor for SENDING (You already have this)
    public TestPacket(int testValue) {
        this.testValue = testValue;
    }

    // 2. ADD THIS: Constructor for DECODING (Receiving from the buffer)
    public TestPacket(FriendlyByteBuf buf) {
        this.testValue = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(testValue); // Make sure this matches the read order in the constructor above!
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static void handle(TestPacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            String side = context.isClientSide() ? "CLIENT" : "SERVER";
            String threadName = Thread.currentThread().getName();
            /*context.getPlayer().sendSystemMessage(
                    Component.literal("[" + side + "] Received TestPacket value: " + packet.testValue)
            );*/
            System.out.println(">>> [" + side + " - Thread: " + threadName + "] Received TestPacket value: " + packet.testValue);
        });
    }
}