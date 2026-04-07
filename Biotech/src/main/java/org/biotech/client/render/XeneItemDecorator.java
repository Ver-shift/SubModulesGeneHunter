package org.biotech.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import org.biotech.api.system.trait.core.IStackTraitAccess;
import org.biotech.item.xene.XeneItem;

/**
 * Replaces the GUI icon rendering of XeneItem with the selected trait texture.
 */
public class XeneItemDecorator implements IItemDecorator {

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof XeneItem)) {
            return false;
        }

        ResourceLocation texture = IStackTraitAccess.getSelectedTraitTexture(stack);
        if (texture == null) {
            return false;
        }

        guiGraphics.blit(texture, xOffset, yOffset, 0, 0, 16, 16, 16, 16);
        return false;
    }
}

