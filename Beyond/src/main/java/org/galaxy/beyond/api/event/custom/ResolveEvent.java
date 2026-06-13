package org.galaxy.beyond.api.event.custom;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.definition.ProgressDefinition;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.List;
import java.util.Map;

/**
 * Definition 解析事件基类。
 * <p>
 * Beyond 的数据包定义不会直接进入运行逻辑，而是先经过 DefinitionManager 解析。
 * 每个解析点都会发布对应的 ResolveEvent，外部模组可以读取 {@link #getFrom()} 的原始定义，
 * 再通过 {@link #setTo(Object)} 替换最终运行值。
 * <p>
 * 这些事件发布在 NeoForge EVENT_BUS 上，通常发生在服务端线程。监听者应避免长时间阻塞，
 * 并且不要直接修改 from 对象，除非内部事件的 JavaDoc 明确允许。
 *
 * @param <T> 原始定义或解析上下文类型
 * @param <V> 最终运行值类型
 */
public abstract class ResolveEvent<T, V> extends LevelEvent {
    @Getter
    private final T from;
    @Getter
    @Setter
    private V to;

    public ResolveEvent(LevelAccessor level, T from, V to) {
        super(level);
        this.from = from;
        this.to = to;
    }

    /**
     * 关卡遭遇事件任务解析事件。
     * <p>
     * 触发时机：DefinitionManager 从当前 {@link ProgressDefinition.Encounter} 的加权事件表中抽出一个
     * {@link EventTask} 后、事件链真正执行前。
     * <p>
     * 使用方式：监听者可以读取 {@link #getFrom()} 得到当前遭遇定义，并用 {@link #setTo(EventTask)}
     * 替换最终事件任务。例如根据玩家人数把单个怪物事件替换成多个事件，或插入奖励事件。
     */
    public static class ResolveEventTaskEvent extends ResolveEvent<ProgressDefinition.Encounter, EventTask> {
        public ResolveEventTaskEvent(LevelAccessor level, ProgressDefinition.Encounter from, EventTask to) {
            super(level, from, to);
        }
    }

    /**
     * 关卡场景序列解析事件。
     * <p>
     * 触发时机：DefinitionManager 已经按 {@link ProgressDefinition.SceneRoll#getOrder()} 排序并完成场景抽取后。
     * <p>
     * from 是参与抽取的 SceneRoll 列表，to 是最终场景序列。监听者可以替换 to，
     * 例如强制追加一个 Boss 场景、移除休息场景，或根据世界难度调整路线。
     */
    public static class ResolveSceneEvent extends ResolveEvent<List<ProgressDefinition.SceneRoll>, List<SceneType>> {
        public ResolveSceneEvent(LevelAccessor level, List<ProgressDefinition.SceneRoll> from, List<SceneType> to) {
            super(level, from, to);
        }
    }

    /**
     * 当前关卡默认刷怪定义解析事件。
     * <p>
     * 触发时机：刷怪事件需要知道当前关卡使用哪个 {@link SpawnDefinition} 前。
     * <p>
     * from 是当前关卡定义，to 是刷怪定义 id。监听者可以按节点颜色、玩家数量、
     * 自定义难度等条件替换 to，从而让同一个关卡动态切换不同刷怪表。
     */
    public static class ResolveSpawnDefinitionEvent extends ResolveEvent<ProgressDefinition, ResourceLocation> {
        public ResolveSpawnDefinitionEvent(LevelAccessor level, ProgressDefinition from, ResourceLocation to) {
            super(level, from, to);
        }
    }

    /**
     * 当前关卡专属 Cap 解析事件。
     * <p>
     * 触发时机：RogueData 初始化默认 Cap 后，准备追加当前关卡专属 Cap 前。
     * <p>
     * from 是当前关卡定义，to 是将被追加的 RogueCap id 列表。监听者可以替换列表，
     * 让特定关卡启用额外规则。注意这些 Cap 必须已经注册到 RogueCap 注册表。
     */
    public static class ResolveProgressCapsEvent extends ResolveEvent<ProgressDefinition, List<ResourceLocation>> {
        public ResolveProgressCapsEvent(LevelAccessor level, ProgressDefinition from, List<ResourceLocation> to) {
            super(level, from, to);
        }
    }

    /**
     * 进度定义集合应用事件。
     * <p>
     * 触发时机：ProgressDataPack 合并插件默认定义和数据包定义后、写入 BeyondGlobalData 前。
     * <p>
     * 监听者可以替换 to，用来插入、删除或覆盖关卡定义。建议复制 Map 后再修改，
     * 避免依赖调用方内部集合实现。
     */
    public static class ResolveProgressDefinitionsEvent extends ResolveEvent<Map<ResourceLocation, ProgressDefinition>, Map<ResourceLocation, ProgressDefinition>> {
        public ResolveProgressDefinitionsEvent(LevelAccessor level, Map<ResourceLocation, ProgressDefinition> from, Map<ResourceLocation, ProgressDefinition> to) {
            super(level, from, to);
        }
    }

    /**
     * 刷怪定义集合应用事件。
     * <p>
     * 触发时机：SpawnDataPack 读取所有 data/&lt;namespace&gt;/spawn_definitions/*.json 后、
     * 写入 RogueDefinition 前。
     * <p>
     * 监听者可以替换 to，用来注入代码生成的刷怪定义，或者按服务器规则禁用某些刷怪表。
     */
    public static class ResolveSpawnDefinitionsEvent extends ResolveEvent<Map<ResourceLocation, SpawnDefinition>, Map<ResourceLocation, SpawnDefinition>> {
        public ResolveSpawnDefinitionsEvent(LevelAccessor level, Map<ResourceLocation, SpawnDefinition> from, Map<ResourceLocation, SpawnDefinition> to) {
            super(level, from, to);
        }
    }
}
