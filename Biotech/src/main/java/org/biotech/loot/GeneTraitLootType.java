package org.biotech.loot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.api.system.gene.GeneRegistryHolder;
import org.biotech.api.system.gene.core.manager.IGeneInventoryManager;
import org.biotech.api.init.*;
import org.galaxylib.api.system.loot.core.ILootType;
import org.biotech.api.system.trait.core.ITrait;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.data.LootEntryDefinition;
import org.biotech.component.TraitComp;

import java.util.Optional;

public class GeneTraitLootType implements ILootType<ITrait> {

    public record ClaimResult(IGeneInventoryManager.AddResult addResult, ItemStack stack, int traitCount) {}

    @Override
    public Optional<ITrait> resolve(LootEntryDefinition entry, LootManager.Context context) {
        return Optional.ofNullable(BiotechTraitInit.getTraitById(entry.id()));
    }

    // 使用 GeneRegistryHolder 延迟获取 EMPTY 基因，避免注册时空指针
    private static final GeneRegistryHolder EMPTY_GENE_HOLDER = new GeneRegistryHolder(BiotechGeneInit.EMPTY_GENE);

    public ClaimResult claimStackToPlayerAndReturn(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return new ClaimResult(IGeneInventoryManager.AddResult.EMPTY_ITEM, ItemStack.EMPTY, 0);
        }
        var manager = BiotechAPI.getGeneInventoryManager(player);
        TraitComp comp = stack.get(BiotechDataComponentInit.TRAIT_COMP.get());
        int traitCount = comp == null ? 0 : comp.size();
        return new ClaimResult(manager.add(stack), stack.copy(), traitCount);
    }

    public ClaimResult claimResultsToPlayerAndReturn(ServerPlayer player, LootManager.Result<?> lootResult) {
        ItemStack stack = lootResult.stack();
        if (stack.isEmpty()) {
            return new ClaimResult(IGeneInventoryManager.AddResult.EMPTY_ITEM, ItemStack.EMPTY, 0);
        }
        var manager = BiotechAPI.getGeneInventoryManager(player);
        return new ClaimResult(manager.add(stack), stack.copy(), lootResult.values().size());
    }

    @Override
    public LootManager.ClaimResult claim(ServerPlayer player, ItemStack stack, LootManager.Context context) {
        ClaimResult result = claimStackToPlayerAndReturn(player, stack);
        return result.addResult().isSuccess() ? LootManager.ClaimResult.success(result.stack()) : LootManager.ClaimResult.empty();
    }

    @Override
    public int getPoolCount(ServerPlayer player) {
        double value = player.getAttributes().getInstance(BiotechAttributeInit.GENE_TRAIT_ROLL_COUNT).getValue();
        return ILootType.getPoolCountFromAttribute((float) value, RandomSource.create());
    }

    /**
     * 将抽取结果转换为 GeneItem ItemStack
     * 收集所有词条到 TraitComp，然后创建带 GENE_INSTANCE 组件的 GeneItem
     */
    @Override
    public ItemStack createStack(LootManager.Bundle<ITrait> bundle, LootManager.Context context) {
        TraitComp comp = TraitComp.empty();
        bundle.values().forEach(value -> comp.addTrait(value.value()));

        if (!comp.isEmpty()) {
            GeneInstance instance = EMPTY_GENE_HOLDER.getInstance();
            instance.set(BiotechDataComponentInit.TRAIT_COMP.get(), comp);

            ItemStack stack = new ItemStack(BiotechItemInit.GENE_ITEM.get());
            stack.set(BiotechDataComponentInit.GENE_INSTANCE.get(), instance);
            return stack;
        }

        return ItemStack.EMPTY;
    }

}
