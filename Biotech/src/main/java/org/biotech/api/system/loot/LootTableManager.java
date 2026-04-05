package org.biotech.api.system.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.biotech.api.GeneData;
import org.biotech.api.init.LootTypeInit;
import org.biotech.api.system.loot.core.ILootTableManager;
import org.biotech.api.system.loot.core.ILootType;
import org.biotech.api.system.loot.data.LootPoolData;
import org.biotech.api.system.loot.data.LootTableGroup;
import org.biotech.api.system.loot.data.LootTableGroupBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootTableManager implements ILootTableManager {

    private final GeneData data;
    private final PlayerLootTableData playerLootTableData;
    private final ServerPlayer player;

    public LootTableManager(GeneData data) {
        this.data = data;
        this.playerLootTableData = data.getPlayerLootTableData();
        this.player = data.getPlayer();

    }

    @Override
    public void init(){
        merge(LootTypeInit.GENE_TRAIT_LOOT_TYPE,
                ResourceLocation.parse("biotech:gene_trait_base"));

        merge(LootTypeInit.XENE_TRAIT_LOOT_TYPE,
                ResourceLocation.parse("biotech:xene_trait_base"));
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
        int count = type.getPoolCount(player);
        LootTableGroup group = getLootTableGroup(lootType);
        if (group == null) return new LootResult(List.of(), type);

        List<LootPoolData.Entry> results = new ArrayList<>();
        RandomSource random = player.getRandom();

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

        return new LootResult(results, type);
    }

    @Override
    public LootResult rollWithReplacement(Supplier<ILootType<?>> lootType) {
        ILootType<?> type = lootType.get();
        int count = type.getPoolCount(player);
        LootTableGroup group = getLootTableGroup(lootType);
        if (group == null) return new LootResult(List.of(), type);

        List<LootPoolData.Entry> results = new ArrayList<>();
        RandomSource random = player.getRandom();

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

        return new LootResult(results, type);
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

        for (ResourceLocation identify : lootTableIdentify) {
            var data = playerLootTableData.getDataPackTables().get(identify);
            targetGroup.put(data);
        }
    }

    @Override
    public void claimResults(LootResult results) {
        results.lootType().claimResultsToPlayer(player,results);
    }


    // ==================== 辅助方法 ====================

    /**
     * 加权随机选择 - 使用 globeChance 概率
     * @param pool 概率池 (Entry ID -> 概率)
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

    // ==================== 示例代码 ====================

    private void text() {
        modify(LootTypeInit.XENE_TRAIT_LOOT_TYPE, lootGroup -> {
            lootGroup.findLootTable(ResourceLocation.parse("biotech:sword_trait"))
                .flatMap(lootTable -> lootTable.findPool("普通物品")
                    .flatMap(pool -> pool.findEntry(ResourceLocation.parse("minecraft:iron_sword"))))
                .ifPresent(entry -> entry.setCount(1).setWeight(1));
        }).modify(LootTypeInit.FOOD_LOOT_TYPE, lootGroup -> {
            lootGroup.findPool(ResourceLocation.parse("biotech:sword"), "普通物品")
                .flatMap(pool -> pool.findEntry(ResourceLocation.parse("minecraft:apple")))
                .ifPresent(entry -> entry.setCount(1).setWeight(1));
        });
    }

    private LootTableGroup getLootTableGroup(Supplier<ILootType<?>> lootType) {
        return playerLootTableData.getLootTableGroup(lootType);
    }

}

