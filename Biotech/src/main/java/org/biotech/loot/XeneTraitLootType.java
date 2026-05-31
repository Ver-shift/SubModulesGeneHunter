package org.biotech.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.BiotechAttributeInit;
import org.biotech.api.init.BiotechDataComponentInit;
import org.biotech.api.init.BiotechItemInit;
import org.biotech.api.init.BiotechTraitInit;
import org.galaxylib.api.system.loot.core.ILootType;
import org.biotech.api.system.trait.core.ITrait;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;
import org.biotech.component.TraitComp;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

/**
 * 效果词条类型
 */
public class XeneTraitLootType implements ILootType<ITrait> {

    @Override
    public Optional<ITrait> resolve(LootEntryDefinition entry, LootManager.Context context) {
        return Optional.ofNullable(BiotechTraitInit.getTraitById(entry.id()));
    }

    @Override
    public LootManager.ClaimResult claim(ServerPlayer player, ItemStack stack, LootManager.Context context) {
        if (stack.isEmpty()) {
            return LootManager.ClaimResult.empty();
        }
        claimStackToPlayer(player, stack);
        return LootManager.ClaimResult.success(stack);
    }

    private void claimStackToPlayer(ServerPlayer player, ItemStack stack) {
        IDynamicStackHandler slotHandler = BiotechAPI.getXeneEquipSlots(player);
        if (slotHandler == null) {
            return;
        }

        for (int i = 0; i < slotHandler.getSlots(); i++) {
            if (slotHandler.getStackInSlot(i).isEmpty()) {
                slotHandler.setStackInSlot(i, stack);
                break;
            }
        }
    }

    /**
     * 将结果转移成物品stack
     */
    @Override
    public ItemStack createStack(LootManager.Bundle<ITrait> bundle, LootManager.Context context) {
        TraitComp comp = TraitComp.empty();
        bundle.values().forEach(value -> comp.addTrait(value.value()));

        if (!comp.isEmpty()) {
            ItemStack xeneStack = new ItemStack(BiotechItemInit.XENE_ITEM.get());
            xeneStack.set(BiotechDataComponentInit.TRAIT_COMP.get(), comp);
            return xeneStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int getPoolCount(ServerPlayer player) {
        double value = player.getAttributes().getInstance(BiotechAttributeInit.XENE_TRAIT_ROLL_COUNT).getValue();
        return ILootType.getPoolCountFromAttribute((float) value, RandomSource.create());
    }
}
