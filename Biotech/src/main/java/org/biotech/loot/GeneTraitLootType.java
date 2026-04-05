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
import org.biotech.api.system.loot.core.ILootTableManager;
import org.biotech.api.system.loot.core.ILootType;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.component.TraitComp;

public class GeneTraitLootType implements ILootType<ITrait> {

    public record ClaimResult(IGeneInventoryManager.AddResult addResult, ItemStack stack, int traitCount) {}

    @Override
    public String getName() {
        return "gene_trait";
    }

    @Override
    public ITrait getLoot(ResourceLocation lootId, int count) {
        return TraitInit.getTraitById(lootId);
    }

    // 使用 GeneRegistryHolder 延迟获取 EMPTY 基因，避免注册时空指针
    private static final GeneRegistryHolder EMPTY_GENE_HOLDER = new GeneRegistryHolder(GeneInit.EMPTY_GENE);

    @Override
    public void claimResultsToPlayer(ServerPlayer player, ILootTableManager.LootResult lootResult) {
        claimResultsToPlayerAndReturn(player, lootResult);
    }

    public ClaimResult claimResultsToPlayerAndReturn(ServerPlayer player, ILootTableManager.LootResult lootResult) {
        // 创建 TraitComp 并收集所有词条
        TraitComp comp = TraitComp.empty();
        for (var entry : lootResult.result()) {
            ITrait trait = getLoot(entry.getId(), entry.getCount());
            if (trait != null) {
                comp.addTrait(trait);
            }
        }

        // 如果收集到了词条，创建 GeneItem 并添加到基因背包
        if (comp.isEmpty()) {
            return new ClaimResult(IGeneInventoryManager.AddResult.EMPTY_ITEM, ItemStack.EMPTY, 0);
        }

        var manager = BiotechAPI.getGeneInventoryManager(player);

        // 使用 GeneRegistryHolder 延迟获取 GeneInstance，避免注册时空指针
        GeneInstance instance = EMPTY_GENE_HOLDER.getInstance();

        // 将 TraitComp 放入 GeneInstance 的组件中
        instance.set(DataComponentInit.TRAIT_COMP.get(), comp);

        // 创建 ItemStack 并设置 GENE_INSTANCE 组件
        ItemStack stack = new ItemStack(ItemInit.GENE_ITEM.get());
        stack.set(DataComponentInit.GENE_INSTANCE.get(), instance);

        return new ClaimResult(manager.add(stack), stack.copy(), comp.size());
    }

    @Override
    public int getPoolCount(ServerPlayer player) {
        double value = player.getAttributes().getInstance(AttributeInit.GENE_TRAIT_ROLL_COUNT).getValue();
        return ILootType.getPoolCountFromAttribute((float) value, RandomSource.create());
    }
}
