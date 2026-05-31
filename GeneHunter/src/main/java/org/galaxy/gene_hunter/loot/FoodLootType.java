package org.galaxy.gene_hunter.loot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;

import java.util.Optional;

/**
 * 食物战利品类型 - 只处理有 FOOD 组件的物品
 */
public class FoodLootType implements ILootType<ItemStack> {
    
    @Override
    public Optional<ItemStack> resolve(LootEntryDefinition entry, LootManager.Context context) {
        var item = BuiltInRegistries.ITEM.get(entry.id());
        
        // 检查物品是否存在
        if (item == Items.AIR) {
            return Optional.empty();
        }
        
        // 创建物品栈
        ItemStack stack = new ItemStack(item, entry.count());
        
        // 检测是否是食物
        if (stack.has(DataComponents.FOOD)) {
            return Optional.of(stack);
        }
        
        return Optional.empty();
    }




}
