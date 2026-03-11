package com.chaotic_loom.loom.mixin.nbt;

import com.chaotic_loom.loom.core.nbt.AttachmentType;
import com.chaotic_loom.loom.core.nbt.EntityDataRegistry;
import com.chaotic_loom.loom.core.nbt.IAttachmentHolder;
import com.chaotic_loom.loom.core.nbt.IEntityAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements IAttachmentHolder {

    @Unique
    private final Map<AttachmentType<?>, IEntityAttachment> mylib$attachments = new HashMap<>();

    @Unique
    private static final String MYLIB_NBT_KEY = "MyLib_Attachments";

    @Override
    public <T extends IEntityAttachment> T getAttachment(AttachmentType<T> type) {
        // Lazy-load the attachment if it doesn't exist yet
        return (T) mylib$attachments.computeIfAbsent(type,
                t -> t.createInstance((Entity) (Object) this));
    }

    @Override
    public boolean hasAttachment(AttachmentType<?> type) {
        return mylib$attachments.containsKey(type);
    }

    // SAVING TO DISK
    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void mylib$saveAttachments(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (mylib$attachments.isEmpty()) return;

        CompoundTag attachmentsTag = new CompoundTag();
        for (Map.Entry<AttachmentType<?>, IEntityAttachment> entry : mylib$attachments.entrySet()) {
            CompoundTag data = entry.getValue().serializeNBT();
            if (data != null && !data.isEmpty()) {
                attachmentsTag.put(entry.getKey().getId().toString(), data);
            }
        }

        if (!attachmentsTag.isEmpty()) {
            tag.put(MYLIB_NBT_KEY, attachmentsTag);
        }
    }

    // LOADING FROM DISK
    @Inject(method = "load", at = @At("RETURN"))
    private void mylib$loadAttachments(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains(MYLIB_NBT_KEY, 10)) return; // 10 is CompoundTag

        CompoundTag attachmentsTag = tag.getCompound(MYLIB_NBT_KEY);

        // Iterate over YOUR custom registry
        for (AttachmentType<?> type : EntityDataRegistry.getAllTypes()) {
            String idStr = type.getId().toString();
            if (attachmentsTag.contains(idStr, 10)) {
                IEntityAttachment attachment = getAttachment(type);
                attachment.deserializeNBT(attachmentsTag.getCompound(idStr));
            }
        }
    }
}
