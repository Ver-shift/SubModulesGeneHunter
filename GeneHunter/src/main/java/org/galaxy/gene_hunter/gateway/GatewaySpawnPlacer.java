package org.galaxy.gene_hunter.gateway;

import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.entity.NormalGatewayEntity;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.rogue.RogueSpawnHelper;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;

import java.util.List;

public class GatewaySpawnPlacer {

    public static final String SPAWN_TAG = "gene_hunter_spawn_event";
    public static final String BOSS_TAG = "gene_hunter_boss_event";

    private final GeneHunterGatewayFactory gatewayFactory = new GeneHunterGatewayFactory();

    public GatewayEntity place(
            SpawnDefinition definition,
            SpawnPlan plan,
            ServerLevel level,
            BlockPos nodePos,
            List<ServerPlayer> players
    ) {
        if (players.isEmpty()) {
            return null;
        }

        DynamicHolder<Gateway> gateway = createGatewayHolder(definition, plan, players.getFirst().serverLevel());
        GatewayEntity entity = new NormalGatewayEntity(level, players.getFirst(), gateway);
        entity.addTag(SPAWN_TAG);
        if (plan.boss()) entity.addTag(BOSS_TAG);
        entity.setPos(RogueSpawnHelper.gatewayPos(level, nodePos, 3));
        level.addFreshEntity(entity);
        entity.onGateCreated();
        return entity;
    }

    public boolean hasActive(ServerLevel level, BlockPos nodePos) {
        return !level.getEntitiesOfClass(
                GatewayEntity.class,
                AABB.ofSize(nodePos.getCenter(), 96.0D, 96.0D, 96.0D),
                entity -> entity.getTags().contains(SPAWN_TAG)
        ).isEmpty();
    }

    private DynamicHolder<Gateway> createGatewayHolder(SpawnDefinition definition, SpawnPlan plan, ServerLevel level) {
        Gateway dynamicGateway = gatewayFactory.create(definition, plan);
        ResourceLocation id = GeneHunterGatewayBinder.runtimeId(definition.getId(), dynamicGateway);
        return GeneHunterGatewayBinder.bind(id, dynamicGateway, level);
    }
}
