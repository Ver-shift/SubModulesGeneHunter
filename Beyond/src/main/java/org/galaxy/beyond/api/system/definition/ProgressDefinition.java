package org.galaxy.beyond.api.system.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关卡定义数据。
 * <p>
 * 一个 {@code ProgressDefinition} 描述一次肉鸽进度的场景序列、遭遇表、默认刷怪定义和关卡专属 Cap。
 * 数据通常来自 {@code data/<namespace>/rogue_progress/*.json}，也可以由插件代码注册。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDefinition implements IPersistedSerializable {

    /**
     * 当前关卡默认使用的刷怪定义。
     * <p>
     * 未配置时回落到 {@link CassandraCharacter#ID}，确保旧数据包不需要额外字段也能运行。
     */
    @Persisted
    private ResourceLocation spawnDefinition;

    /**
     * 关卡可用的战利品表。键为 loot type id，值为表 id 列表。
     * <p>
     * 该字段是可选的；未配置时由具体玩法模块提供兼容默认值。
     */
    @Persisted
    @Builder.Default
    private Map<ResourceLocation, List<ResourceLocation>> lootTables = new HashMap<>();

    /**
     * 只在当前关卡初始化时加入的 RogueCap id。
     * <p>
     * 这些 Cap 仍然复用 {@link org.galaxy.beyond.api.system.rogue.RogueCapData} 保存，区别只在来源是关卡定义。
     */
    @Persisted
    @Builder.Default
    private List<ResourceLocation> progressCaps = new ArrayList<>();

    /**
     * 按 order 排序后逐个抽取的场景 roll。
     */
    @Persisted(subPersisted = true)
    @Builder.Default
    private List<SceneRoll> sceneRolls = new ArrayList<>();

    /**
     * 按 {@link EncounterType} 分类的遭遇表。
     */
    @Persisted(subPersisted = true)
    @Builder.Default
    private List<Encounter> encounters = new ArrayList<>();

    public ResourceLocation getSpawnDefinition() {
        return spawnDefinition != null ? spawnDefinition : CassandraCharacter.ID;
    }

    public List<ResourceLocation> getProgressCaps() {
        return progressCaps != null ? progressCaps : List.of();
    }

    public Map<ResourceLocation, List<ResourceLocation>> getLootTables() {
        if (lootTables == null) lootTables = new HashMap<>();
        return lootTables;
    }

    public List<ResourceLocation> getLootTables(ResourceLocation lootType) {
        if (lootType == null) return List.of();
        List<ResourceLocation> ids = getLootTables().get(lootType);
        return ids != null ? ids : List.of();
    }

    /**
     * 返回按 {@link SceneRoll#getOrder()} 升序排列后的场景 roll。
     */
    public List<SceneRoll> sceneRollsOrdered() {
        return sceneRolls.stream()
                .sorted(Comparator.comparingInt(SceneRoll::getOrder))
                .toList();
    }

    /**
     * 单次场景抽取配置。
     * <p>
     * {@code order} 决定它在进度中的位置，{@code entries} 是候选场景的加权表。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneRoll {
        /**
         * 触发顺序，小值优先。
         */
        @Persisted
        @Builder.Default
        private int order = 1;

        /**
         * 当前顺序可抽取的场景条目。
         */
        @Persisted(subPersisted = true)
        @Builder.Default
        private List<SceneEntry> entries = new ArrayList<>();

        public static SceneRoll of(int order, SceneEntry... entries) {
            return SceneRoll.builder().order(order).entries(List.of(entries)).build();
        }

        public WeightedRandomList<WeightedEntry.Wrapper<SceneType>> asWeightedList() {
            return WeightedRandomList.create(entries.stream()
                    .map(SceneEntry::toWeighted)
                    .toList());
        }
    }

    /**
     * 场景候选项。
     * <p>
     * {@code weight} 使用 Minecraft 原版权重工具处理，值越大越容易被抽中。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneEntry {
        @Persisted
        @Builder.Default
        private int weight = 1;

        @Persisted
        private SceneType scene;

        public static SceneEntry of(SceneType scene, int weight) {
            return SceneEntry.builder().scene(scene).weight(weight).build();
        }

        public WeightedEntry.Wrapper<SceneType> toWeighted() {
            return WeightedEntry.wrap(scene, weight);
        }
    }

    /**
     * 遭遇事件候选项。
     * <p>
     * 一个 {@link EventTask} 可以包含多个事件 id，运行时会按顺序执行这些事件。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventRoll {
        @Persisted
        @Builder.Default
        private int weight = 1;

        @Persisted(subPersisted = true)
        private EventTask task;

        public static EventRoll of(int weight, EventTask task) {
            return EventRoll.builder().weight(weight).task(task).build();
        }

        public WeightedEntry.Wrapper<EventTask> toWeighted() {
            return WeightedEntry.wrap(task, weight);
        }
    }

    /**
     * 某一种节点遭遇类型的事件抽取表。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Encounter {
        @Persisted
        private EncounterType type;

        @Persisted(subPersisted = true)
        @Builder.Default
        private List<EventRoll> events = new ArrayList<>();

        public WeightedRandomList<WeightedEntry.Wrapper<EventTask>> eventsAsWeightedList() {
            return WeightedRandomList.create(events.stream().map(EventRoll::toWeighted).toList());
        }
    }
}
