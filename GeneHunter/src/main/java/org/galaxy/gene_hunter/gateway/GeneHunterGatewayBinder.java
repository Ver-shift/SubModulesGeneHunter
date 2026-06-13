package org.galaxy.gene_hunter.gateway;

import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.galaxy.gene_hunter.GeneHunter;

public final class GeneHunterGatewayBinder {

    private GeneHunterGatewayBinder() {
    }

    public static DynamicHolder<Gateway> bind(ResourceLocation id, Gateway gateway, ServerLevel level) {
        DynamicHolder<Gateway> holder = GeneHunterDynamicGatewayManager.register(id, gateway, level == null ? currentOverworld() : level);
        if (holder.isBound()) return holder;
        throw new IllegalStateException("Failed to bind runtime gateway: " + id);
    }

    public static ResourceLocation runtimeId(ResourceLocation spawnDefinitionId, Gateway gateway) {
        String path = "runtime/" + spawnDefinitionId.getPath() + "/" + Integer.toHexString(gateway.hashCode());
        return GeneHunter.asResource(path);
    }

    private static ServerLevel currentOverworld() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.overworld();
    }
}
