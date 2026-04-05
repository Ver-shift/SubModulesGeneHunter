package org.biotech.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import org.biotech.component.TraitComp;
import org.biotech.item.GeneItem;

/**
 * Renders a roman numeral badge for GeneItem when it has more than one trait.
 */
public class GeneItemDecorator implements IItemDecorator {

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof GeneItem geneItem)) {
            return false;
        }

        TraitComp comp = geneItem.readTraitComp(stack);
        int traitCount = comp != null ? comp.size() : 0;
        if (traitCount <= 1) {
            return false;
        }

        String roman = toRoman(traitCount);
        int textWidth = font.width(roman);

        // Bottom-right badge inside the 16x16 item icon bounds.
        int textX = xOffset + 17 - textWidth;
        int textY = yOffset + 9;
        guiGraphics.drawString(font, roman, textX, textY, 0xFFE08A, true);
        return false;
    }

    private static String toRoman(int value) {
        if (value <= 0) {
            return "";
        }

        int[] numbers = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder result = new StringBuilder();
        int remaining = value;

        for (int i = 0; i < numbers.length && remaining > 0; i++) {
            while (remaining >= numbers[i]) {
                result.append(symbols[i]);
                remaining -= numbers[i];
            }
        }
        return result.toString();
    }
}

