package org.biotech.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.api.system.gene.core.IGene;
import org.biotech.api.system.gene.core.IGeneItem;
import org.biotech.api.init.DataComponentInit;
import org.biotech.api.init.ItemInit;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.system.trait.core.ITraitProvider;
import org.biotech.api.util.TooltipUtil;
import org.biotech.component.TraitComp;
import org.biotech.gene.EmptyGene;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * 异种核心物品 - 局内基因，可装备到饰品栏
 */
public class GeneItem extends Item implements IGeneItem<GeneItem>, ITraitProvider {

    public GeneItem() {
        super(new Properties()
                .stacksTo(1)
                .component(DataComponentInit.GENE_INSTANCE.get(), GeneInstance.EMPTY)
        );
    }

    @Override
    public IGene asGene() {
        return Objects.requireNonNull(getGeneInstance().getGene());
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        IGene gene = getGeneFromStack(stack);
        if (gene == null) {
            return styleNameByTraits(stack, super.getName(stack));
        }

        if (gene instanceof EmptyGene) {
            List<ITrait> traits = getTraitsFromStack(stack);
            if (!traits.isEmpty() && traits.getFirst() != null) {
                return styleNameByTraits(stack, traits.getFirst().getDisplayName());
            }
            return styleNameByTraits(stack, super.getName(stack));
        }

        return styleNameByTraits(stack, gene.getDisplayName());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        // Intentionally left empty: trait details are rendered by getTooltipImage() only.
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        return getTraitTooltipImage(stack);
    }

    @Override
    public TraitComp readTraitComp(ItemStack stack) {
        GeneInstance instance = stack.get(DataComponentInit.GENE_INSTANCE.get());
        if (instance == null) {
            return null;
        }
        return instance.getComponents().get(DataComponentInit.TRAIT_COMP.get());
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
        return TooltipUtil.suppressCuriosSlotTypeTooltip(tooltips, context, stack);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
        // GeneItem 不显示 Curios 生成的属性修饰 tooltip。
        return List.of();
    }

    public GeneInstance getGeneInstance() {
        return Objects.requireNonNull(this.components().get(DataComponentInit.GENE_INSTANCE.get()));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        IGene gene = getGeneFromStack(stack);
        if (gene == null) {
            return;
        }

        gene.curioTick(slotContext, stack);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        IGene gene = getGeneFromStack(stack);
        if (gene == null) {
            return;
        }

        gene.onEquip(slotContext, prevStack, stack);
    }



    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        IGene gene = getGeneFromStack(stack);
        if (gene == null) {
            return;
        }

        gene.onUnequip(slotContext, newStack, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        // 无效基因实例不可装备，避免空指针
        IGene gene = getGeneFromStack(stack);
        return gene != null && gene.canEquip(slotContext, stack);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        // 无效基因实例允许卸下，避免卡槽
        IGene gene = getGeneFromStack(stack);
        return gene == null || gene.canUnequip(slotContext, stack);
    }
    

    public IGene getGene(){
        return getGeneInstance().getGene();
    }

    private IGene getGeneFromStack(ItemStack stack) {
        GeneInstance instance = stack.get(DataComponentInit.GENE_INSTANCE.get());
        return instance != null ? instance.getGene() : null;
    }

    /**
     * 创建一个包含特定基因的 GeneItem ItemStack
     * 稀有度与基因稀有度相同
     */
    public static ItemStack createForGene(IGene gene) {
        ItemStack stack = new ItemStack(ItemInit.GENE_ITEM.get());
        GeneInstance instance = new GeneInstance(gene);
        stack.set(DataComponentInit.GENE_INSTANCE.get(), instance);
        
        // 设置物品稀有度与基因稀有度相同
        // 从 GeneInstance 的组件中获取稀有度
        Rarity rarity = instance.getComponents().get(DataComponents.RARITY);
        if (rarity != null) {
            stack.set(DataComponents.RARITY, rarity);
        }
        
        return stack;
    }
    
    /**
     * 向 GeneItem 的 GENE_INSTANCE 中添加词条
     * @param stack GeneItem 的 ItemStack
     * @param trait 要添加的词条
     * @return 是否添加成功
     */
    public static boolean addTraitToGene(ItemStack stack, ITrait trait) {
        // 检查是否是 GeneItem
        if (!(stack.getItem() instanceof GeneItem)) {
            return false;
        }
        
        // 获取当前的 GeneInstance
        GeneInstance instance = stack.get(DataComponentInit.GENE_INSTANCE.get());
        if (instance == null) {
            return false;
        }
        
        // 获取或创建 TraitComp
        TraitComp comp = instance.getComponents().get(DataComponentInit.TRAIT_COMP.get());
        if (comp == null) {
            comp = TraitComp.empty();
        }
        
        // 添加词条（ TraitComp 会检查重复）
        comp.addTrait(trait);
        
        // 将 TraitComp 设置回 GeneInstance
        instance.set(DataComponentInit.TRAIT_COMP.get(), comp);
        
        // 更新 ItemStack 的 GeneInstance
        stack.set(DataComponentInit.GENE_INSTANCE.get(), instance);
        
        return true;
    }
    
}
