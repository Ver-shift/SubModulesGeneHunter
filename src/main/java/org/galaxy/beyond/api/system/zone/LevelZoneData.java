package org.galaxy.beyond.api.system.zone;

import lombok.Data;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

@Data
public class LevelZoneData {

    //二重结构查询：先根据区块查询区域类型，再根据区域类型查询区域数据
    private Map<ChunkPos,ZoneType> levelZone = new HashMap<>();
    private Map<ZoneType,ZoneData> levelZoneData = new HashMap<>();

    public ZoneData getZoneData(ChunkPos chunkPos) {
        return levelZoneData.get(levelZone.get(chunkPos));
    }

    public ZoneData getZoneData(ZoneType zoneType) {
        return levelZoneData.get(zoneType);
    }

    public ZoneData getOrCreateZoneData(ZoneType zoneType) {
        return levelZoneData.computeIfAbsent(zoneType, k -> new ZoneData());
    }

    public void addZone(ChunkPos chunkPos, ZoneType zoneType) {
        levelZone.put(chunkPos, zoneType);
    }

    

}
