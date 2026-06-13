package org.galaxy.beyond.api.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.event.custom.ResolveEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.definition.SpawnDefinition;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取 {@code data/<namespace>/spawn_definitions/*.json}。
 */
public class SpawnDataPack extends SimplePreparableReloadListener<Map<ResourceLocation, SpawnDefinition>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new IdentifierTypeAdapter())
            .create();

    private static Map<ResourceLocation, SpawnDefinition> pendingPreparations;

    @Override
    protected Map<ResourceLocation, SpawnDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, SpawnDefinition> map = new HashMap<>();

        for (var entry : manager.listResources(
                "spawn_definitions",
                id -> id.getPath().endsWith(".json")
        ).entrySet()) {
            ResourceLocation fullId = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                SpawnDefinition definition = GSON.fromJson(reader, SpawnDefinition.class);
                ResourceLocation key = definitionId(fullId);
                if (definition.getId() == null) {
                    definition.setId(key);
                }
                map.put(key, definition);
                LOGGER.debug("Parsed spawn definition: {}", key);
            } catch (Exception e) {
                LOGGER.error("Failed to parse spawn definition: {}", fullId, e);
            }
        }

        LOGGER.info("Prepared {} spawn definitions from datapacks", map.size());
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, SpawnDefinition> preparations, ResourceManager manager, ProfilerFiller profiler) {
        if (tryApply(preparations)) return;
        pendingPreparations = preparations;
        LOGGER.info("Server not ready yet, stored {} spawn definitions for deferred apply", preparations.size());
    }

    public static void applyPending(MinecraftServer server) {
        if (pendingPreparations == null || pendingPreparations.isEmpty()) return;
        apply(server, pendingPreparations);
        pendingPreparations = null;
    }

    private static boolean tryApply(Map<ResourceLocation, SpawnDefinition> preparations) {
        MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        apply(server, preparations);
        return true;
    }

    private static void apply(MinecraftServer server, Map<ResourceLocation, SpawnDefinition> preparations) {
        Map<ResourceLocation, SpawnDefinition> definitions = new HashMap<>(preparations);
        ResolveEvent.ResolveSpawnDefinitionsEvent event = new ResolveEvent.ResolveSpawnDefinitionsEvent(
                server.overworld(),
                Map.copyOf(definitions),
                new HashMap<>(definitions)
        );
        NeoForge.EVENT_BUS.post(event);
        definitions = new HashMap<>(event.getTo());

        var spawnDefinitions = BeyondAPI.getRogueDefinition(server.overworld()).getSpawnDefinitions();
        LOGGER.info("Replacing {} old spawn definitions with {} new ones", spawnDefinitions.size(), definitions.size());
        spawnDefinitions.clear();
        spawnDefinitions.putAll(definitions);
        LOGGER.info("Applied {} spawn definitions", definitions.size());
    }

    private ResourceLocation definitionId(ResourceLocation fullId) {
        String path = fullId.getPath();
        path = path.substring("spawn_definitions/".length());
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        return ResourceLocation.fromNamespaceAndPath(fullId.getNamespace(), path);
    }
}
