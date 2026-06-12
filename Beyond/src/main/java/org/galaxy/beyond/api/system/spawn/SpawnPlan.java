package org.galaxy.beyond.api.system.spawn;

import java.util.ArrayList;
import java.util.List;

/**
 * 刷怪计算结果，只描述要生成什么，不负责生成实体。
 */
public record SpawnPlan(SpawnBudget budget, List<SpawnPack> packs) {

    public SpawnPlan {
        packs = packs == null ? List.of() : List.copyOf(packs);
    }

    public int usedValue() {
        int value = 0;
        for (SpawnPack pack : packs) {
            value += pack.getValue();
        }
        return value;
    }

    public int spawnCount() {
        int count = 0;
        for (SpawnPack pack : packs) {
            count += pack.spawnCount();
        }
        return count;
    }

    public List<SpawnEntry> entries() {
        List<SpawnEntry> entries = new ArrayList<>();
        for (SpawnPack pack : packs) {
            entries.addAll(pack.getEntries());
        }
        return List.copyOf(entries);
    }
}
