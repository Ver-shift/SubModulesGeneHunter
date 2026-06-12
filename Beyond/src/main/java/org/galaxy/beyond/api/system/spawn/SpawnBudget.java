package org.galaxy.beyond.api.system.spawn;

/**
 * 角色公式计算出的本次刷怪预算。
 */
public record SpawnBudget(int totalValue, int maxSpawnCount, int colorMultiplier) {

    public SpawnBudget {
        totalValue = Math.max(0, totalValue);
        maxSpawnCount = Math.max(0, maxSpawnCount);
        colorMultiplier = Math.max(0, colorMultiplier);
    }
}
