package org.galaxy.beyond.api.system.rogue.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import org.galaxy.beyond.api.init.BeyondRogueEventTypeInit;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.Comparator;
import java.util.List;

/**
 * 关卡定义 —— 单个进度阶段的内容表。
 * <p>
 * {@link #sceneRolls} 按 {@link SceneRoll#order} 升序排列，每个 roll
 * 从自身加权条目表中抽取 1 个 {@link SceneType}，组成最终的场景序列。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDefinition implements IPersistedSerializable {

    @DescSynced
    @Persisted(subPersisted = true)
    private List<SceneRoll> sceneRolls;

    @DescSynced
    @Persisted(subPersisted = true)
    private List<Encounter> encounters;

    /** 按 {@code order} 排序后的场景 roll 列表（用于逐序号抽取）。 */
    public List<SceneRoll> sceneRollsOrdered() {
        return sceneRolls.stream()
                .sorted(Comparator.comparingInt(SceneRoll::getOrder))
                .toList();
    }

    // ============================================================
    // SceneRoll —— 单次场景 roll
    // ============================================================

    /**
     * 单次场景 roll —— order 决定触发序号，entries 是加权条目表，从中抽取 1 个 {@link SceneType}。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneRoll {
        /** 触发顺序，小值优先 */
        @DescSynced
    @Persisted
        @Builder.Default
        private int order = 1;

        @DescSynced
    @Persisted(subPersisted = true)
        @Builder.Default
        private List<SceneEntry> entries = List.of();

        public static SceneRoll of(int order, SceneEntry... entries) {
            return SceneRoll.builder().order(order).entries(List.of(entries)).build();
        }

        public WeightedList<SceneType> asWeightedList() {
            return WeightedList.of(entries.stream()
                    .map(SceneEntry::toWeighted)
                    .toList());
        }
    }

    /**
     * 场景条目 —— 权重 + 目标 {@link SceneType}。
     * <p>
     * 开发者为每个条目设定独立 weight，运行时按权抽取。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SceneEntry {
        @DescSynced
    @Persisted
        @Builder.Default
        private int weight = 1;
        @DescSynced
    @Persisted
        private SceneType scene;

        public static SceneEntry of(SceneType scene, int weight) {
            return SceneEntry.builder().scene(scene).weight(weight).build();
        }

        public Weighted<SceneType> toWeighted() {
            return new Weighted<>(scene, weight);
        }
    }

    // ============================================================
    // EventRoll —— 单事件加权条目
    // ============================================================

    /**
     * 事件加权条目 —— 等价于 {@code Weighted<EventTask>}。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventRoll {
        @DescSynced
    @Persisted
        @Builder.Default
        private int weight = 1;
        @DescSynced
    @Persisted(subPersisted = true)
        private EventTask task;

        public static EventRoll of(int weight, EventTask task) {
            return EventRoll.builder().weight(weight).task(task).build();
        }

        public Weighted<EventTask> toWeighted() {
            return new Weighted<>(task, weight);
        }
    }

    // ============================================================
    // Encounter —— 遭遇定义
    // ============================================================

    /**
     * 遭遇定义 —— 按 {@link EncounterType} 分类，挂载一组加权事件。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Encounter {
        @DescSynced
    @Persisted
        private EncounterType type;
        @DescSynced
    @Persisted(subPersisted = true)
        @Builder.Default
        private List<EventRoll> events = List.of();

        public WeightedList<EventTask> eventsAsWeightedList() {
            return WeightedList.of(events.stream().map(EventRoll::toWeighted).toList());
        }
    }

    // ============================================================
    // 编译器验证
    // ============================================================
    public static void test() {
        var def = ProgressDefinition.builder()
                .sceneRolls(List.of(
                        SceneRoll.of(1,
                                SceneEntry.of(SceneType.HARVEST, 5),
                                SceneEntry.of(SceneType.REPOSE, 1)
                        ),
                        SceneRoll.of(2,
                                SceneEntry.of(SceneType.HARVEST, 3),
                                SceneEntry.of(SceneType.CLIMAX, 2)
                        )
                ))
                .encounters(List.of(
                        Encounter.builder()
                                .type(EncounterType.Green_Event)
                                .events(List.of(
                                        EventRoll.of(8, new EventTask(BeyondRogueEventTypeInit.MONSTER,BeyondRogueEventTypeInit.SHOP)),
                                        EventRoll.of(2, new EventTask(List.of()))
                                ))
                                .build()
                ))
                .build();
    }
}
