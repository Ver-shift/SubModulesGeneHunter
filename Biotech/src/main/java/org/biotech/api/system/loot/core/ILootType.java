package org.biotech.api.system.loot.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.init.AttributeInit;

public interface ILootType<T> {


    String getName();

    /**
     * 从ld 里面获取想要的东西，例如物品，词条，
     * @param lootId
     * @return
     */
    T getLoot(ResourceLocation lootId,int count);

    default int getPoolCount(ServerPlayer player){
        double value = player.getAttributes().getInstance(AttributeInit.ITEM_ROLL_COUNT).getValue();

        return getPoolCountFromAttribute((float) value,RandomSource.create());
    }


    /**
     * 将战利品发放给玩家
     * @param player 玩家
     * @param lootResult 抽取结果
     */
    default void claimResultsToPlayer(ServerPlayer player, ILootTableManager.LootResult lootResult) {
        // 遍历所有抽中的条目
        for (var entry : lootResult.result()) {
            // 通过 lootType 获取实际的战利品对象
            Object loot = lootResult.lootType().getLoot(entry.getId(), entry.getCount());

            if (loot instanceof ItemStack stack) {
                // 添加到玩家背包，如果背包满了则掉落在地上
                if (!player.getInventory().add(stack)) {
                    player.spawnAtLocation(stack);
                }
            }
        }
    }
    
    /**
     * 根据属性值计算抽取次数
     * <p>
     * 算法说明：
     * <ul>
     *   <li>整数部分：固定抽取次数</li>
     *   <li>小数部分：额外抽取的概率（0.0 ~ 0.99）</li>
     * </ul>
     * <p>
     * 示例：
     * <ul>
     *   <li>1.0 → 固定抽 1 次</li>
     *   <li>1.35 → 固定抽 1 次 + 35% 概率额外抽 1 次</li>
     *   <li>2.0 → 固定抽 2 次</li>
     *   <li>2.35 → 固定抽 2 次 + 35% 概率额外抽 1 次</li>
     * </ul>
     *
     * @param attributeValue 属性值
     * @param random 随机数源
     * @return 抽取次数
     */
    static int getPoolCountFromAttribute(float attributeValue, RandomSource random) {
        // 整数部分：固定抽取次数
        int baseCount = (int) attributeValue;
        
        // 小数部分：额外抽取的概率
        float extraChance = attributeValue - baseCount;
        
        // 根据概率判断是否额外抽取
        if (extraChance > 0 && random.nextFloat() < extraChance) {
            baseCount++;
        }
        
        return Math.max(1, baseCount);  // 至少抽取 1 次
    }

}
