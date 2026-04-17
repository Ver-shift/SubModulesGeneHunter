package com.pz.beyond.api.system.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pz.beyond.api.system.progress.SceneType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 进度的数据层定义。可能有多个scene，通过
 */
@Data
@NoArgsConstructor
public class SceneDefinition {

    public static final String SCENE_TYPES = "scene_types";
    public static final String REAL_SCENE = "real_scene";
    public static final String PRIORITY = "priority";
    public static final String SCENE_TYPE = "scene_type";
    public static final String WEIGHT = "weight";

    /**
     * 关卡数据，可能有多个，实际只会抽取一个
     */
    private List<SceneEntry> sceneTypes = new ArrayList<>();
    private SceneType realScene = SceneType.EMPTY;
    private int priority = 0; //用于调整先后顺，数值越小，放在关卡的越前面

    public static final Codec<SceneDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            SceneEntry.CODEC.listOf().fieldOf(SCENE_TYPES).forGetter(SceneDefinition::getSceneTypes),
            SceneType.CODEC.optionalFieldOf(REAL_SCENE, SceneType.EMPTY).forGetter(SceneDefinition::getRealScene),
            Codec.INT.optionalFieldOf(PRIORITY, 0).forGetter(SceneDefinition::getPriority)
        ).apply(instance, SceneDefinition::new)
    );

    // 工厂方法专用构造
    private SceneDefinition(List<SceneEntry> sceneTypes, SceneType realScene, int priority) {
        this.sceneTypes = sceneTypes;
        this.realScene = realScene;
        this.priority = priority;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SceneDefinition> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, SceneEntry.STREAM_CODEC),
        SceneDefinition::getSceneTypes,
        SceneType.STREAM_CODEC,
        SceneDefinition::getRealScene,
        ByteBufCodecs.VAR_INT,
        SceneDefinition::getPriority,
        SceneDefinition::new
    );

    /**
     * 获取实际的 SceneType
     * 第一次调用时会根据权重随机抽取，之后直接返回缓存的结果
     * @return 选中的 SceneType，如果列表为空返回 null
     */
    public SceneType getRealScene(SingleThreadedRandomSource random) {
        if (realScene == SceneType.EMPTY) {
            realScene = roll(random);
        }
        return realScene;
    }



    /**
     * 根据权重随机抽取一个 SceneType
     * @param random 随机源
     * @return 选中的 SceneType，如果列表为空返回 null
     */
    public SceneType roll(SingleThreadedRandomSource random){
        if (sceneTypes == null || sceneTypes.isEmpty()) {
            return null;
        }

        // 计算总权重
        int totalWeight = sceneTypes.stream().mapToInt(SceneEntry::getWeight).sum();
        if (totalWeight <= 0) {
            return null;
        }

        // 加权随机选择
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (SceneEntry entry : sceneTypes) {
            cumulative += entry.getWeight();
            if (roll < cumulative) {
                return entry.getSceneType();
            }
        }

        // 默认返回最后一个（防止浮点误差）
        return sceneTypes.get(sceneTypes.size() - 1).getSceneType();
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneEntry {

        private SceneType sceneType;
        private int weight;

        public static final Codec<SceneEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                SceneType.CODEC.fieldOf(SCENE_TYPE).forGetter(SceneEntry::getSceneType),
                Codec.INT.fieldOf(WEIGHT).forGetter(SceneEntry::getWeight)
            ).apply(instance, SceneEntry::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, SceneEntry> STREAM_CODEC = StreamCodec.composite(
            SceneType.STREAM_CODEC,
            SceneEntry::getSceneType,
            ByteBufCodecs.VAR_INT,
            SceneEntry::getWeight,
            SceneEntry::new
        );

    }
}
