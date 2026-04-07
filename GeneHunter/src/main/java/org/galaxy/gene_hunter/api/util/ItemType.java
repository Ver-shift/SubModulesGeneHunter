package org.galaxy.gene_hunter.api.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class ItemType {


    public static boolean isWeapon(ItemStack stack) {
        // 检查属性修饰符中是否有攻击伤害
        var modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            return modifiers.modifiers().stream()
                    .anyMatch(modifier -> modifier.attribute().equals(Attributes.ATTACK_DAMAGE));
        }

        return false;
    }


}
