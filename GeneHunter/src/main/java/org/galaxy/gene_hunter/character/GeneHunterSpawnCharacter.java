package org.galaxy.gene_hunter.character;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.system.gateway.GatewaySpawnPlacer;
import org.galaxy.gene_hunter.api.system.gateway.GatewaySpawnTokenFactory;

import java.util.List;

public class GeneHunterSpawnCharacter extends CassandraCharacter {
    public static final ResourceLocation ID = GeneHunter.asResource("gene_hunter_spawn");
    private final GatewaySpawnTokenFactory tokenFactory = new GatewaySpawnTokenFactory();
    private final GatewaySpawnPlacer placer = new GatewaySpawnPlacer();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ItemStack createSpawnToken(SpawnDefinition definition, SpawnPlan plan) {
        return tokenFactory.create(definition, plan);
    }

    @Override
    public GatewayEntity placeSpawn(
            SpawnDefinition definition,
            SpawnPlan plan,
            ServerLevel level,
            BlockPos nodePos,
            List<ServerPlayer> players
    ) {
        return placer.place(definition, plan, level, nodePos, players);
    }

    @Override
    public boolean hasActiveSpawn(SpawnDefinition definition, ServerLevel level, BlockPos nodePos) {
        return placer.hasActive(level, nodePos);
    }
}
