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
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 每种遭遇可能刷新的任务列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncounterDefinition {

    public static final String EVENT_TASKS = "event_tasks";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String EVENT_TASK = "event_task";
    public static final String WEIGHT = "weight";

    /**
     * 普通的任务列表
     */
    private List<Entry> eventTasks = new ArrayList<>();


    public static final Codec<EncounterDefinition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Entry.CODEC.listOf().fieldOf(EVENT_TASKS).forGetter(EncounterDefinition::getEventTasks)
        ).apply(instance, EncounterDefinition::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EncounterDefinition> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(ArrayList::new, Entry.STREAM_CODEC),
        EncounterDefinition::getEventTasks,
        EncounterDefinition::new
    );



    /**
     * 根据权重随机抽取一个 EventTask
     * @param randomSource 随机源
     * @return 选中的 EventTask，如果列表为空返回 null
     */
    public EventTask getEventTask(SingleThreadedRandomSource randomSource) {
        if (eventTasks == null || eventTasks.isEmpty()) {
            return null;
        }

        // 计算总权重
        int totalWeight = eventTasks.stream().mapToInt(Entry::getWeight).sum();
        if (totalWeight <= 0) {
            return null;
        }

        // 加权随机选择
        int roll = randomSource.nextInt(totalWeight);
        int cumulative = 0;
        for (Entry entry : eventTasks) {
            cumulative += entry.getWeight();
            if (roll < cumulative) {
                return entry.getEventTask();
            }
        }

        // 默认返回最后一个（防止浮点误差）
        return eventTasks.get(eventTasks.size() - 1).getEventTask();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private EventTask eventTask;
        private int weight;

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                EventTask.CODEC.fieldOf(EVENT_TASK).forGetter(Entry::getEventTask),
                Codec.INT.fieldOf(WEIGHT).forGetter(Entry::getWeight)
            ).apply(instance, Entry::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
            EventTask.STREAM_CODEC,
            Entry::getEventTask,
            ByteBufCodecs.VAR_INT,
            Entry::getWeight,
            Entry::new
        );
    }
}
