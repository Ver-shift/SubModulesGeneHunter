package org.galaxy.beyond.api.system.spawn;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Boss 怪物团，不参与普通刷怪预算和随机抽包。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossSpawnPack implements IPersistedSerializable {

    /**
     * 出场顺序。第一个 CLIMAX 使用 1，第二个使用 2，以此类推。
     */
    @Persisted
    @Builder.Default
    private int order = 1;

    @Persisted(subPersisted = true)
    @Builder.Default
    private List<BossSpawnEntry> entries = new ArrayList<>();

    public int spawnCount() {
        int count = 0;
        for (BossSpawnEntry entry : entries) {
            count += entry.getCount();
        }
        return count;
    }

    public SpawnPack asSpawnPack() {
        List<SpawnEntry> spawnEntries = new ArrayList<>();
        for (BossSpawnEntry entry : entries) {
            spawnEntries.add(SpawnEntry.builder()
                    .entity(entry.getEntity())
                    .count(entry.getCount())
                    .health(entry.getHealth())
                    .attackDamage(entry.getAttackDamage())
                    .armor(entry.getArmor())
                    .fixedAttributes(true)
                    .build());
        }
        return SpawnPack.builder()
                .entries(spawnEntries)
                .value(0)
                .weight(1)
                .build();
    }
}
