package org.galaxy.gene_hunter.api.system.choice.core;


import net.minecraft.world.item.ItemStack;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.gene_hunter.api.system.choice.ChoiceStage;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.List;

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

    void doRoll(ILootType<?> lootType, NodeColor nodeColor);

    /**
     * 在服务端进行roll操作
     * 目前支持
     * 武器：
     * 局外基因：
     * @param lootType
     */
    void startRoll(ILootType<?> lootType);

    void startRoll(ILootType<?> lootType, NodeColor nodeColor);

    void startStages(List<ChoiceStage> stages);

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
