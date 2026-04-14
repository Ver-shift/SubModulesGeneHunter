package com.pz.beyond.api.system.progress;

import com.pz.beyond.api.init.BeyondNodeEventTypes;
import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.node.NodeEventType;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 进度类型定义，包含多个 Scene 节点和事件表
 */
public class ProgressType {

    private final ResourceLocation id;
    private final List<SceneEntry> scenes;
    private final Map<EncounterType, List<EventMapping>> encounterEvents;

    private ProgressType(ResourceLocation id, List<SceneEntry> scenes,
                         Map<EncounterType, List<EventMapping>> encounterEvents) {
        this.id = id;
        this.scenes = scenes;
        this.encounterEvents = encounterEvents;
    }

    /**
     * 创建新的 ProgressType
     */
    public static ProgressType create(ResourceLocation id, Consumer<Builder> consumer) {
        Builder builder = new Builder(id);
        consumer.accept(builder);
        return builder.build();
    }

    /**
     * 根据权重抽取事件列表
     * @param encounterType 遭遇类型
     * @param random 随机源
     */
    public List<NodeEventType> roll(EncounterType encounterType, Random random) {
        List<EventMapping> mappings = encounterEvents.get(encounterType);
        if (mappings == null || mappings.isEmpty()) {
            return List.of(BeyondNodeEventTypes.EMPTY);
        }

        int totalWeight = mappings.stream().mapToInt(EventMapping::weight).sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (EventMapping mapping : mappings) {
            cumulative += mapping.weight();
            if (roll < cumulative) {
                return mapping.events();
            }
        }
        return List.of(BeyondNodeEventTypes.EMPTY);
    }

    public ResourceLocation getId() {
        return id;
    }

    public List<SceneEntry> getScenes() {
        return scenes;
    }

    public Map<EncounterType, List<EventMapping>> getEncounterEvents() {
        return encounterEvents;
    }

    /**
     * 场景节点
     */
    public record SceneEntry(int displayWeight, SceneType sceneType) {
    }

    /**
     * 事件映射，权重 + 事件列表
     */
    public record EventMapping(int weight, List<NodeEventType> events) {
    }

    /**
     * 构建器
     */
    public static class Builder {
        private final ResourceLocation id;
        private final List<SceneEntry> scenes = new ArrayList<>();
        private final Map<EncounterType, List<EventMapping>> encounterEvents = new HashMap<>();

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        /**
         * 添加场景节点
         *
         * @param displayWeight 显示权重（越小越排在前面）
         * @param sceneType     场景类型
         */
        public Builder scene(int displayWeight, SceneType sceneType) {
            scenes.add(new SceneEntry(displayWeight, sceneType));
            return this;
        }

        /**
         * 为指定遭遇类型添加单个事件
         */
        public Builder add(EncounterType encounterType, int weight, Supplier<NodeEventType> event) {
            encounterEvents.computeIfAbsent(encounterType, k -> new ArrayList<>())
                    .add(new EventMapping(weight, List.of(event.get())));
            return this;
        }

        /**
         * 为指定遭遇类型添加事件列表（依次触发）
         */
        public Builder addList(EncounterType encounterType, int weight, List<Supplier<NodeEventType>> events) {
            List<NodeEventType> resolved = events.stream()
                    .map(Supplier::get).toList();
            encounterEvents.computeIfAbsent(encounterType, k -> new ArrayList<>())
                    .add(new EventMapping(weight, resolved));
            return this;
        }

        /**
         * 构建 ProgressType
         */
        public ProgressType build() {
            scenes.sort(Comparator.comparingInt(SceneEntry::displayWeight));
            Map<EncounterType, List<EventMapping>> immutableEvents = new HashMap<>();
            encounterEvents.forEach((k, v) -> immutableEvents.put(k, List.copyOf(v)));
            return new ProgressType(id, List.copyOf(scenes), Map.copyOf(immutableEvents));
        }
    }
}
