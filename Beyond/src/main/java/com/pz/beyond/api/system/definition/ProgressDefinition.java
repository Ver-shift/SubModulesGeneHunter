package com.pz.beyond.api.system.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.node.EncounterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 关卡的数据层，通过数据包进行控制
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDefinition {

    private ResourceLocation identifier;
    private List<SceneDefinition> scenes = new ArrayList<>();

    /**
     * 每个遭遇可能遇到的事件列表
     */
    private List<EncounterMapping> encounters = new ArrayList<>();

    public static final Codec<ProgressDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("identifier").forGetter(ProgressDefinition::getIdentifier),
            SceneDefinition.CODEC.listOf().fieldOf("scenes").forGetter(ProgressDefinition::getScenes),
            EncounterMapping.CODEC.listOf().fieldOf("encounters").forGetter(ProgressDefinition::getEncounters)
        ).apply(instance, ProgressDefinition::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProgressDefinition> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC,
        ProgressDefinition::getIdentifier,
        ByteBufCodecs.collection(ArrayList::new, SceneDefinition.STREAM_CODEC),
        ProgressDefinition::getScenes,
        ByteBufCodecs.collection(ArrayList::new, EncounterMapping.STREAM_CODEC),
        ProgressDefinition::getEncounters,
        ProgressDefinition::new
    );

    /**
     * 获取 encounters 作为 Map（便利方法）
     */
    public Map<EncounterType, EncounterDefinition> getEncountersAsMap() {
        Map<EncounterType, EncounterDefinition> map = new HashMap<>();
        for (EncounterMapping mapping : encounters) {
            map.put(mapping.getEncounterType(), mapping.getEncounterDefinition());
        }
        return map;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncounterMapping {
        private EncounterType encounterType;
        private EncounterDefinition encounterDefinition;

        public static final Codec<EncounterMapping> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                EncounterType.CODEC.fieldOf("encounter_type").forGetter(EncounterMapping::getEncounterType),
                EncounterDefinition.CODEC.fieldOf("encounter_definition").forGetter(EncounterMapping::getEncounterDefinition)
            ).apply(instance, EncounterMapping::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, EncounterMapping> STREAM_CODEC = StreamCodec.composite(
            EncounterType.STREAM_CODEC,
            EncounterMapping::getEncounterType,
            EncounterDefinition.STREAM_CODEC,
            EncounterMapping::getEncounterDefinition,
            EncounterMapping::new
        );
    }

}
