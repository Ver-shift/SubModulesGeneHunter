package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

/**
 * 区块 Zone 数据 —— 三种 Zone 独立存储，同一区块可同时属于多个区域。
 */
public class LevelZoneData implements IPersistedSerializable {

    public static final MapCodec<LevelZoneData> CODEC = PersistedParser.createMapCodec(LevelZoneData::new);
    public static final StreamCodec<ByteBuf, LevelZoneData> STREAM_CODEC = PersistedParser.createStreamCodec(LevelZoneData::new);

    public LevelZoneData() {}

    // ============================================================
    // 三个独立 Set —— 序列化走 List<Long>
    // ============================================================

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Persisted
    private List<Long> safeZonesPacked = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Persisted
    private List<Long> nodeZonesPacked = new ArrayList<>();

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Persisted
    private List<Long> activeZonesPacked = new ArrayList<>();

    private transient Set<Long> safeSet, nodeSet, activeSet;

    private Set<Long> safe() { if (safeSet == null) safeSet = new HashSet<>(safeZonesPacked); return safeSet; }
    private Set<Long> node() { if (nodeSet == null) nodeSet = new HashSet<>(nodeZonesPacked); return nodeSet; }
    private Set<Long> act()  { if (activeSet == null) activeSet = new HashSet<>(activeZonesPacked); return activeSet; }

    private static long pack(ChunkPos p) { int x = p.getMinBlockX() >> 4, z = p.getMinBlockZ() >> 4; return ((long)x & 0xFFFFFFFFL) | (((long)z & 0xFFFFFFFFL) << 32); }
    private static ChunkPos unpack(long v) { return new ChunkPos((int)(v & 0xFFFFFFFFL), (int)((v >> 32) & 0xFFFFFFFFL)); }

    // ---- 查询 ----

    public boolean isSafe(ChunkPos p)   { return safe().contains(pack(p)); }
    public boolean isNode(ChunkPos p)   { return node().contains(pack(p)); }
    public boolean isActive(ChunkPos p) { return act().contains(pack(p)); }
    public boolean hasAny(ChunkPos p)   { return isSafe(p) || isNode(p) || isActive(p); }
    public boolean hasZones()           { return !safeZonesPacked.isEmpty() || !nodeZonesPacked.isEmpty() || !activeZonesPacked.isEmpty(); }

    public Set<ChunkPos> safeChunks()  { var s = new HashSet<ChunkPos>(); safe().forEach(v -> s.add(unpack(v))); return s; }
    public Set<ChunkPos> nodeChunks()  { var s = new HashSet<ChunkPos>(); node().forEach(v -> s.add(unpack(v))); return s; }
    public Set<ChunkPos> activeChunks(){ var s = new HashSet<ChunkPos>(); act().forEach(v -> s.add(unpack(v))); return s; }

    /** 返回所有 zone entries（兼容旧 API） */
    public Set<Map.Entry<ChunkPos, ZoneType>> getZoneEntries() {
        Set<Map.Entry<ChunkPos, ZoneType>> result = new HashSet<>();
        for (long v : safeZonesPacked) result.add(Map.entry(unpack(v), ZoneType.Safe_Zone));
        for (long v : nodeZonesPacked) result.add(Map.entry(unpack(v), ZoneType.Node_Zone));
        for (long v : activeZonesPacked) result.add(Map.entry(unpack(v), ZoneType.Active_Zone));
        return result;
    }

    // ---- 修改 ----

    public boolean addSafe(ChunkPos p)   { return safe().add(pack(p))   && syncPersisted(pack(p), safeZonesPacked, safeSet); }
    public boolean addNode(ChunkPos p)   { return node().add(pack(p))   && syncPersisted(pack(p), nodeZonesPacked, nodeSet); }
    public boolean addActive(ChunkPos p) { return act().add(pack(p))    && syncPersisted(pack(p), activeZonesPacked, activeSet); }

    private static boolean syncPersisted(long v, List<Long> list, Set<Long> set) { list.add(v); return true; }

    // ---- 兼容旧 API ----

    /** 按优先级返回最高优 ZoneType（Safe > Node > Active） */
    public ZoneType getZoneType(BlockPos pos) { return getZoneType(ChunkPos.containing(pos)); }

    public ZoneType getZoneType(ChunkPos p) {
        if (isSafe(p)) return ZoneType.Safe_Zone;
        if (isNode(p)) return ZoneType.Node_Zone;
        if (isActive(p)) return ZoneType.Active_Zone;
        return null;
    }

    public boolean addZone(ChunkPos p, ZoneType type) {
        return switch (type) {
            case Safe_Zone   -> addSafe(p);
            case Node_Zone   -> addNode(p);
            case Active_Zone -> addActive(p);
            case Empty       -> false;
        };
    }
}
