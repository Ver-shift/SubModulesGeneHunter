package org.galaxy.beyond.api.system.spawn;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.util.random.WeightedEntry;

import java.util.List;

/**
 * 一组可被抽中的生成包，抽中后按条目数量展开。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpawnPack implements IPersistedSerializable {

    @Persisted(subPersisted = true)
    @Builder.Default
    private List<SpawnEntry> entries = List.of();

    @Persisted
    private int value;

    @Persisted
    @Builder.Default
    private int weight = 1;

    /**
     * 最小刷怪点数。
     */
    @Persisted
    private int minValue;

    public int spawnCount() {
        int count = 0;
        for (SpawnEntry entry : entries) {
            count += entry.getCount();
        }
        return count;
    }

    public WeightedEntry.Wrapper<SpawnPack> toWeighted() {
        return WeightedEntry.wrap(this, weight);
    }
}
