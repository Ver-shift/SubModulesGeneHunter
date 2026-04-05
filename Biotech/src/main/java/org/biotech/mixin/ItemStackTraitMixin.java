package org.biotech.mixin;

import net.minecraft.world.item.ItemStack;
import org.biotech.api.init.DataComponentInit;
import org.biotech.api.system.gene.GeneInstance;
import org.biotech.api.system.trait.core.IStackTraitAccess;
import org.biotech.component.TraitComp;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackTraitMixin implements IStackTraitAccess {

    @Override
    @Nullable
    public TraitComp biotech$getTraitComp() {
        ItemStack stack = (ItemStack) (Object) this;

        GeneInstance instance = stack.get(DataComponentInit.GENE_INSTANCE.get());
        if (instance != null) {
            TraitComp geneComp = instance.getComponents().get(DataComponentInit.TRAIT_COMP.get());
            if (geneComp != null) {
                return geneComp;
            }
        }

        return stack.get(DataComponentInit.TRAIT_COMP.get());
    }
}

