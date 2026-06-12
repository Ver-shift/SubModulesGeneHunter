package org.galaxy.beyond.api.system.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.spawn.SpawnPack;

import java.util.List;

/**
 * 刷怪定义数据。
 * <p>
 * 该定义只描述刷怪预算、候选怪物包和要使用的 Character，不绑定具体实现模组。
 * 具体如何生成传送门、实体或其它刷怪载体，由 Character 子类决定。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpawnDefinition implements IPersistedSerializable {

    /**
     * 定义 id，通常来自数据包文件路径。
     */
    @Persisted
    private ResourceLocation id;

    /**
     * 使用的刷怪角色 id。为空时由 SpawnDefinitionManager 回落到 Cassandra。
     */
    @Persisted
    private ResourceLocation character;

    /**
     * 可选外部载体 id，例如 GeneHunter 中由 GatewayCharacter 解释为 gateway id。
     */
    @Persisted
    private ResourceLocation gateway;

    /**
     * 可被抽取的怪物包。
     */
    @Persisted(subPersisted = true)
    @Builder.Default
    private List<SpawnPack> packs = List.of();

    /**
     * 初始预算值。
     */
    @Persisted
    @Builder.Default
    private int baseValue = 10;

    /**
     * 每个关卡 index 增加的预算值。
     */
    @Persisted
    @Builder.Default
    private int valueGrowth = 5;

    /**
     * 单次计划最多刷出的怪物数量。
     */
    @Persisted
    @Builder.Default
    private int maxSpawnCount = 8;

    /**
     * 最大波次时间，单位 tick。
     */
    @Persisted
    @Builder.Default
    private int maxWaveTime = 760;

    /**
     * 开始刷怪前的准备时间，单位 tick。
     */
    @Persisted
    @Builder.Default
    private int setupTime = 40;

    /**
     * 绿色节点预算倍率，100 表示 1 倍。
     */
    @Persisted
    @Builder.Default
    private int greenValue = 100;

    /**
     * 橙色节点预算倍率，100 表示 1 倍。
     */
    @Persisted
    @Builder.Default
    private int orangeValue = 130;

    /**
     * 红色节点预算倍率，100 表示 1 倍。
     */
    @Persisted
    @Builder.Default
    private int redValue = 170;
}
