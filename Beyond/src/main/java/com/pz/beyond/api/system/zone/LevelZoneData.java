package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.core.IZonePosManager;
import lombok.Data;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Data
public class LevelZoneData implements IZonePosManager {

    public static final String DIMENSION = "dimension";
    public static final String ZONE_DATA = "zone_data";

    public static final Codec<LevelZoneData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
        ResourceLocation.CODEC.fieldOf(DIMENSION).forGetter(LevelZoneData::getDimensionId),
        ZoneData.CODEC.listOf().optionalFieldOf(ZONE_DATA, List.of()).forGetter(LevelZoneData::getZoneData)
    ).apply(builder, LevelZoneData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LevelZoneData> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        LevelZoneData::getDimensionId,
        ByteBufCodecs.collection(ArrayList::new, ZoneData.STREAM_CODEC),
        LevelZoneData::getZoneData,
        LevelZoneData::new
    );

    private ResourceKey<Level> dimension = Level.OVERWORLD;
    //就这么几种类型，不过度设计了
    private List<ZoneData> zoneData = new ArrayList<>();



    public LevelZoneData() {

    }

    public LevelZoneData(ResourceLocation dimension, List<ZoneData> zoneData) {
        this.dimension = ResourceKey.create(Registries.DIMENSION, dimension);
        this.zoneData = zoneData == null ? new ArrayList<>() : new ArrayList<>(zoneData);
    }

    public ResourceLocation getDimensionId() {
        return dimension.location();
    }


    public void addListener(ResourceLocation zoneId, RuleData ruleData) {

    }


    @Override
    public void addSafeZone(int chunkCountSize) {

    }

    @Override
    public void spawnZone(ChunkPos chunkPos) {

    }

    @Override
    public void addPlayerActiveZone() {

    }

    @Override
    public void addPendingPlayerZone() {

    }
}
