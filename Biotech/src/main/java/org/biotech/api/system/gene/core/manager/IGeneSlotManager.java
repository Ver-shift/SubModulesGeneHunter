package org.biotech.api.system.gene.core.manager;

/**
 * 动态控制槽位的多少和大小
 */
public interface IGeneSlotManager {
    //========================控制槽位大小=======================

    /**
     * 添加一页 玩家的inventory page
     * @param pageCount 添加的page 数量
     * @return 是否成功添加
     */
    boolean addInventoryPage(int pageCount);
    boolean removeInventoryPage(int pageCount);

    /**
     * 添加一页 玩家的favorite page
     * @param pageCount 添加的page数量
     * @return 是否成功添加
     */
    boolean addFavoritePage(int pageCount);
    boolean removeFavoritePage(int pageCount);
//    ItemLike
    /**
     * 添加一个玩家装备槽位
     * @param slotCount
     * @return
     */
    boolean addEquippedSlot(int slotCount);
    boolean removeEquippedSlot(int slotCount);

    boolean addMergeSlot(int slotCount);
    boolean removeMergeSlot(int slotCount);
}
