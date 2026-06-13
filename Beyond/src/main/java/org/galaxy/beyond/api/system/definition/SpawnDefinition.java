package org.galaxy.beyond.api.system.definition;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.spawn.BossSpawnPack;
import org.galaxy.beyond.api.system.spawn.SpawnPack;

import java.util.ArrayList;
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
     * 使用的刷怪角色 id。为空时由 DefinitionManager 回落到 Cassandra。
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
    private List<SpawnPack> packs = new ArrayList<>();

    /**
     * Boss 怪物团。它独立于普通刷怪包，不参与普通预算、随机抽包和颜色倍率。
     */
    @Persisted(subPersisted = true)
    @Builder.Default
    private List<BossSpawnPack> bossPacks = new ArrayList<>();

    /**
     * 最基准的刷怪点数，Character 会在此基础上做动态倍率计算。
     */
    @Persisted
    @Builder.Default
    private int baseValue = 100;

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

}
