package org.biotech.api.event.custom;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxylib.api.system.loot.data.GeneLootTableData;

import java.util.List;

/**
 * 战利品抽取事件
 * <p>
 * 包含三个阶段：
 * - Pre: 抽取前，可修改抽取次数
 * - Modify: 抽取后，可修改输出结果
 * - Post: 抽取完成，只读
 */
public class LootRollEvent extends PlayerEvent {

    protected LootRollEvent(Player player) {
        super(player);
    }

    /**
     * 抽取前事件
     * <p>
     * 在计算抽取次数之前触发，可以修改抽取次数
     */
    public static class Pre extends LootRollEvent implements ICancellableEvent {
        private int rollCount;
        private final GeneLootTableData lootTable;

        public Pre(Player player, GeneLootTableData lootTable, int rollCount) {
            super(player);
            this.lootTable = lootTable;
            this.rollCount = rollCount;
        }

        /**
         * 获取战利品表
         */
        public GeneLootTableData getLootTable() {
            return lootTable;
        }

        /**
         * 获取抽取次数
         */
        public int getRollCount() {
            return rollCount;
        }

        /**
         * 设置抽取次数
         */
        public void setRollCount(int rollCount) {
            this.rollCount = rollCount;
        }
    }

    /**
     * 修改输出事件
     * <p>
     * 在抽取完成后触发，可以修改输出结果
     */
    public static class Modify extends LootRollEvent implements ICancellableEvent {
        private final List<ResourceLocation> results;
        private final GeneLootTableData lootTable;

        public Modify(Player player, GeneLootTableData lootTable, List<ResourceLocation> results) {
            super(player);
            this.lootTable = lootTable;
            this.results = results;
        }

        /**
         * 获取战利品表
         */
        public GeneLootTableData getLootTable() {
            return lootTable;
        }

        /**
         * 获取抽取结果列表（可修改）
         */
        public List<ResourceLocation> getResults() {
            return results;
        }

        /**
         * 添加结果
         */
        public void addResult(ResourceLocation id) {
            results.add(id);
        }

        /**
         * 移除结果
         */
        public void removeResult(ResourceLocation id) {
            results.remove(id);
        }

        /**
         * 清空结果
         */
        public void clearResults() {
            results.clear();
        }
    }

    /**
     * 抽取完成事件
     * <p>
     * 在抽取完全完成后触发，只读，无法修改任何数据
     */
    public static class Post extends LootRollEvent {
        private final List<ResourceLocation> results;
        private final GeneLootTableData lootTable;

        public Post(Player player, GeneLootTableData lootTable, List<ResourceLocation> results) {
            super(player);
            this.lootTable = lootTable;
            this.results = List.copyOf(results); // 不可变副本
        }

        /**
         * 获取战利品表
         */
        public GeneLootTableData getLootTable() {
            return lootTable;
        }

        /**
         * 获取抽取结果列表（只读）
         */
        public List<ResourceLocation> getResults() {
            return results;
        }
    }
}
