package com.pz.beyond.api.pack;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.system.definition.ProgressDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProgressPack extends SimplePreparableReloadListener<Map<ResourceLocation, ProgressDefinition>> {

    public static final String PATH_PREFIX = "progress";

    private static volatile Map<ResourceLocation, ProgressDefinition> latestDefinitions = Map.of();

    @Override
    protected Map<ResourceLocation, ProgressDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ProgressDefinition> definitions = new HashMap<>();

        profiler.startTick();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(PATH_PREFIX,
            path -> path.getPath().endsWith(".json")).entrySet()) {

            ResourceLocation fileLocation = entry.getKey();
            Resource resource = entry.getValue();

            try (BufferedReader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                var result = ProgressDefinition.CODEC.parse(JsonOps.INSTANCE, json);

                if (result.error().isPresent()) {
                    Beyond.debugLog("Failed to parse progress {}: {}", fileLocation, result.error().get().message());
                    continue;
                }

                ProgressDefinition definition = result.result().orElse(null);
                if (definition == null || definition.getIdentifier() == null) {
                    Beyond.debugLog("Skip progress {} because identifier is missing", fileLocation);
                    continue;
                }

                definitions.put(definition.getIdentifier(), definition);
                Beyond.debugLog("Loaded progress {} from data pack {}", definition.getIdentifier(), resource.sourcePackId());
            } catch (IOException e) {
                Beyond.debugLog("Failed to read progress {}: {}", fileLocation, e.getMessage());
            } catch (Exception e) {
                Beyond.debugLog("Failed to load progress {}", fileLocation, e);
            }
        }
        profiler.endTick();

        return definitions;
    }

    @Override
    protected void apply(Map<ResourceLocation, ProgressDefinition> definitions, ResourceManager resourceManager, ProfilerFiller profiler) {
        latestDefinitions = Collections.unmodifiableMap(new HashMap<>(definitions));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            applyToServer(server);
        }
        Beyond.debugLog("Loaded {} progress definitions from data packs", latestDefinitions.size());
    }

    public static Map<ResourceLocation, ProgressDefinition> getLatestDefinitions() {
        return latestDefinitions;
    }

    public static void applyToServer(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }

        var data = BeyondAPI.getBeyondLevelData(level);
        data.setProgressDefinitions(new HashMap<>(latestDefinitions));
    }
}
