package org.galaxy.gene_hunter.loot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.galaxylib.api.system.loot.core.ILootType;

/**
 * 食物战利品类型 - 只处理有 FOOD 组件的物品
 */
public class FoodLootType implements ILootType<ItemStack> {
    
    @Override
    public String getName() {
        return "food";
    }

    @Override
    public ItemStack getLoot(ResourceLocation lootId, int count) {
        // 从注册表获取物品
        var item = BuiltInRegistries.ITEM.get(lootId);
        
        // 检查物品是否存在
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        
        // 创建物品栈
        ItemStack stack = new ItemStack(item, count);
        
        // 检测是否是食物
        if (stack.has(DataComponents.FOOD)) {
            return stack;
        }
        
        return ItemStack.EMPTY;
    }




}
