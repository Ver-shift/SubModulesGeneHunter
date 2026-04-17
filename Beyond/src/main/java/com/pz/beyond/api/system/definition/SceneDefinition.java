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
@AllArgsConstructor
public class SceneDefinition {


    /**
     * 关卡数据，可能有多个，实际只会抽取一个
     */
    private List<SceneEntry> sceneTypes = new ArrayList<>();
    private SceneType realScene;
    private int priority = 0; //用于调整先后顺，数值越小，放在关卡的越前面

    public static final Codec<SceneDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            SceneEntry.CODEC.listOf().fieldOf("scene_types").forGetter(SceneDefinition::getSceneTypes),
            SceneType.CODEC.optionalFieldOf("real_scene", null).forGetter(SceneDefinition::getRealScene),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(SceneDefinition::getPriority)
        ).apply(instance, SceneDefinition::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SceneDefinition> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, SceneEntry.STREAM_CODEC),
        SceneDefinition::getSceneTypes,
        ByteBufCodecs.optional(SceneType.STREAM_CODEC),
        def -> java.util.Optional.ofNullable(def.getRealScene()),
        ByteBufCodecs.VAR_INT,
        SceneDefinition::getPriority,
        (types, realOpt, priority) -> {
            SceneDefinition def = new SceneDefinition();
            def.setSceneTypes(types);
            def.setRealScene(realOpt.orElse(null));
            def.setPriority(priority);
            return def;
        }
    );

    /**
     * 获取实际的 SceneType
     * 第一次调用时会根据权重随机抽取，之后直接返回缓存的结果
     * @return 选中的 SceneType，如果列表为空返回 null
     */
    public SceneType getRealScene(SingleThreadedRandomSource random) {
        if (realScene == null) {

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
                SceneType.CODEC.fieldOf("scene_type").forGetter(SceneEntry::getSceneType),
                Codec.INT.fieldOf("weight").forGetter(SceneEntry::getWeight)
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
