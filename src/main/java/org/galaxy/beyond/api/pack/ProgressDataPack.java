package org.galaxy.beyond.api.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.BeyondGlobalData;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 在 /reload 时扫描 data/beyond/rogue_progress/ 下的所有 JSON，
 * 反序列化为 {@link ProgressDefinition} 并注入到 {@link BeyondGlobalData}。
 */
public class ProgressDataPack extends SimplePreparableReloadListener<Map<Identifier, ProgressDefinition>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierTypeAdapter())
            .create();

    @Override
    protected Map<Identifier, ProgressDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, ProgressDefinition> map = new HashMap<>();

        for (var entry : manager.listResources(
                "rogue_progress",
                id -> id.getNamespace().equals(Beyond.MODID) && id.getPath().endsWith(".json")
        ).entrySet()) {
            Identifier fullId = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                ProgressDefinition def = GSON.fromJson(reader, ProgressDefinition.class);

                // 键 = beyond:<文件名>，例如 beyond:forest
                String fileName = fullId.getPath();
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1); // 去掉路径前缀
                if (fileName.endsWith(".json")) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }
                Identifier key = Beyond.asResource(fileName);
                map.put(key, def);

                LOGGER.debug("Loaded progress definition: {}", key);
            } catch (Exception e) {
                LOGGER.error("Failed to parse progress definition: {}", fullId, e);
            }
        }

        LOGGER.info("Loaded {} progress definitions", map.size());
        return map;
    }

    @Override
    protected void apply(Map<Identifier, ProgressDefinition> preparations, ResourceManager manager, ProfilerFiller profiler) {
        if (BeyondAPI.getOverMinecraftServer() == null) return;

        BeyondGlobalData globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
        globalData.getRogueDefinition().getRogueProgress().clear();
        globalData.getRogueDefinition().getRogueProgress().putAll(preparations);

        LOGGER.info("Applied {} progress definitions to GlobalData", preparations.size());
    }
}
