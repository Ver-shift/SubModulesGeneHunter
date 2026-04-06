package org.galaxylib.api.system.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import org.galaxylib.GalaxyLib;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxylib.api.system.loot.data.GeneLootTableData;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 战利品表数据包加载器
 * <p>
 * 从 data/<namespace>/gene_loot_table/ 目录加载 JSON 文件
 */
public class LootPack extends SimplePreparableReloadListener<Map<ResourceLocation, GeneLootTableData>> {

    // 数据包路径前缀
    public static final String PATH_PREFIX = "gene_loot_table";
    private static volatile Map<ResourceLocation, GeneLootTableData> latestTables = Map.of();

    @Override
    protected Map<ResourceLocation, GeneLootTableData> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, GeneLootTableData> tables = new HashMap<>();

        profiler.startTick();

        // 扫描所有命名空间的 gene_loot_table 目录
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(PATH_PREFIX,
            path -> path.getPath().endsWith(".json")).entrySet()) {

            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try (BufferedReader reader = resource.openAsReader()) {
                // 解析 JSON
                JsonElement json = JsonParser.parseReader(reader);

                // 使用 CODEC 解析为 GeneLootTableData
                var result = GeneLootTableData.CODEC.parse(JsonOps.INSTANCE, json);
                if (result.error().isPresent()) {
                    GalaxyLib.LOGGER.error("Failed to parse loot table {}: {}", location, result.error().get().message());
                    continue;
                }
                GeneLootTableData table = result.result().orElse(null);

                if (table != null) {
                    // 使用 JSON 中定义的 identify 作为 key
                    ResourceLocation tableId = table.getIdentify();
                    tables.put(tableId, table);
                }

            } catch (IOException e) {
                GalaxyLib.LOGGER.error("Failed to read loot table {}: {}", location, e.getMessage());
            } catch (Exception e) {
                GalaxyLib.LOGGER.error("Failed to parse loot table {}: {}", location, e.getMessage());
            }
        }

        profiler.endTick();
        return tables;
    }

    @Override
    protected void apply(Map<ResourceLocation, GeneLootTableData> tables, ResourceManager resourceManager, ProfilerFiller profiler) {
        latestTables = Collections.unmodifiableMap(new HashMap<>(tables));
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            LevelLootData.get(server).setTables(latestTables);
        }
        GalaxyLib.LOGGER.info("Loaded {} gene loot tables from data packs", tables.size());

        // 注意：reload 阶段可能还拿不到稳定的 server 引用，玩家同步放到 OnDatapackSyncEvent。
    }
    
    /**
     * 将战利品表数据同步到所有在线玩家
     */
    public static Map<ResourceLocation, GeneLootTableData> getLatestTables() {
        return latestTables;
    }

    public static void syncToAllPlayers() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        try {
            var manager = GalaxyLibAPI.getLootTableManager(player);
            if (manager != null) {
                manager.init();
            }
        } catch (Exception e) {
            GalaxyLib.LOGGER.error("Failed to sync loot tables to player {}", player.getName().getString(), e);
        }
    }

}
