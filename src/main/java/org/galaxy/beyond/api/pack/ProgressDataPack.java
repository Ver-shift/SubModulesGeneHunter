package org.galaxy.beyond.api.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.definition.DefinitionManager;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 在 /reload 时扫描 data/*\/beyond/rogue_progress/ 下的所有 JSON 并解析为 ProgressDefinition。
 * 键 = 本 mod 命名空间 + JSON 文件名（不含扩展名）。例如 beyond:forest。
 */
public class ProgressDataPack extends SimplePreparableReloadListener<Map<Identifier, ProgressDefinition>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();

    @Override
    protected Map<Identifier, ProgressDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, ProgressDefinition> map = new HashMap<>();

        for (var entry : manager.listResources("beyond/rogue_progress", id -> id.getPath().endsWith(".json")).entrySet()) {
            Identifier fullId = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                ProgressDefinition def = GSON.fromJson(reader, ProgressDefinition.class);
                Identifier key = Beyond.asResource(extractName(fullId.getPath()));
                map.put(key, def);
            } catch (Exception e) {
                LOGGER.error("Failed to parse progress definition: {}", fullId, e);
            }
        }

        LOGGER.info("Loaded {} progress definitions", map.size());
        return map;
    }

    @Override
    protected void apply(Map<Identifier, ProgressDefinition> preparations, ResourceManager manager, ProfilerFiller profiler) {
    }


}
