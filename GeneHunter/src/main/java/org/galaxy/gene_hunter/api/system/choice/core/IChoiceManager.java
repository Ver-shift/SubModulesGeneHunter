package org.galaxy.gene_hunter.api.system.choice.core;


import net.minecraft.world.item.ItemStack;
import org.galaxylib.api.system.loot.core.ILootType;

public interface IChoiceManager {


    /**
     * 打开基因选择界面
     */
    boolean openChoiceMenu();

    int getChoiceCount();

    void setChoiceCount(int choice);

    /**
     * 进行holder数据刷新
     * @param lootType
     */
    void doRoll(ILootType<?> lootType);

    /**
     * 在服务端进行roll操作
     * 目前支持
     * 武器：
     * 局外基因：
     * @param lootType
     */
    void startRoll(ILootType<?> lootType);

    /**
     *
     * 结束roll。让无法刷新
     * @param claimedStack 玩家领取的物品
     */
    void endRoll(ItemStack claimedStack);

    /**
     * 对当前的holderData进行刷新
     */
    void refresh();


}
