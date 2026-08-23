package org.biotech.loot;

import net.minecraft.world.item.ItemStack;
import org.biotech.api.init.BiotechGeneInit;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.biotech.item.GeneItem;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;

import java.util.Optional;

/**
 * 基因战利品类型 - 返回 GeneItem ItemStack
 */
public class GeneLootType implements ILootType<ItemStack> {
    
    @Override
    public Optional<ItemStack> resolve(LootEntryDefinition entry, LootManager.Context context) {
        var gene = BiotechGeneInit.getGene(context.level().registryAccess(), entry.id());
        if (gene.isEmpty()) return Optional.empty();

        ItemStack stack = GeneItem.createForGene(entry.id(), gene.get());
        stack.setCount(entry.count());
        return Optional.of(stack);
    }


}
