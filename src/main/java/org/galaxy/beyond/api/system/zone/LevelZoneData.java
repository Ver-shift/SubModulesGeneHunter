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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.zone.util.PackedChunkPos;

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

    // ---- 查询 ----

    public boolean isSafe(ChunkPos p)   { return safe().contains(PackedChunkPos.pack(p)); }
    public boolean isNode(ChunkPos p)   { return node().contains(PackedChunkPos.pack(p)); }
    public boolean isActive(ChunkPos p) { return act().contains(PackedChunkPos.pack(p)); }
    public boolean hasAny(ChunkPos p)   { return isSafe(p) || isNode(p) || isActive(p); }
    public boolean hasZones()           { return !safeZonesPacked.isEmpty() || !nodeZonesPacked.isEmpty() || !activeZonesPacked.isEmpty(); }

    public Set<ChunkPos> safeChunks()  { var s = new HashSet<ChunkPos>(); safe().forEach(v -> s.add(PackedChunkPos.unpack(v))); return s; }
    public Set<ChunkPos> nodeChunks()  { var s = new HashSet<ChunkPos>(); node().forEach(v -> s.add(PackedChunkPos.unpack(v))); return s; }
    public Set<ChunkPos> activeChunks(){ var s = new HashSet<ChunkPos>(); act().forEach(v -> s.add(PackedChunkPos.unpack(v))); return s; }

    /** 返回所有 Zone 条目；同一 ChunkPos 可按不同 ZoneType 出现多次。 */
    public Set<Map.Entry<ChunkPos, ZoneType>> getZoneEntries() {
        Set<Map.Entry<ChunkPos, ZoneType>> result = new HashSet<>();
        for (long v : safeZonesPacked) result.add(Map.entry(PackedChunkPos.unpack(v), ZoneType.Safe_Zone));
        for (long v : nodeZonesPacked) result.add(Map.entry(PackedChunkPos.unpack(v), ZoneType.Node_Zone));
        for (long v : activeZonesPacked) result.add(Map.entry(PackedChunkPos.unpack(v), ZoneType.Active_Zone));
        return result;
    }

    // ---- 修改 ----

    public boolean addSafe(ChunkPos p)   { long packed = PackedChunkPos.pack(p); return safe().add(packed) && syncPersisted(packed, safeZonesPacked, safeSet); }
    public boolean addNode(ChunkPos p)   { long packed = PackedChunkPos.pack(p); return node().add(packed) && syncPersisted(packed, nodeZonesPacked, nodeSet); }
    public boolean addActive(ChunkPos p) { long packed = PackedChunkPos.pack(p); return act().add(packed) && syncPersisted(packed, activeZonesPacked, activeSet); }

    private static boolean syncPersisted(long v, List<Long> list, Set<Long> set) { list.add(v); return true; }

    public boolean addPacked(ZoneType type, long packed) {
        return switch (type) {
            case Safe_Zone -> safe().add(packed) && syncPersisted(packed, safeZonesPacked, safeSet);
            case Node_Zone -> node().add(packed) && syncPersisted(packed, nodeZonesPacked, nodeSet);
            case Active_Zone -> act().add(packed) && syncPersisted(packed, activeZonesPacked, activeSet);
            case Empty -> false;
        };
    }

    public boolean addPackedAll(ZoneType type, Collection<Long> packedChunks) {
        boolean changed = false;
        for (long packed : packedChunks) {
            if (addPacked(type, packed)) changed = true;
        }
        return changed;
    }

    public boolean removePacked(ZoneType type, long packed) {
        return switch (type) {
            case Safe_Zone -> removePacked(packed, safeZonesPacked, safe());
            case Node_Zone -> removePacked(packed, nodeZonesPacked, node());
            case Active_Zone -> removePacked(packed, activeZonesPacked, act());
            case Empty -> false;
        };
    }

    public boolean removePackedAll(ZoneType type, Collection<Long> packedChunks) {
        boolean changed = false;
        for (long packed : packedChunks) {
            if (removePacked(type, packed)) changed = true;
        }
        return changed;
    }

    private static boolean removePacked(long packed, List<Long> list, Set<Long> set) {
        if (!set.remove(packed)) return false;
        list.remove(packed);
        return true;
    }

    public List<Long> getPacked(ZoneType type) {
        return switch (type) {
            case Safe_Zone -> List.copyOf(safe());
            case Node_Zone -> List.copyOf(node());
            case Active_Zone -> List.copyOf(act());
            case Empty -> List.of();
        };
    }

    public LevelZoneData copy() {
        LevelZoneData copy = new LevelZoneData();
        copy.addPackedAll(ZoneType.Safe_Zone, safe());
        copy.addPackedAll(ZoneType.Node_Zone, node());
        copy.addPackedAll(ZoneType.Active_Zone, act());
        return copy;
    }

    public void replaceFrom(LevelZoneData other) {
        safeZonesPacked = new ArrayList<>(other.safe());
        nodeZonesPacked = new ArrayList<>(other.node());
        activeZonesPacked = new ArrayList<>(other.act());
        safeSet = null;
        nodeSet = null;
        activeSet = null;
    }

    public static void writeFull(FriendlyByteBuf buf, LevelZoneData data) {
        writeLongList(buf, data.safe());
        writeLongList(buf, data.node());
        writeLongList(buf, data.act());
    }

    public static LevelZoneData readFull(FriendlyByteBuf buf) {
        LevelZoneData data = new LevelZoneData();
        data.addPackedAll(ZoneType.Safe_Zone, readLongList(buf));
        data.addPackedAll(ZoneType.Node_Zone, readLongList(buf));
        data.addPackedAll(ZoneType.Active_Zone, readLongList(buf));
        return data;
    }

    private static void writeLongList(FriendlyByteBuf buf, Collection<Long> values) {
        buf.writeVarInt(values.size());
        for (long value : values) {
            buf.writeLong(value);
        }
    }

    private static List<Long> readLongList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Long> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(buf.readLong());
        }
        return values;
    }

    // ---- 单值视图 ----

    /** 用于实体当前区域判定；底层数据仍允许同一区块同时属于多个 Zone。 */
    public ZoneType getZoneType(BlockPos pos) { return getZoneType(org.galaxy.beyond.api.util.CompatUtil.chunkPos(pos)); }

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
