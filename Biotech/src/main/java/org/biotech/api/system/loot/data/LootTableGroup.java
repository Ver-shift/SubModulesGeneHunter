package org.biotech.api.system.loot.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 战利品表组 - 管理多个战利品表
 * 尽量不要直接调用getLootTable();会造成数据无法更新
 */

public class LootTableGroup {

    private Map<ResourceLocation, LootInstance> lootTables = new HashMap<>();

    /**
     * 脏数据，需要更新，用于更新各个概率
     */
    @Setter @Getter
    private boolean dirty;

    /**
     * 全局概率缓存表 - 存储所有 Entry 的最终抽取概率
     * <p>
     * <b>数据结构：</b>
     * <ul>
     *   <li>Key - Entry 的 ID（物品ID或词条ID，理论上不会冲突）</li>
     *   <li>Value - 该 Entry 的最终概率（0.0 ~ 1.0），相同 ID 的概率会累加</li>
     * </ul>
     * <p>
     * <b>计算公式：</b>
     * <pre>
     * 最终概率 = LootTable概率 × Pool概率 × Entry概率
     * </pre>
     * <p>
     * <b>特性：</b>
     * <ul>
     *   <li>相同 ID 的 Entry 概率会自动累加（支持跨表/跨池的同ID条目）</li>
     *   <li>通过 {@link #refreshData()} 方法更新，由 dirty 标记触发</li>
     * </ul>
     *
     * @see #refreshData()
     * @see #calculateLootTableChance()
     * @see #calculatePoolChance(LootInstance)
     * @see #calculateEntryChance(LootPoolData)
     */
    @Setter
    private Map<ResourceLocation,Float> globeChance = new HashMap<>();

    public Map<ResourceLocation, Float> getGlobeChance() {
        if (dirty) {
            refreshData();
        }
        return globeChance;
    }

    public Map<ResourceLocation, LootInstance> getLootTables() {
        if (dirty) {
            refreshData();
            dirty = false;
        }
        return lootTables;
    }

    public LootTableGroup(){
        refreshData();
    }

    public void put(GeneLootTableData data){
        put(data,1);
    }
    public void put(GeneLootTableData data,int weight){
        if (data == null) {
            return;
        }
        lootTables.put(data.getIdentify(),new LootInstance(data,weight));
        dirty = true;
    }


    // ==================== 查询方法 ====================

    public LootInstance getLootTable(ResourceLocation lootTableIdentify) {
        return lootTables.get(lootTableIdentify);
    }

    /**
     * 获取所有指定名称的 pools，名字是能够重复的
     */
    public List<LootPoolData> findPools(String name) {
        return lootTables.values().stream()
                .flatMap(instance -> instance.getLootTableData().getPools().stream())
                .filter(pool -> pool.getName().equals(name))
                .toList();
    }



    // ==================== 查找方法 ====================

