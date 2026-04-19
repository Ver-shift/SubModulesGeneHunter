package com.pz.beyond.api.system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.definition.ProgressDefinition;
import com.pz.beyond.api.system.structure.StructureData;
import com.pz.beyond.api.system.zone.LevelZoneData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 只存储到主世界的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeyondLevelData {

    public static final String DIMENSION = "dimension";
    public static final String PROGRESS_DEFINITIONS = "progress_definitions";
    public static final String LEVEL_ZONE_DATA = "level_zone_data";
    public static final String STRUCTURE_DATA = "structure_data";

    private ResourceKey<Level> dimension = Level.OVERWORLD;

    //关卡数据
    private Map<ResourceLocation, ProgressDefinition> progressDefinitions = new HashMap<>();
    //区域数据
    private LevelZoneData levelZoneData = new LevelZoneData();
    //todo 运行时关卡数据

    //结构数据
    private StructureData structureData = new StructureData();






    public static final Codec<BeyondLevelData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf(DIMENSION).forGetter(BeyondLevelData::getDimension),
            Codec.unboundedMap(ResourceLocation.CODEC, ProgressDefinition.CODEC).fieldOf(PROGRESS_DEFINITIONS).forGetter(BeyondLevelData::getProgressDefinitions),
            LevelZoneData.CODEC.fieldOf(LEVEL_ZONE_DATA).forGetter(BeyondLevelData::getLevelZoneData),
            StructureData.CODEC.fieldOf(STRUCTURE_DATA).forGetter(BeyondLevelData::getStructureData)
        ).apply(instance, BeyondLevelData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BeyondLevelData> STREAM_CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION),
        BeyondLevelData::getDimension,
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ProgressDefinition.STREAM_CODEC),
        BeyondLevelData::getProgressDefinitions,
        LevelZoneData.STREAM_CODEC,
        BeyondLevelData::getLevelZoneData,
        StructureData.STREAM_CODEC,
        BeyondLevelData::getStructureData,
        BeyondLevelData::new
    );

}
