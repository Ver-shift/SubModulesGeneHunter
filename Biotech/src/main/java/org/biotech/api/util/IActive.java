package org.biotech.api.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.biotech.api.system.gene.GeneContext;

/**
 * 玩家的活动
 */
public interface IActive {


    default void tick(GeneContext context) {
    }

    default void tick(GeneContext context, int traitCount) {
        tick(context);
    }

    default void attack(GeneContext context, Entity target) {
    }

    default void attack(GeneContext context, Entity target, int traitCount) {
        attack(context, target);
    }

    default void jump(GeneContext context) {
    }

    default void jump(GeneContext context, int traitCount) {
        jump(context);
    }

    default void modifyAttributes(Player player) {

    }

    default void modifyAttributes(Player player, int traitCount) {
        modifyAttributes(player);
    }

    default void removeAttributes(Player player) {

    }
}
