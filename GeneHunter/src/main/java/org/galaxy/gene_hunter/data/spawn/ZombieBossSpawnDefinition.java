package org.galaxy.gene_hunter.data.spawn;

import net.minecraft.world.entity.EntityType;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.spawn.SpawnEntry;
import org.galaxy.beyond.api.system.spawn.SpawnPack;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.rogue_event.ZombieGatewayCharacter;

import java.util.List;

public class ZombieBossSpawnDefinition {

    public static final net.minecraft.resources.ResourceLocation ID = GeneHunter.asResource("zombie_boss");

    public SpawnDefinition build() {
        return SpawnDefinition.builder()
                .id(ID)
                .character(ZombieGatewayCharacter.ID)
                .gateway(ID)
                .baseValue(20)
                .valueGrowth(8)
                .maxSpawnCount(4)
                .maxWaveTime(4800)
                .setupTime(100)
                .greenValue(100)
                .orangeValue(130)
                .redValue(170)
                .packs(List.of(SpawnPack.builder()
                        .entries(List.of(SpawnEntry.builder()
                                .entity(EntityType.getKey(EntityType.ZOMBIE))
                                .count(1)
                                .build()))
                        .value(20)
                        .weight(1)
                        .minValue(0)
                        .build()))
                .build();
    }
}
