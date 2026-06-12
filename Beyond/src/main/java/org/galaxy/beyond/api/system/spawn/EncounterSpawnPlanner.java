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
        SpawnBudget budget = director.createBudget(definition, context);
        List<SpawnPack> selected = new ArrayList<>();
        int remainingValue = budget.totalValue();
        int remainingCount = budget.maxSpawnCount();

        for (int attempts = 0; attempts < MAX_ROLL_ATTEMPTS; attempts++) {
            WeightedRandomList<WeightedEntry.Wrapper<SpawnPack>> candidates = candidates(definition, remainingValue, remainingCount);
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

        return new SpawnPlan(budget, selected);
    }

    private WeightedRandomList<WeightedEntry.Wrapper<SpawnPack>> candidates(
            SpawnDefinition definition,
            int remainingValue,
            int remainingCount
    ) {
        List<WeightedEntry.Wrapper<SpawnPack>> candidates = new ArrayList<>();
        for (SpawnPack pack : definition.getPacks()) {
            if (canSelect(pack, remainingValue, remainingCount)) {
                candidates.add(pack.toWeighted());
            }
        }
        return WeightedRandomList.create(candidates);
    }

    private boolean canSelect(SpawnPack pack, int remainingValue, int remainingCount) {
        return pack.getWeight() > 0
                && pack.getValue() > 0
                && pack.getValue() <= remainingValue
                && pack.getMinValue() <= remainingValue
                && pack.spawnCount() > 0
                && pack.spawnCount() <= remainingCount;
    }
}
