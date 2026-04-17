package org.galaxylib.api.system.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import org.galaxylib.api.init.GalaxyLibAttachInit;
import org.galaxylib.api.system.loot.core.ILootTableManager;
import org.galaxylib.api.system.random.RandomManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.GeneLootTableData;
import org.galaxylib.api.system.loot.data.LootPoolData;
import org.galaxylib.api.system.loot.data.LootTableGroup;
import org.galaxylib.api.system.loot.data.LootTableGroupBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTableManager implements ILootTableManager {

    private final PlayerLootTableData playerLootTableData;
    private final ServerPlayer player;
    private final RandomManager randomManager;

    public LootTableManager(PlayerLootTableData data) {
        this.playerLootTableData = data;
        this.player = data.getPlayer();
        this.randomManager = player != null ? GalaxyLibAttachInit.getRandomManager(player.level()) : null;
    }

    @Override
    public void init() {

    }


    @Override
    public ILootTableManager modify(Supplier<ILootType<?>> lootType, Consumer<LootTableGroupBuilder> builderConsumer) {
        LootTableGroup group = getLootTableGroup(lootType);
        if (group != null) {
            LootTableGroupBuilder builder = LootTableGroupBuilder.builder(group);
            builderConsumer.accept(builder);
        }
        return this;
    }

    @Override
    public LootResult roolWithoutReplacement(Supplier<ILootType<?>> lootType) {
        ILootType<?> type = lootType.get();
        return roolWithoutReplacement(type);
    }

    @Override
    public LootResult roolWithoutReplacement(ILootType<?> lootType) {
        return roolWithoutReplacement(lootType, RandomManager.PROGRESS_RANDOM_ID);
    }

    public LootResult roolWithoutReplacement(ILootType<?> lootType, String randomId) {
        int count = lootType.getPoolCount(player);
        LootTableGroup group = getLootTableGroup(lootType);
        if (group == null) return new LootResult(List.of(), lootType);

        List<LootPoolData.Entry> results = new ArrayList<>();
        SingleThreadedRandomSource random = randomManager.getSeed(randomId);

        // 获取 globeChance 作为概率池
        Map<ResourceLocation, Float> chancePool = new HashMap<>(group.getGlobeChance());

        for (int i = 0; i < count && !chancePool.isEmpty(); i++) {
            // 加权随机选择
            ResourceLocation selectedId = weightedRandomSelect(chancePool, random);
            if (selectedId == null) break;

            // 查找对应的 Entry
            LootPoolData.Entry entry = group.findEntryById(selectedId);
            if (entry != null) {
                results.add(entry);
            }
            // 不放回：从池中移除已抽中的条目
            chancePool.remove(selectedId);
        }

        return new LootResult(results, lootType);
    }

    @Override
    public LootResult rollWithReplacement(Supplier<ILootType<?>> lootType) {
        ILootType<?> type = lootType.get();
        return roolWithoutReplacement(type);
    }

    @Override
    public LootResult rollWithReplacement(ILootType<?> lootType) {
        return rollWithReplacement(lootType, RandomManager.PROGRESS_RANDOM_ID);
    }

    public LootResult rollWithReplacement(ILootType<?> lootType, String randomId) {
        int count = lootType.getPoolCount(player);
        LootTableGroup group = getLootTableGroup(lootType);
        if (group == null) return new LootResult(List.of(), lootType);

        List<LootPoolData.Entry> results = new ArrayList<>();
        SingleThreadedRandomSource random = randomManager.getSeed(randomId);

        // 获取 globeChance 作为概率池
        Map<ResourceLocation, Float> chancePool = group.getGlobeChance();

        for (int i = 0; i < count && !chancePool.isEmpty(); i++) {
            // 加权随机选择
            ResourceLocation selectedId = weightedRandomSelect(chancePool, random);
            if (selectedId == null) break;

            // 查找对应的 Entry
            LootPoolData.Entry entry = group.findEntryById(selectedId);
            if (entry != null) {
                results.add(entry);
            }
            // 放回：不移除，保持池子不变
        }
        return new LootResult(results, lootType);
    }


    @Override
    public void setWeightByName(String name, int weight) {
        // 遍历所有 LootTableGroup 中所有匹配的 pool
        playerLootTableData.getLootTableGroups().values().forEach(group -> {
            group.findPools(name).forEach(pool -> pool.setBaseWeight(weight));
            group.setDirty(true);
        });
    }

    @Override
    public void merge(Supplier<ILootType<?>> targetLootType, ResourceLocation... lootTableIdentify) {
        LootTableGroup targetGroup = getLootTableGroup(targetLootType);
        if (targetGroup == null) return;

        Map<ResourceLocation, GeneLootTableData> levelTables = getLevelTables();

        for (ResourceLocation identify : lootTableIdentify) {
            GeneLootTableData data = levelTables.get(identify);
            if (data != null) {
                targetGroup.put(data);
            }
        }

        targetGroup.setDirty(true);
    }

    @Override
    public void claimResults(LootResult results) {
        results.lootType().claimResultsToPlayer(player, results);
    }


    // ==================== 辅助方法 ====================

    /**
     * 加权随机选择 - 使用 globeChance 概率
     *
     * @param pool   概率池 (Entry ID -> 概率)
     * @param random 随机数源
     * @return 选中的 Entry ID
     */
    private ResourceLocation weightedRandomSelect(Map<ResourceLocation, Float> pool, RandomSource random) {
        if (pool.isEmpty()) return null;

        float totalChance = (float) pool.values().stream().mapToDouble(f -> f).sum();
        if (totalChance <= 0) return null;

        float randomValue = random.nextFloat() * totalChance;
        float currentChance = 0;

        for (Map.Entry<ResourceLocation, Float> entry : pool.entrySet()) {
            currentChance += entry.getValue();
            if (randomValue <= currentChance) {
                return entry.getKey();
            }
        }

        // 返回最后一个（防止浮点误差）
        return pool.keySet().iterator().next();
    }


    private LootTableGroup getLootTableGroup(Supplier<ILootType<?>> lootType) {
        return playerLootTableData.getLootTableGroup(lootType);
    }
    private LootTableGroup getLootTableGroup(ILootType<?> lootType) {
        return playerLootTableData.getLootTableGroup(() -> lootType);
    }

    private Map<ResourceLocation, GeneLootTableData> getLevelTables() {
        if (player == null || player.getServer() == null) {
            return LootPack.getLatestTables();
        }

        Map<ResourceLocation, GeneLootTableData> tables = LevelLootData.get(player.getServer()).snapshot();
        if (tables.isEmpty()) {
            return LootPack.getLatestTables();
        }
        return tables;
    }

}

