package org.galaxy.beyond.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

public final class CompatUtil {
    private CompatUtil() {
    }

    public static ChunkPos chunkPos(BlockPos pos) {
        return new ChunkPos(pos);
    }

    public static ListTag getCompoundListOrEmpty(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_LIST) ? tag.getList(key, Tag.TAG_COMPOUND) : new ListTag();
    }

    public static ListTag getStringListOrEmpty(CompoundTag tag, String key) {
        return tag.contains(key, Tag.TAG_LIST) ? tag.getList(key, Tag.TAG_STRING) : new ListTag();
    }

    public static String asString(Tag tag) {
        return tag.getAsString();
    }
}
