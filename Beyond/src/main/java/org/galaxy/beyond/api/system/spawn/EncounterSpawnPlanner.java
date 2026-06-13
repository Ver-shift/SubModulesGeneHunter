package org.galaxy.beyond.api.system.spawn;

import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.galaxy.beyond.api.system.spawn.character.Character;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * 遭遇刷怪计划生成器。
 */
public class EncounterSpawnPlanner {

    private static final int MAX_ROLL_ATTEMPTS = 32;

    private final Character director;

    public EncounterSpawnPlanner(Character director) {
        this.director = director;
    }

    public SpawnPlan createPlan(SpawnDefinition definition, SpawnContext context) {
        return createPlan(definition, context, false);
    }

    public SpawnPlan createPlan(SpawnDefinition definition, SpawnContext context, boolean boss) {
        if (boss) {
            return createBossPlan(definition, context);
        }

        SpawnBudget budget = director.createBudget(definition, context);
        List<SpawnPack> selected = new ArrayList<>();
        int remainingValue = budget.totalValue();
        int remainingCount = budget.maxSpawnCount();

        for (int attempts = 0; attempts < MAX_ROLL_ATTEMPTS; attempts++) {
            WeightedRandomList<WeightedEntry.Wrapper<SpawnPack>> candidates = candidates(
                    definition,
                    context.stageIndex(),
                    budget.totalValue(),
                    remainingValue,
                    remainingCount
            );
            if (candidates.isEmpty()) break;

            SpawnPack pack = candidates.getRandom(context.random())
                    .map(WeightedEntry.Wrapper::data)
                    .orElse(null);
            if (pack == null) break;

            selected.add(pack);
            remainingValue -= pack.getValue();
            remainingCount -= pack.spawnCount();
            if (remainingValue <= 0 || remainingCount <= 0) break;
        }

        return new SpawnPlan(budget, director.waveCount(definition, context), selected);
    }

    private SpawnPlan createBossPlan(SpawnDefinition definition, SpawnContext context) {
        SpawnBudget budget = new SpawnBudget(0, definition.getMaxSpawnCount(), 1.0F);
        if (definition.getBossPacks().isEmpty()) {
            return new SpawnPlan(budget, 1, List.of(), true);
        }

        int order = director.currentLayer(context);
        var pack = definition.getBossPacks().stream()
                .filter(candidate -> candidate.getOrder() == order)
                .findFirst()
                .orElseGet(() -> definition.getBossPacks().getLast());
        budget = new SpawnBudget(0, Math.max(1, pack.spawnCount()), 1.0F);
        return new SpawnPlan(budget, 1, List.of(pack.asSpawnPack()), true);
    }

    private WeightedRandomList<WeightedEntry.Wrapper<SpawnPack>> candidates(
            SpawnDefinition definition,
            int stageIndex,
            int totalValue,
            int remainingValue,
            int remainingCount
    ) {
        List<WeightedEntry.Wrapper<SpawnPack>> candidates = new ArrayList<>();
        for (SpawnPack pack : definition.getPacks()) {
            if (canSelect(pack, stageIndex, totalValue, remainingValue, remainingCount)) {
                candidates.add(pack.toWeighted());
            }
        }
        return WeightedRandomList.create(candidates);
    }

    private boolean canSelect(SpawnPack pack, int stageIndex, int totalValue, int remainingValue, int remainingCount) {
        return pack.getWeight() > 0
                && pack.getValue() > 0
                && pack.getValue() <= remainingValue
                && pack.getMinValue() <= totalValue
                && pack.getMaxValue() >= totalValue
                && pack.getMinStageIndex() <= stageIndex
                && pack.getMaxStageIndex() >= stageIndex
                && pack.spawnCount() > 0
                && pack.spawnCount() <= remainingCount;
    }
}
