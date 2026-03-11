package com.chaotic_loom.loom.core.nbt;

import net.minecraft.nbt.CompoundTag;

public interface IEntityAttachment {
    // Called when the entity is saved to disk or prepped for a full sync packet
    CompoundTag serializeNBT();

    // Called when the entity is loaded from disk or receives a full sync packet
    void deserializeNBT(CompoundTag tag);

    // Optional: a method to copy data when a player dies and respawns
    default void copyFrom(IEntityAttachment oldData) {}
}