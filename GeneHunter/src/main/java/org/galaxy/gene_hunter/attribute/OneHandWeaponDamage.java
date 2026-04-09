package org.galaxy.gene_hunter.attribute;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 单手武器属性，就是单纯的增加伤害。不过人为分配的时候是放到单手武器上面
 */
public class OneHandWeaponDamage extends Attribute implements IOneHandWeaponAttribute{

    protected OneHandWeaponDamage(String descriptionId, double defaultValue) {
        super(descriptionId, defaultValue);
    }

//    UIElement

}
