package com.chaotic_loom.loom.core.nbt;

public interface IAttachmentHolder {
    <T extends IEntityAttachment> T getAttachment(AttachmentType<T> type);
    boolean hasAttachment(AttachmentType<?> type);
}
