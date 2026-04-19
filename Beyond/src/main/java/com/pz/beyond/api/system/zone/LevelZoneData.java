package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.init.BeyondZoneInit;
import lombok.Data;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 世界区域数据，挂载在 Level 上
 */
@Data
public class LevelZoneData {

    public static final String ZONE_POS = "zone_pos";
    public static final String ZONE_DATA = "zone_data";

    /**
     * 每个区块只能有一种数据
     */
    private Map<Long, ZoneType> zonePos = new HashMap<>();
    private Map<ZoneType,ZoneData> zoneData = new HashMap<>();

    public LevelZoneData() {
    }



    public ZoneData getZoneData(ChunkPos chunkPos){
        var key = chunkPos.toLong();
        if (zonePos.containsKey(key)){
            var type = zonePos.get(key);
            return getZoneData(type);
        }
        return emptyZoneData();
    }

    public ZoneData getZoneData(BlockPos pos){
        ChunkPos chunkPos = new ChunkPos(pos);
        return getZoneData(chunkPos);
    }

    public ZoneData getZoneData(ZoneType zoneType){
        ZoneType safeZone = zoneType == null ? BeyondZoneInit.EMPTY : zoneType;
        return zoneData.getOrDefault(safeZone, emptyZoneData());
    }

    private static ZoneData emptyZoneData() {
        return new ZoneData(BeyondZoneInit.EMPTY, new ArrayList<>());
    }


    // 工厂方法专用构造
    private LevelZoneData(Map<Long, ZoneType> zonePos, Map<ZoneType, ZoneData> zoneData) {
        this.zonePos = zonePos;
        this.zoneData = zoneData;
    }

    public static final Codec<LevelZoneData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ExtraCodecs.strictUnboundedMap(Codec.LONG, ZoneType.CODEC).fieldOf(ZONE_POS).forGetter(LevelZoneData::getZonePos),
            ExtraCodecs.strictUnboundedMap(ZoneType.CODEC, ZoneData.CODEC).fieldOf(ZONE_DATA).forGetter(LevelZoneData::getZoneData)
    ).apply(builder, LevelZoneData::new));


    public static final StreamCodec<RegistryFriendlyByteBuf, LevelZoneData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.VAR_LONG,
                    ZoneType.STREAM_CODEC
            ),
            LevelZoneData::getZonePos,

            ByteBufCodecs.map(
                    HashMap::new,
                    ZoneType.STREAM_CODEC,
                    ZoneData.STREAM_CODEC
            ),
            LevelZoneData::getZoneData,
            LevelZoneData::new
    );

}