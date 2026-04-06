package org.biotech.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.init.BiotechGeneInit;
import org.galaxylib.api.system.loot.core.ILootType;
import org.biotech.item.GeneItem;

/**
 * 基因战利品类型 - 返回 GeneItem ItemStack
 */
public class GeneLootType implements ILootType<ItemStack> {
    
    @Override
    public String getName() {
        return "gene";
    }

    @Override
    public ItemStack getLoot(ResourceLocation lootId, int count) {
        // 从注册表获取基因
        var gene = BiotechGeneInit.getGeneById(lootId);
        
        // 检查基因是否存在（空基因检查）
        if (gene == null || gene == BiotechGeneInit.EMPTY) {
            return ItemStack.EMPTY;
        }
        
        // 创建 GeneItem ItemStack
        ItemStack stack = GeneItem.createForGene(gene);
        stack.setCount(count);
        return stack;
    }


}
