package org.galaxy.gene_hunter.datagen;

import net.minecraft.data.PackOutput;
import org.galaxy.beyond.api.datagen.custom.SpawnDefinitionProvider;
import org.galaxy.gene_hunter.data.spawn.ZombieBossSpawnDefinition;
import org.galaxy.gene_hunter.data.spawn.ZombieExampleSpawnDefinition;

import java.util.List;

public class GeneHunterSpawnDefinitionGen extends SpawnDefinitionProvider {

    public GeneHunterSpawnDefinitionGen(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerDefinitions(List<Entry> entries) {
        entries.add(entry(ZombieExampleSpawnDefinition.ID, new ZombieExampleSpawnDefinition().build()));
        entries.add(entry(ZombieBossSpawnDefinition.ID, new ZombieBossSpawnDefinition().build()));
    }

    @Override
    public String getName() {
        return "GeneHunter Spawn Definitions";
    }
}
