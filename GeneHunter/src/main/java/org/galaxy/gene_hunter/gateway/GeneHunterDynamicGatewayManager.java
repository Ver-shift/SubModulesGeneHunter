package org.galaxy.gene_hunter.gateway;

import com.mojang.datafixers.util.Either;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.RegistryCallback;
import dev.shadowsoffire.placebo.reload.ReloadListenerPayloads;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GeneHunterDynamicGatewayManager {

    private static final String GATEWAY_PATH = "gateways";
    private static final Map<ResourceLocation, Gateway> GATEWAYS = new LinkedHashMap<>();
    private static boolean restored;

    private GeneHunterDynamicGatewayManager() {
    }

    public static void bootstrap() {
        GatewayRegistry.INSTANCE.addCallback(RegistryCallback.reloadOnly(registry -> {
            if (!GATEWAYS.isEmpty()) {
                GATEWAYS.forEach((id, gateway) -> GeneHunterDynamicRegistryHelper.register(registry, id, gateway));
            }
        }));
    }

    public static DynamicHolder<Gateway> register(ResourceLocation id, Gateway gateway, ServerLevel level) {
        DynamicHolder<Gateway> holder = registerInternal(id, gateway);
        if (level != null) {
            dynamicData(level).put(id, GeneHunterDynamicRegistryHelper.encode(GatewayRegistry.INSTANCE, gateway));
            syncToAll();
        }
        return holder;
    }

    public static void restore(ServerLevel level) {
        if (level == null) {
            return;
        }
        GATEWAYS.clear();
        dynamicData(level).entries().forEach((id, json) -> {
            Gateway gateway = GeneHunterDynamicRegistryHelper.decode(GatewayRegistry.INSTANCE, json);
            registerInternal(id, gateway);
        });
        restored = true;
    }

    public static void syncToAll() {
        PacketDistributor.sendToAllPlayers(new ReloadListenerPayloads.Start(GATEWAY_PATH));
        GatewayRegistry.INSTANCE.getKeys().forEach(id -> {
            Gateway gateway = GatewayRegistry.INSTANCE.getValue(id);
            if (gateway != null) {
                PacketDistributor.sendToAllPlayers(new ReloadListenerPayloads.Content<>(GATEWAY_PATH, id, Either.left(gateway)));
            }
        });
        PacketDistributor.sendToAllPlayers(new ReloadListenerPayloads.End(GATEWAY_PATH));
    }

    public static void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ReloadListenerPayloads.Start(GATEWAY_PATH));
        GatewayRegistry.INSTANCE.getKeys().forEach(id -> {
            Gateway gateway = GatewayRegistry.INSTANCE.getValue(id);
            if (gateway != null) {
                PacketDistributor.sendToPlayer(player, new ReloadListenerPayloads.Content<>(GATEWAY_PATH, id, Either.left(gateway)));
            }
        });
        PacketDistributor.sendToPlayer(player, new ReloadListenerPayloads.End(GATEWAY_PATH));
    }

    public static void clear() {
        GATEWAYS.clear();
        restored = false;
    }

    public static void restoreIfNeeded(ServerLevel level) {
        if (!restored) {
            restore(level);
        }
    }

    public static void ensureAvailable(ServerLevel level, ResourceLocation id) {
        if (GeneHunterDynamicRegistryHelper.contains(GatewayRegistry.INSTANCE, id) || level == null) {
            return;
        }
        String json = dynamicData(level).get(id);
        if (json != null && !json.isEmpty()) {
            registerInternal(id, GeneHunterDynamicRegistryHelper.decode(GatewayRegistry.INSTANCE, json));
        }
    }

    private static DynamicHolder<Gateway> registerInternal(ResourceLocation id, Gateway gateway) {
        DynamicHolder<Gateway> holder = GeneHunterDynamicRegistryHelper.register(GatewayRegistry.INSTANCE, id, gateway);
        GATEWAYS.put(id, gateway);
        return holder;
    }

    private static GeneHunterDynamicGatewayData dynamicData(ServerLevel level) {
        return level.getData(GeneHunterAttachInit.DYNAMIC_GATEWAY_DATA.get());
    }
}
