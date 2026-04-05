package org.biotech.api.event.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.biotech.item.GeneItem;

/**
 * 基因鉴定事件
 * 有两种鉴定类型：
 * - Item: 输入是 Rarity（稀有度）
 * - Merge: 输入是词条数量
 * 
 * 此事件不可取消，但监听器可以修改输出结果
 */
public abstract class GeneIdentifyEvent extends PlayerEvent {
    
    protected GeneItem output;  // 输出：鉴定结果（可修改）
    
    public GeneIdentifyEvent(Player player, GeneItem output) {
        super(player);
        this.output = output;
    }
    
    /**
     * 获取当前输出结果
     */
    public GeneItem getOutput() {
        return output;
    }
    
    /**
     * 设置输出结果（允许监听器修改）
     */
    public void setOutput(GeneItem output) {
        this.output = output;
    }
    
    /**
     * 获取鉴定类型
     */
    public abstract IdentifierType getType();
    
    // ========================= Item 鉴定（输入：Rarity） =========================
    
    /**
     * 物品鉴定事件 - 输入是稀有度
     */
    public static class Item extends GeneIdentifyEvent {
        private final Rarity rarity;  // 输入：稀有度
        
        public Item(Player player, Rarity rarity, GeneItem output) {
            super(player, output);
            this.rarity = rarity;
        }
        
        @Override
        public IdentifierType getType() {
            return IdentifierType.ITEM;
        }
        
        /**
         * 获取输入的稀有度
         */
        public Rarity getRarity() {
            return rarity;
        }
    }
    
    // ========================= Merge 鉴定（输入：词条数量） =========================
    
    /**
     * 合并鉴定事件 - 输入是词条数量
     */
    public static class Merge extends GeneIdentifyEvent {
        private final int entryCount;  // 输入：词条数量
        
        public Merge(Player player, int entryCount, GeneItem output) {
            super(player, output);
            this.entryCount = entryCount;
        }
        
        @Override
        public IdentifierType getType() {
            return IdentifierType.MERGE;
        }
        
        /**
         * 获取输入的词条数量
         */
        public int getEntryCount() {
            return entryCount;
        }
    }
    
    // ========================= 鉴定类型枚举 =========================
    
    /**
     * 鉴定类型
     */
    public enum IdentifierType {
        /** 物品鉴定 - 输入是 Rarity */
        ITEM,
        /** 合并鉴定 - 输入是词条数量 */
        MERGE
    }
}
