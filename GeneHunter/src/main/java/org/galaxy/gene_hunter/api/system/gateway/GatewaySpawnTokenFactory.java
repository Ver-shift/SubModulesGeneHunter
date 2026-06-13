package org.galaxy.gene_hunter.api.system.gateway;

import dev.shadowsoffire.gateways.GatewayObjects;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.item.GatePearlItem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.galaxy.beyond.api.system.spawn.SpawnPlan;

public class GatewaySpawnTokenFactory {

    private final GeneHunterGatewayFactory gatewayFactory = new GeneHunterGatewayFactory();

    public ItemStack create(SpawnDefinition definition, SpawnPlan plan) {
        Gateway dynamicGateway = gatewayFactory.create(definition, plan);
        ResourceLocation id = GeneHunterGatewayBinder.runtimeId(definition.getId(), dynamicGateway);
        DynamicHolder<Gateway> gateway = GeneHunterGatewayBinder.bind(id, dynamicGateway, null);
        ItemStack stack = new ItemStack(GatewayObjects.GATE_PEARL);
        GatePearlItem.setGate(stack, gateway);
        return stack;
    }
}
