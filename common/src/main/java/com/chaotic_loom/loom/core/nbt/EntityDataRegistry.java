package com.chaotic_loom.loom.core.nbt;

import net.minecraft.resources.ResourceLocation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EntityDataRegistry {
    // Stores all registered data types mapped by their unique ResourceLocation
    private static final Map<ResourceLocation, AttachmentType<?>> REGISTRY = new HashMap<>();

    /**
     * Modders call this during mod init to register their custom data.
     */
    public static <T extends IEntityAttachment> AttachmentType<T> register(AttachmentType<T> type) {
        if (REGISTRY.containsKey(type.getId())) {
            throw new IllegalArgumentException("Duplicate entity data ID registered: " + type.getId());
        }
        REGISTRY.put(type.getId(), type);
        return type;
    }

    /**
     * Used internally by the Network Packet Handler to find the right factory.
     */
    public static AttachmentType<?> getType(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    /**
     * Used internally by the EntityMixin to iterate over everything when loading from disk.
     */
    public static Collection<AttachmentType<?>> getAllTypes() {
        return REGISTRY.values();
    }
}