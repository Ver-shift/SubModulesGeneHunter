package org.galaxylib.api.system.loot.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * 战利品表组 Builder - 链式调用 API
 * <p>
 * 提供链式查找和修改战利品表结构的能力
 * <p>
 * <b>使用示例：</b>
 * <pre>
 * LootTableGroupBuilder.builder(group)
 *     .modifyLootTable(tableId, table -> table.setWeight(10))
 *     .findLootTable(tableId)
 *     .ifPresent(table -> table
 *         .modifyPool("main", pool -> pool.setWeight(5))
 *         .findPool("main")
 *         .ifPresent(pool -> pool
 *             .modifyEntry(entryId, e -> e.setWeight(3).setCount(2))));
 * </pre>
 */
public class LootTableGroupBuilder {

    private final LootTableGroup group;

    private LootTableGroupBuilder(LootTableGroup group) {
        this.group = group;
    }

    /**
     * 静态工厂方法
     */
    public static LootTableGroupBuilder builder(LootTableGroup group) {
        return new LootTableGroupBuilder(group);
    }

    // ==================== 本层操作 ====================

    /**
     * 查找战利品表
     * @param id 战利品表 ID
     * @return Optional 包装的 LootTableBuilder
     */
    public Optional<LootTableBuilder> findLootTable(ResourceLocation id) {
        LootTableGroup.LootInstance instance = group.getLootTables().get(id);
        return instance != null 
            ? Optional.of(new LootTableBuilder(group, instance, id)) 
            : Optional.empty();
    }

    /**
     * 修改战利品表（只修改 LootInstance 层的数据）
     * @param id 战利品表 ID
     * @param consumer 修改逻辑
     * @return 返回自身支持链式调用
     */
    public LootTableGroupBuilder modifyLootTable(ResourceLocation id, Consumer<LootTableBuilder> consumer) {
        findLootTable(id).ifPresent(consumer);
        return this;
    }

    // ==================== 跃层查找（不提供跃层修改）====================

    /**
     * 跃层查找 Pool
     * @param tableId 战利品表 ID
     * @param poolName 池名称
     * @return Optional 包装的 PoolBuilder
     */
    public Optional<PoolBuilder> findPool(ResourceLocation tableId, String poolName) {
        return findLootTable(tableId)
            .flatMap(table -> table.findPool(poolName));
    }

    /**
     * 跃层查找 Entry
     * @param tableId 战利品表 ID
     * @param poolName 池名称
     * @param entryId 条目 ID
     * @return Optional 包装的 EntryBuilder
     */
    public Optional<EntryBuilder> findEntry(ResourceLocation tableId, String poolName, ResourceLocation entryId) {
        return findLootTable(tableId)
            .flatMap(table -> table.findEntry(poolName, entryId));
    }

    // ==================== 内部 Builder 类 ====================

    /**
     * 战利品表 Builder - 操作 LootInstance 层
     */
    public class LootTableBuilder {
        private final LootTableGroup group;
        private final LootTableGroup.LootInstance instance;
        private final ResourceLocation tableId;

        private LootTableBuilder(LootTableGroup group, LootTableGroup.LootInstance instance, ResourceLocation tableId) {
            this.group = group;
            this.instance = instance;
            this.tableId = tableId;
        }

        // === 本层修改 ===

        /**
         * 设置战利品表权重
         */
        public LootTableBuilder setWeight(int weight) {
            group.setLootTableWeight(instance, weight);
            return this;
        }

        // === 本层查找 ===

        /**
         * 查找池
         */
        public Optional<PoolBuilder> findPool(String poolName) {
            if (instance.getLootTableData() == null) return Optional.empty();
            return instance.getLootTableData().getPools().stream()
                .filter(pool -> pool.getName().equals(poolName))
                .findFirst()
                .map(pool -> new PoolBuilder(pool, this));
        }

        /**
         * 修改池（只修改 LootPoolData 层的数据）
         */
        public LootTableBuilder modifyPool(String poolName, Consumer<PoolBuilder> consumer) {
            findPool(poolName).ifPresent(consumer);
            return this;
        }

        // === 跃层查找 ===

        /**
         * 跃层查找 Entry
         */
        public Optional<EntryBuilder> findEntry(String poolName, ResourceLocation entryId) {
            return findPool(poolName)
                .flatMap(pool -> pool.findEntry(entryId));
        }

        /**
         * 返回上级 Builder
         */
        public LootTableGroupBuilder end() {
            return LootTableGroupBuilder.this;
        }
    }

    /**
     * 池 Builder - 操作 LootPoolData 层
     */
    public class PoolBuilder {
        private final LootPoolData pool;
        private final LootTableBuilder parent;

        private PoolBuilder(LootPoolData pool, LootTableBuilder parent) {
            this.pool = pool;
            this.parent = parent;
        }

        // === 本层修改 ===

        /**
         * 设置池权重
         */
        public PoolBuilder setWeight(int weight) {
            parent.end().group.setPoolWeight(pool, weight);
            return this;
        }

        // === 本层查找 ===

        /**
         * 查找条目
         */
        public Optional<EntryBuilder> findEntry(ResourceLocation entryId) {
            return pool.getEntries().stream()
                .filter(entry -> entry.getId().equals(entryId))
                .findFirst()
                .map(entry -> new EntryBuilder(entry, this));
        }

        /**
         * 修改条目（只修改 Entry 层的数据）
         */
        public PoolBuilder modifyEntry(ResourceLocation entryId, Consumer<EntryBuilder> consumer) {
            findEntry(entryId).ifPresent(consumer);
            return this;
        }

        /**
         * 返回上级 Builder
         */
        public LootTableBuilder endTable() {
            return parent;
        }

        /**
         * 返回顶层 Builder
         */
        public LootTableGroupBuilder end() {
            return parent.end();
        }
    }

    /**
     * 条目 Builder - 操作 Entry 层
     */
    public class EntryBuilder {
        private final LootPoolData.Entry entry;
        private final PoolBuilder parent;

        private EntryBuilder(LootPoolData.Entry entry, PoolBuilder parent) {
            this.entry = entry;
            this.parent = parent;
        }

        /**
         * 设置权重
         */
        public EntryBuilder setWeight(int weight) {
            parent.end().group.setEntryWeight(entry, weight);
            return this;
        }

        /**
         * 设置数量
         */
        public EntryBuilder setCount(int count) {
            parent.end().group.setEntryCount(entry, count);
            return this;
        }

        /**
         * 返回上级 Builder
         */
        public PoolBuilder endPool() {
            return parent;
        }

        /**
         * 返回战利品表 Builder
         */
        public LootTableBuilder endTable() {
            return parent.endTable();
        }

        /**
         * 返回顶层 Builder
         */
        public LootTableGroupBuilder end() {
            return parent.end();
        }
    }
}
