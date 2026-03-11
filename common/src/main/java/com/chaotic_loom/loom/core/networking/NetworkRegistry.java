package com.chaotic_loom.loom.core.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class NetworkRegistry {
    public static final NetworkRegistry INSTANCE = new NetworkRegistry();

    // Stores factories to decode the buffer into a packet
    private final Map<ResourceLocation, Function<FriendlyByteBuf, CustomPacket>> decoders = new HashMap<>();

    // Stores the handlers for executing the packet logic
    private final Map<ResourceLocation, BiConsumer<CustomPacket, PacketContext>> handlers = new HashMap<>();

    // Users call this to register their packets
    public <T extends CustomPacket> void register(ResourceLocation id, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, PacketContext> handler) {
        decoders.put(id, decoder::apply);
        // Unchecked cast is safe here because we guarantee the mapping by the generic method signature
        handlers.put(id, (BiConsumer<CustomPacket, PacketContext>) handler);
    }

    // Internal method called by our Mixins when a packet arrives
    public boolean handleIncoming(ResourceLocation id, FriendlyByteBuf buf, PacketContext context) {
        if (!decoders.containsKey(id)) {
            return false; // Not our packet, let vanilla handle it
        }

        CustomPacket packet = decoders.get(id).apply(buf);
        handlers.get(id).accept(packet, context);
        return true;
    }

    public static void sendToServer(CustomPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(packet.getId(), buf));
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        player.connection.send(new ClientboundCustomPayloadPacket(packet.getId(), buf));
    }

    public static void sendToAllPlayers(MinecraftServer server, CustomPacket packet) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendToPlayer(player, packet);
        }
    }
}