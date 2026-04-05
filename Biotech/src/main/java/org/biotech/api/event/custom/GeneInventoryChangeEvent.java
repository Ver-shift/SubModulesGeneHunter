package org.biotech.api.event.custom;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.biotech.item.GeneItem;

/**
 * 基因背包变更事件
 * 包含四种变更类型：Equip(装备)、Inventory(背包)、Favorite(收藏)、MergeInput(合并输入)
 * 每种类型都有 Pre(前置) 和 Post(后置) 事件
 */
public abstract class GeneInventoryChangeEvent extends PlayerEvent {
    
    protected final GeneItem item;
    protected final int slotIndex;
    
    public GeneInventoryChangeEvent(Player player, GeneItem item, int slotIndex) {
        super(player);
        this.item = item;
        this.slotIndex = slotIndex;
    }
    
    public GeneItem getItem() {
        return item;
    }
    
    public int getSlotIndex() {
        return slotIndex;
    }
    
    // ========================= Equip 装备事件 =========================
    
    /** 装备基因事件 */
    public static class Equip extends GeneInventoryChangeEvent {
        public Equip(Player player, GeneItem item, int slotIndex) {
            super(player, item, slotIndex);
        }
        
        /** 装备前 - 可取消 */
        public static class Pre extends Equip implements ICancellableEvent {
            public Pre(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
        
        /** 装备后 */
        public static class Post extends Equip {
            public Post(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
    }
    
    // ========================= Inventory 背包事件 =========================
    
    /** 背包变更事件 */
    public static class Inventory extends GeneInventoryChangeEvent {
        public Inventory(Player player, GeneItem item, int slotIndex) {
            super(player, item, slotIndex);
        }
        
        /** 背包变更前 - 可取消 */
        public static class Pre extends Inventory implements ICancellableEvent {
            public Pre(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
        
        /** 背包变更后 */
        public static class Post extends Inventory {
            public Post(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
    }
    
    // ========================= Favorite 收藏事件 =========================
    
    /** 收藏变更事件 */
    public static class Favorite extends GeneInventoryChangeEvent {
        public Favorite(Player player, GeneItem item, int slotIndex) {
            super(player, item, slotIndex);
        }
        
        /** 收藏变更前 - 可取消 */
        public static class Pre extends Favorite implements ICancellableEvent {
            public Pre(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
        
        /** 收藏变更后 */
        public static class Post extends Favorite {
            public Post(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
    }
    
    // ========================= MergeInput 合并输入事件 =========================
    
    /** 合并输入事件 */
    public static class MergeInput extends GeneInventoryChangeEvent {
        public MergeInput(Player player, GeneItem item, int slotIndex) {
            super(player, item, slotIndex);
        }
        
        /** 合并输入前 - 可取消 */
        public static class Pre extends MergeInput implements ICancellableEvent {
            public Pre(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
        
        /** 合并输入后 */
        public static class Post extends MergeInput {
            public Post(Player player, GeneItem item, int slotIndex) {
                super(player, item, slotIndex);
            }
        }
    }
}
