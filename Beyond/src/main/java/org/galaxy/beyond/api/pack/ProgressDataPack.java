package org.galaxy.beyond.api.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.BeyondGlobalData;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * 在 /reload 时扫描 data/beyond/rogue_progress/ 下的所有 JSON，
 * 反序列化为 {@link ProgressDefinition} 并注入到 {@link BeyondGlobalData}。
 */
public class ProgressDataPack extends SimplePreparableReloadListener<Map<ResourceLocation, ProgressDefinition>> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new IdentifierTypeAdapter())
            .create();

    /** 首次加载时服务器尚未就绪，暂存数据，等 ServerStartedEvent 时注入 */
    private static Map<ResourceLocation, ProgressDefinition> pendingPreparations;

    @Override
    protected Map<ResourceLocation, ProgressDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, ProgressDefinition> map = new HashMap<>();

        for (var entry : manager.listResources(
                "rogue_progress",
                id -> id.getNamespace().equals(Beyond.MODID) && id.getPath().endsWith(".json")
        ).entrySet()) {
            ResourceLocation fullId = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                ProgressDefinition def = GSON.fromJson(reader, ProgressDefinition.class);

                String fileName = fullId.getPath();
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                if (fileName.endsWith(".json")) {
                    fileName = fileName.substring(0, fileName.length() - 5);
                }
                ResourceLocation key = Beyond.asResource(fileName);
                map.put(key, def);

                LOGGER.debug("Parsed progress definition: {}", key);
            } catch (Exception e) {
                LOGGER.error("Failed to parse progress definition: {}", fullId, e);
            }
        }

        LOGGER.info("Prepared {} progress definitions from datapacks", map.size());
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, ProgressDefinition> preparations, ResourceManager manager, ProfilerFiller profiler) {
        // 直接尝试注入，失败则暂存等待 ServerStartedEvent
        if (tryApply(preparations)) return;

        pendingPreparations = preparations;
        LOGGER.info("Server not ready yet, stored {} progress definitions for deferred apply", preparations.size());
    }

    /**
     * 服务端启动完成后由 BeyondManagerEventHandle 调用，
     * 将暂存的关卡数据注入 GlobalData。
     */
    public static void applyPending(MinecraftServer server) {
        if (pendingPreparations == null || pendingPreparations.isEmpty()) return;
        tryApply(server, pendingPreparations);
        pendingPreparations = null;
    }

    private static boolean tryApply(Map<ResourceLocation, ProgressDefinition> preparations) {
        MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        tryApply(server, preparations);
        return true;
    }

    private static void tryApply(MinecraftServer server, Map<ResourceLocation, ProgressDefinition> preparations) {
        var globalData = BeyondAPI.getGlobalData(server.overworld());
        if (globalData == null) {
            LOGGER.error("Cannot apply progress definitions: globalData is null");
            return;
        }

        var oldKeys = globalData.getRogueDefinition().getRogueProgress().keySet();
        LOGGER.info("Replacing {} old progress definitions with {} new ones", oldKeys.size(), preparations.size());
        LOGGER.debug("  Old keys: {}", oldKeys);
        LOGGER.debug("  New keys: {}", preparations.keySet());

        globalData.getRogueDefinition().getRogueProgress().clear();
        globalData.getRogueDefinition().getRogueProgress().putAll(preparations);

        LOGGER.info("Applied {} progress definitions to GlobalData", preparations.size());
    }
}
