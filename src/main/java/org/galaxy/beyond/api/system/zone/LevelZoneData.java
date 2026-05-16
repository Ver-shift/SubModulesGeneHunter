package org.galaxy.beyond.api.system.zone;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 只允许从内部方法获取数据
 */
public class LevelZoneData implements IPersistedSerializable {

    public LevelZoneData() {
    }

    // K/V皆为LDLib2 direct类型，直接@Persisted即可
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Persisted
    private Map<ChunkPos, ZoneType> levelZone = new HashMap<>();

    // K为direct类型，V为IPersistedSerializable(read-only)，需final
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    @Persisted
    private final Map<ZoneType, ZoneData> levelZoneData = new HashMap<>();

    // ---- 区块区域类型查询 ----

    public ZoneType getZoneType(ChunkPos pos) {
        return levelZone.get(pos);
    }

    public Set<Map.Entry<ChunkPos, ZoneType>> getZoneEntries() {
        return Collections.unmodifiableSet(levelZone.entrySet());
    }

    public boolean hasZones() {
        return !levelZone.isEmpty();
    }

    // ---- ZoneData 查询 ----

    public ZoneData getZoneData(ChunkPos chunkPos) {
        return levelZoneData.get(levelZone.get(chunkPos));
    }

    public ZoneData getZoneData(BlockPos pos) {
        return getZoneData(ChunkPos.containing(pos));
    }

    public ZoneData getZoneData(ZoneType zoneType) {
        return levelZoneData.get(zoneType);
    }

    // ---- 修改 ----

    public ZoneData getOrCreateZoneData(ZoneType zoneType) {
        return levelZoneData.computeIfAbsent(zoneType, k -> new ZoneData(k));
    }

    public boolean addZone(ChunkPos chunkPos, ZoneType zoneType) {
        if (levelZone.get(chunkPos) == zoneType) return false;
        levelZone.put(chunkPos, zoneType);
        getOrCreateZoneData(zoneType);
        return true;
    }
}
