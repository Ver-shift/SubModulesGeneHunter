package com.pz.beyond.api.system.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.init.BeyondEncounters;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.progress.ProgressData;
import com.pz.beyond.api.system.progress.SceneType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

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
public class ProgressDefinition implements ITranslate<ProgressData, SingleThreadedRandomSource> {

    public static final String IDENTIFIER = "identifier";
    public static final String SCENES = "scenes";
    public static final String ENCOUNTERS = "encounters";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String ENCOUNTER_DEFINITION = "encounter_definition";

    private ResourceLocation identifier = Beyond.asResource("empty");
    private List<SceneDefinition> scenes = new ArrayList<>();

    /**
     * 每个遭遇可能遇到的事件列表
     */
    private List<EncounterMapping> encounters = new ArrayList<>();

    public static final Codec<ProgressDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf(IDENTIFIER).forGetter(ProgressDefinition::getIdentifier),
            SceneDefinition.CODEC.listOf().fieldOf(SCENES).forGetter(ProgressDefinition::getScenes),
            EncounterMapping.CODEC.listOf().fieldOf(ENCOUNTERS).forGetter(ProgressDefinition::getEncounters)
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
     * {@link ITranslate} 实现：将当前 ProgressDefinition 转化为一个新的
     * {@link ProgressData}（已填好 currentProgress / sceneTypes，其他为空默认态）。
     * <p>仅处理关卡静态配置的初始化，具体的 NodeData 等由 chunk加载时再填充。</p>
     */
    @Override
    public ProgressData translate(SingleThreadedRandomSource input) {
        ProgressData pd = new ProgressData();
        pd.setCurrentProgress(this.identifier);
        pd.setIndex(0);

        List<SceneType> rolledScenes = new ArrayList<>();
        for (SceneDefinition sd : this.scenes) {
            if (sd == null) continue;
            SceneType st = sd.translate(input);
            rolledScenes.add(st == null ? SceneType.EMPTY : st);
        }
        pd.setSceneTypes(rolledScenes);
        return pd;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncounterMapping {
        private EncounterType encounterType = BeyondEncounters.EMPTY;
        private EncounterDefinition encounterDefinition = new EncounterDefinition();

        public static final Codec<EncounterMapping> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                EncounterType.CODEC.fieldOf(ENCOUNTER_TYPE).forGetter(EncounterMapping::getEncounterType),
                EncounterDefinition.CODEC.fieldOf(ENCOUNTER_DEFINITION).forGetter(EncounterMapping::getEncounterDefinition)
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
