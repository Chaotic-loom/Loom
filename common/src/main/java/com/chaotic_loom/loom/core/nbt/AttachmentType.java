package com.chaotic_loom.loom.core.nbt;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import java.util.function.Function;

public class AttachmentType<T extends IEntityAttachment> {
    private final ResourceLocation id;
    private final Function<Entity, T> factory;

    public AttachmentType(ResourceLocation id, Function<Entity, T> factory) {
        this.id = id;
        this.factory = factory;
    }

    public ResourceLocation getId() {
        return id;
    }

    public T createInstance(Entity entity) {
        return factory.apply(entity);
    }
}