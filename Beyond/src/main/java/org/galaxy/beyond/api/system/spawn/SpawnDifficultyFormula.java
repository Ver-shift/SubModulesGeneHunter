package org.galaxy.beyond.api.system.spawn;

/**
 * 关卡刷怪难度公式。
 */
public class SpawnDifficultyFormula {

    public SpawnBudget createBudget(
            SpawnDifficultySettings settings,
            int stageOrder,
            int layer,
            int baseValue,
            int maxSpawnCount,
            float colorMultiplier
    ) {
        float value = value(settings, stageOrder, layer);
        int totalValue = Math.round(baseValue * value * colorMultiplier);
        return new SpawnBudget(totalValue, maxSpawnCount, colorMultiplier);
    }

    public float value(SpawnDifficultySettings settings, int stageOrder, int layer) {
        stageOrder = Math.max(1, stageOrder);
        layer = Math.max(1, layer);
        double stageDifficulty = Math.pow(1.0D + settings.stageGrowthRate(), stageOrder - 1);
        double layerDifficulty = 1.0D + settings.layerDifficultyMultiplier() * (layer - 1);
        return (float) (stageDifficulty * layerDifficulty);
    }
}
