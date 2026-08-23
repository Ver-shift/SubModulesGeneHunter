package org.biotech.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.biotech.api.init.BiotechDataComponentInit;
import org.biotech.api.system.gene.GeneDefinition;
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.api.util.TooltipUtil;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Optional;

/** Common item-stack behaviour for Gene and Xene datapack definitions. */
public abstract class DefinedGeneItem extends Item implements ICurioItem {
    private final String translationPrefix;

    protected DefinedGeneItem(String translationPrefix) {
        super(new Properties().stacksTo(1).component(BiotechDataComponentInit.GENE_INSTANCE.get(), GeneInstance.EMPTY));
        this.translationPrefix = translationPrefix;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return stack.getOrDefault(DataComponents.ITEM_NAME, super.getName(stack));
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        // Curios' generic slot-type text would expose the internal +1 Xene-slot
        // modifier.  Only the actual equipped attribute effects should be shown.
        return TooltipUtil.suppressCuriosSlotTypeTooltip(tooltips, context, stack);
    }

    /** Returns whether this stack was created from a registered datapack definition. */
    protected final boolean hasDefinition(ItemStack stack) {
        GeneInstance instance = stack.get(BiotechDataComponentInit.GENE_INSTANCE.get());
        return instance != null && !instance.isEmpty();
    }

    protected ItemStack createDefinedStack(ResourceLocation id, GeneDefinition definition) {
        ItemStack stack = new ItemStack(this);
        stack.set(BiotechDataComponentInit.GENE_INSTANCE.get(), new GeneInstance(id));
        stack.set(DataComponents.RARITY, definition.rarity());
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, definition.attributes());
        stack.set(DataComponents.ITEM_NAME,
                Component.translatable(translationPrefix + "." + id.getNamespace() + "." + id.getPath() + ".name"));
        stack.set(BiotechDataComponentInit.GENE_DESCRIPTION.get(), definition.description());
        return stack;
    }
}
