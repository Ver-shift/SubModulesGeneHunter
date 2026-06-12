package org.galaxy.gene_hunter.data.spawn;

import net.minecraft.world.entity.EntityType;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.spawn.SpawnEntry;
import org.galaxy.beyond.api.system.spawn.SpawnPack;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.rogue_event.ZombieGatewayCharacter;

import java.util.List;

public class ZombieExampleSpawnDefinition {

    public static final net.minecraft.resources.ResourceLocation ID = GeneHunter.asResource("zombie_example");

    public SpawnDefinition build() {
        return SpawnDefinition.builder()
                .id(ID)
                .character(ZombieGatewayCharacter.ID)
                .gateway(ID)
                .baseValue(10)
                .valueGrowth(5)
                .maxSpawnCount(8)
                .maxWaveTime(760)
                .setupTime(40)
                .greenValue(100)
                .orangeValue(130)
                .redValue(170)
                .packs(List.of(
                        pack(6, 5, 0, entry(EntityType.ZOMBIE, 3)),
                        pack(9, 3, 8, entry(EntityType.ZOMBIE, 2), entry(EntityType.SKELETON, 1))
                ))
                .build();
    }

    private static SpawnPack pack(int value, int weight, int minValue, SpawnEntry... entries) {
        return SpawnPack.builder()
                .entries(List.of(entries))
                .value(value)
                .weight(weight)
                .minValue(minValue)
                .build();
    }

    private static SpawnEntry entry(EntityType<?> entity, int count) {
        return SpawnEntry.builder()
                .entity(EntityType.getKey(entity))
                .count(count)
                .build();
    }
}