    /**
     * 根据 ID 查找 Pool
     */
    private LootPoolData findPool(LootInstance instance, String poolName) {
        if (instance == null || instance.getLootTableData() == null) return null;
        return instance.getLootTableData().getPools().stream()
                .filter(pool -> pool.getName().equals(poolName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据 ID 查找 Entry
     */
    private LootPoolData.Entry findEntry(LootPoolData pool, ResourceLocation entryId) {
        if (pool == null) return null;
        return pool.getEntries().stream()
                .filter(entry -> entry.getId().equals(entryId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据条目 ID 查找 Entry（公开方法，遍历所有表和池）
     * @param entryId 条目 ID
     * @return 找到的 Entry，如果未找到返回 null
     */
    public LootPoolData.Entry findEntryById(ResourceLocation entryId) {
        for (LootInstance instance : lootTables.values()) {
            if (instance.getLootTableData() == null) continue;
            for (LootPoolData pool : instance.getLootTableData().getPools()) {
                LootPoolData.Entry entry = findEntry(pool, entryId);
                if (entry != null) return entry;
            }
        }
        return null;
    }

    // ==================== 文本类输入（ResourceLocation / String）====================

    /**
     * 计算每个 LootTable 被抽中的概率
     */
    public Map<ResourceLocation, Float> calculateLootTableChance() {
        return calculateChance(
                lootTables.entrySet().stream(),
                Map.Entry::getKey,
                entry -> entry.getValue().getLootTableWeight()
        );
    }

    /**
     * 计算单个 LootTable 里面每个 Pool 的概率
     *
     * @param lootTableIdentify 战利品表ID
     */
    public Map<String, Float> calculatePoolChance(ResourceLocation lootTableIdentify) {
        LootInstance instance = lootTables.get(lootTableIdentify);
        return instance != null ? calculatePoolChance(instance) : Map.of();
    }

    /**
     * 计算单个 Pool 里面每个 Entry 的抽取概率
     *
     * @param lootTableIdentify 战利品表ID
     * @param poolName 池名称
     */
    public Map<ResourceLocation, Float> calculateEntryChance(ResourceLocation lootTableIdentify, String poolName) {
        LootInstance instance = lootTables.get(lootTableIdentify);
        LootPoolData pool = findPool(instance, poolName);
        return pool != null ? calculateEntryChance(pool) : Map.of();
    }

    // ==================== 实例类输入（LootInstance / LootPoolData）====================

    /**
     * 计算单个 LootTable 里面每个 Pool 的概率
     *
     * @param instance 战利品表实例
     */
    public Map<String, Float> calculatePoolChance(LootInstance instance) {
        if (instance == null || instance.getLootTableData() == null) return Map.of();
        return calculateChance(
                instance.getLootTableData().getPools().stream(),
                LootPoolData::getName,
                LootPoolData::getBaseWeight
        );
    }

    /**
     * 计算单个 Pool 里面每个 Entry 的抽取概率
     *
     * @param pool 池数据
     */
    public Map<ResourceLocation, Float> calculateEntryChance(LootPoolData pool) {
        if (pool == null) return Map.of();
        return calculateChance(
                pool.getEntries().stream(),
                LootPoolData.Entry::getId,
                LootPoolData.Entry::getWeight
        );
    }

    // ==================== 通用方法 ====================

    /**
     * 通用概率计算方法
     */
    private <K, T> Map<K, Float> calculateChance(java.util.stream.Stream<T> stream, java.util.function.Function<T, K> keyMapper, java.util.function.ToIntFunction<T> weightMapper) {
        List<T> items = stream.toList();
        int totalWeight = items.stream().mapToInt(weightMapper).sum();
        
        if (totalWeight <= 0) return Map.of();
        
        Map<K, Float> chances = new HashMap<>();
        items.forEach(item -> chances.put(keyMapper.apply(item), (float) weightMapper.applyAsInt(item) / totalWeight));
        return chances;
    }

    // ==================== 内部类 ====================

    /**
     * 单个战利品表实例
     */
    @Data
    public static class LootInstance {
        private int lootTableWeight = 1;
        private GeneLootTableData lootTableData;

        public LootInstance(GeneLootTableData lootTableData,int lootTableWeight) {
            this.lootTableWeight = lootTableWeight;
            this.lootTableData = lootTableData;
        }
    }

    // ==================== Set 方法 - 文本类输入（ResourceLocation / String）====================

    /**
     * 设置战利品表的权重
     *
     * @param lootTableId 战利品表ID
     * @param weight 新权重
     */
    public void setLootTableWeight(ResourceLocation lootTableId, int weight) {
        setLootTableWeight(lootTables.get(lootTableId), weight);
    }

    /**
     * 设置池的权重
     *
     * @param lootTableId 战利品表ID
     * @param poolName 池名称
     * @param weight 新权重
     */
    public void setPoolWeight(ResourceLocation lootTableId, String poolName, int weight) {
        setPoolWeight(findPool(lootTables.get(lootTableId), poolName), weight);
    }

    /**
     * 设置条目的权重
     *
     * @param lootTableId 战利品表ID
     * @param poolName 池名称
     * @param entryId 条目ID
     * @param weight 新权重
     */
    public void setEntryWeight(ResourceLocation lootTableId, String poolName, ResourceLocation entryId, int weight) {
        setEntryWeight(findEntry(findPool(lootTables.get(lootTableId), poolName), entryId), weight);
    }

    // ==================== Set 方法 - 实例类输入（LootInstance / LootPoolData / Entry）====================

    /**
     * 设置战利品表的权重
     *
     * @param instance 战利品表实例
     * @param weight 新权重
     */
    public void setLootTableWeight(LootInstance instance, int weight) {
        if (instance != null) {
            instance.setLootTableWeight(weight);
            dirty = true;
        }
    }

    /**
     * 设置池的权重
     *
     * @param pool 池数据
     * @param weight 新权重
     */
    public void setPoolWeight(LootPoolData pool, int weight) {
        if (pool != null) {
            pool.setBaseWeight(weight);
            dirty = true;
        }
    }

    /**
     * 设置条目的权重
     *
     * @param entry 条目数据
     * @param weight 新权重
     */
    public void setEntryWeight(LootPoolData.Entry entry, int weight) {
        if (entry != null) {
            entry.setWeight(weight);
            dirty = true;
        }
    }

    // ==================== Set Count 方法 - 文本类输入 ====================

    /**
     * 设置条目的数量
     *
     * @param lootTableId 战利品表ID
     * @param poolName 池名称
     * @param entryId 条目ID
     * @param count 新数量
     */
    public void setEntryCount(ResourceLocation lootTableId, String poolName, ResourceLocation entryId, int count) {
        setEntryCount(findEntry(findPool(lootTables.get(lootTableId), poolName), entryId), count);
    }

    // ==================== Set Count 方法 - 实例类输入 ====================

    /**
     * 设置条目的数量
     *
     * @param entry 条目数据
     * @param count 新数量
     */
    public void setEntryCount(LootPoolData.Entry entry, int count) {
        if (entry != null) {
            entry.setCount(count);
            dirty = true;
        }
    }

    public void refreshData() {
        globeChance.clear();

        if (lootTables.isEmpty()) return;

        // 1. 获取所有 LootTable 的概率
        Map<ResourceLocation, Float> tableChances = calculateLootTableChance();

        // 2. 遍历每个 LootTable
        lootTables.forEach((tableId, instance) -> {
            float tableChance = tableChances.getOrDefault(tableId, 0f);
            if (tableChance <= 0) return;

            // 3. 获取该 LootTable 内所有 Pool 的概率
            Map<String, Float> poolChances = calculatePoolChance(instance);

            // 4. 遍历每个 Pool
            if (instance.getLootTableData() == null) return;
            for (LootPoolData pool : instance.getLootTableData().getPools()) {
                float poolChance = poolChances.getOrDefault(pool.getName(), 0f);
                if (poolChance <= 0) continue;

                // 5. 获取该 Pool 内所有 Entry 的概率
                Map<ResourceLocation, Float> entryChances = calculateEntryChance(pool);

                // 6. 计算最终概率并累加
                entryChances.forEach((entryId, entryChance) -> {
                    // 最终概率 = Table概率 × Pool概率 × Entry概率
                    float finalChance = tableChance * poolChance * entryChance;
                    globeChance.merge(entryId, finalChance, Float::sum);
                });
            }
        });

        dirty = false;
    }
}

