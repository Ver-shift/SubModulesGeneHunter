package org.galaxy.beyond.api.system.spawn;

/**
 * 刷怪难度公式参数，由 Character 持有。
 */
public record SpawnDifficultySettings(double stageGrowthRate, double layerDifficultyMultiplier) {

    public static final SpawnDifficultySettings DEFAULT = new SpawnDifficultySettings(
            0.1D,
            0.1D
    );

    public SpawnDifficultySettings {
        stageGrowthRate = Math.max(0.0D, stageGrowthRate);
        layerDifficultyMultiplier = Math.max(0.0D, layerDifficultyMultiplier);
    }
}
