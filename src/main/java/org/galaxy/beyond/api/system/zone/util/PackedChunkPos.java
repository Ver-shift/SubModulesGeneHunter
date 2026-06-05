package org.galaxy.beyond.api.system.zone.util;

import net.minecraft.world.level.ChunkPos;

public final class PackedChunkPos {

    private PackedChunkPos() {
    }

    public static long pack(ChunkPos pos) {
        return pack(pos.x(), pos.z());
    }

    public static long pack(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    public static ChunkPos unpack(long packed) {
        return new ChunkPos(x(packed), z(packed));
    }

    public static int x(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    public static int z(long packed) {
        return (int) ((packed >> 32) & 0xFFFFFFFFL);
    }
}
