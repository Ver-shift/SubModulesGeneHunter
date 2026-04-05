package org.biotech.api.util;

import net.minecraft.world.entity.Entity;
import org.biotech.api.system.gene.GeneContext;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

/**
 * 玩家的活动
 */
public interface IActive {


    default void tick(GeneContext context){}

    default void attack(GeneContext context, Entity target) {}

    default void jump(GeneContext context) {}

    default void modifyAttributes(CurioAttributeModifierEvent event){

    }
}
