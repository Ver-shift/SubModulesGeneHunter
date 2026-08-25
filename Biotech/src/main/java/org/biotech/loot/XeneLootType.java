package org.biotech.loot;

import net.minecraft.world.item.ItemStack;
import org.biotech.api.init.BiotechGeneInit;
import org.biotech.item.xene.XeneItem;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;

import java.util.Optional;

/** Resolves a loot entry to a datapack-defined Xene item. */
public class XeneLootType implements ILootType<ItemStack> {
    @Override
    public Optional<ItemStack> resolve(LootEntryDefinition entry, LootManager.Context context) {
        var xene = BiotechGeneInit.getXene(context.level().registryAccess(), entry.id());
        if (xene.isEmpty()) return Optional.empty();

        ItemStack stack = XeneItem.createForXene(entry.id(), xene.get());
        stack.setCount(entry.count());
        return Optional.of(stack);
    }
}
