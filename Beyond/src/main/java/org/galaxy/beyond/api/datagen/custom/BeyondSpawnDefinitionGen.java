package org.galaxy.beyond.api.datagen.custom;

import net.minecraft.data.PackOutput;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;
import org.galaxy.beyond.data.spawn.CassandraSpawnDefinition;

import java.util.List;

/**
 * Beyond 默认刷怪定义生成器。
 */
public class BeyondSpawnDefinitionGen extends SpawnDefinitionProvider {

    public BeyondSpawnDefinitionGen(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerDefinitions(List<Entry> entries) {
        entries.add(entry(CassandraCharacter.ID, new CassandraSpawnDefinition().build()));
    }

    @Override
    public String getName() {
        return "Beyond Spawn Definitions";
    }
}
