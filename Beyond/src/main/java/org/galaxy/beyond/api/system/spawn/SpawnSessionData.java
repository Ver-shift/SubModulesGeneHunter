package org.galaxy.beyond.api.system.spawn;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.node.NodeColor;

import java.util.ArrayList;
import java.util.List;

/**
 * 单次节点启动时计算出的刷怪数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpawnSessionData implements IPersistedSerializable {

    @Persisted
    private ResourceLocation spawnDefinition;

    @Persisted
    private ResourceLocation character;

    @Persisted
    private int stageIndex;

    @Persisted
    @Builder.Default
    private NodeColor nodeColor = NodeColor.EMPTY;

    @Persisted
    private int totalValue;

    @Persisted
    private int maxSpawnCount;

    @Persisted
    private float colorMultiplier;

    @Persisted
    private int waveCount;

    @Persisted
    private boolean boss;

    @Persisted(subPersisted = true)
    @Builder.Default
    private List<SpawnPack> packs = new ArrayList<>();

    public static SpawnSessionData empty() {
        return SpawnSessionData.builder().build();
    }

    public static SpawnSessionData of(ResourceLocation spawnDefinition, ResourceLocation character, SpawnContext context, SpawnPlan plan) {
        SpawnBudget budget = plan.budget();
        return SpawnSessionData.builder()
                .spawnDefinition(spawnDefinition)
                .character(character)
                .stageIndex(context.stageIndex())
                .nodeColor(context.nodeColor())
                .totalValue(budget.totalValue())
                .maxSpawnCount(budget.maxSpawnCount())
                .colorMultiplier(budget.colorMultiplier())
                .waveCount(plan.waveCount())
                .boss(plan.boss())
                .packs(new ArrayList<>(plan.packs()))
                .build();
    }

    public SpawnBudget budget() {
        return new SpawnBudget(totalValue, maxSpawnCount, colorMultiplier);
    }

    public SpawnPlan plan() {
        return new SpawnPlan(budget(), waveCount, packs, boss);
    }
}
