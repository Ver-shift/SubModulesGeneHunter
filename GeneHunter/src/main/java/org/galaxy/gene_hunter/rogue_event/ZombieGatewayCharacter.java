package org.galaxy.gene_hunter.rogue_event;

import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.galaxy.beyond.api.system.rogue.RogueSpawnHelper;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;
import org.galaxy.beyond.api.system.spawn.character.CassandraCharacter;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.gene_hunter.GeneHunter;

import java.util.List;

public class ZombieGatewayCharacter extends CassandraCharacter {
    public static final ResourceLocation ID = GeneHunter.asResource("zombie_gateway");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ItemStack createSpawnToken(SpawnDefinition definition, SpawnPlan plan) {
        Gateway gateway = resolveGateway(definition);
        ItemStack stack = new ItemStack(GatewayObjects.GATE_PEARL);
        GatePearlItem.setGate(stack, gateway);
        return stack;
    }

    @Override
    public GatewayEntity placeSpawn(
            SpawnDefinition definition,
            SpawnPlan plan,
            ServerLevel level,
            BlockPos nodePos,
            List<ServerPlayer> players
    ) {
        if (players.isEmpty()) {
            return null;
        }

        ItemStack pearl = createSpawnToken(definition, plan);
        Gateway gateway = GatePearlItem.getGate(pearl).getOptional()
                .orElseThrow(() -> new IllegalStateException("Unbound gateway pearl: " + definition.getId()));
        GatewayEntity entity = gateway.createEntity(level, players.getFirst());
        entity.setPos(RogueSpawnHelper.gatewayPos(level, nodePos, 3));
        level.addFreshEntity(entity);
        entity.onGateCreated();
        return entity;
    }

    @Override
    public boolean hasActiveSpawn(SpawnDefinition definition, ServerLevel level, BlockPos nodePos) {
        Gateway gateway = resolveGateway(definition);
        double range = gateway.rules().leashRange();
        return !level.getEntitiesOfClass(
                GatewayEntity.class,
                AABB.ofSize(nodePos.getCenter(), range, range, range)
        ).isEmpty();
    }

    private Gateway resolveGateway(SpawnDefinition definition) {
        ResourceLocation gateway = definition.getGateway();
        if (gateway == null) {
            throw new IllegalStateException("Spawn definition missing gateway: " + definition.getId());
        }
        return GatewayRegistry.INSTANCE.holder(gateway).getOptional()
                .orElseThrow(() -> new IllegalStateException("Missing gateway definition: " + gateway));
    }
}
