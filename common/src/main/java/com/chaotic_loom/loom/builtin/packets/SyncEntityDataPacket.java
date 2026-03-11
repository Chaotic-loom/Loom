package com.chaotic_loom.loom.builtin.packets;

import com.chaotic_loom.loom.Constants;
import com.chaotic_loom.loom.core.nbt.AttachmentType;
import com.chaotic_loom.loom.core.nbt.EntityDataRegistry;
import com.chaotic_loom.loom.core.nbt.IAttachmentHolder;
import com.chaotic_loom.loom.core.networking.CustomPacket;
import com.chaotic_loom.loom.core.networking.PacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SyncEntityDataPacket implements CustomPacket {
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "sync_entity_data");

    private final int entityId;
    private final ResourceLocation attachmentId;
    private final CompoundTag data;

    // 1. Constructor for SENDING (Server-side)
    public SyncEntityDataPacket(int entityId, ResourceLocation attachmentId, CompoundTag data) {
        this.entityId = entityId;
        this.attachmentId = attachmentId;
        this.data = data;
    }

    // 2. Constructor for DECODING (Receiving on the Client)
    public SyncEntityDataPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.attachmentId = buf.readResourceLocation();
        this.data = buf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeResourceLocation(this.attachmentId);
        buf.writeNbt(this.data);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    // 3. The Handler Logic
    public static void handle(SyncEntityDataPacket packet, PacketContext context) {
        context.enqueueWork(() -> {
            // Entity data syncing is almost always Server -> Client.
            // We ignore malicious or accidental Client -> Server packets here.
            if (!context.isClientSide()) return;

            Player player = context.getPlayer();
            if (player == null) return;

            Level level = player.level();
            if (level == null) return;

            // Fetch the entity using the network ID perfectly synced from the server
            Entity entity = level.getEntity(packet.entityId);

            if (entity instanceof IAttachmentHolder holder) {
                // Fetch the registered AttachmentType
                AttachmentType<?> type = EntityDataRegistry.getType(packet.attachmentId);

                // Deserialize the data directly into the entity's attachment instance
                if (type != null && packet.data != null) {
                    holder.getAttachment(type).deserializeNBT(packet.data);
                }
            }
        });
    }
}