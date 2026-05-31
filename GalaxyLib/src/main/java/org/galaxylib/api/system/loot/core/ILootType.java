package org.galaxylib.api.system.loot.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.galaxylib.api.init.GalaxyLibAttributeInit;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;

import java.util.Optional;

public interface ILootType<T> {

    Optional<T> resolve(LootEntryDefinition entry, LootManager.Context context);

    default ItemStack createStack(LootManager.Bundle<T> bundle, LootManager.Context context) {
        return bundle.first()
                .map(value -> value.value() instanceof ItemStack stack ? stack : ItemStack.EMPTY)
                .orElse(ItemStack.EMPTY);
    }

    default LootManager.ClaimResult claim(ServerPlayer player, ItemStack stack, LootManager.Context context) {
        if (stack.isEmpty()) {
            return LootManager.ClaimResult.empty();
        }
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(stack);
        }
        return LootManager.ClaimResult.success(stack);
    }


    /**
     * 可以给词条 当成多词条用，
     * @param player
     * @return
     */
    default int getPoolCount(ServerPlayer player){
        double value = player.getAttributes().getInstance(GalaxyLibAttributeInit.ITEM_ROLL_COUNT).getValue();

        return getPoolCountFromAttribute((float) value,RandomSource.create());
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
