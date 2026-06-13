package org.galaxy.gene_hunter.data.spawn;

import net.minecraft.world.entity.EntityType;
import org.galaxy.beyond.api.system.spawn.BossSpawnEntry;

public final class GeneHunterBossEntries {

    private GeneHunterBossEntries() {
    }

    public static BossSpawnEntry ironGolem(float health, double attackDamage, double armor) {
        return BossSpawnEntry.builder()
                .entity(EntityType.getKey(EntityType.IRON_GOLEM))
                .count(1)
                .health(health)
                .attackDamage(attackDamage)
                .armor(armor)
                .build();
    }
}
