package com.pz.beyond.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 玩家在已经解锁过的节点内部
 */
public class NodeEffectUnlock extends MobEffect {
    protected NodeEffectUnlock(MobEffectCategory category, int color) {
        super(category, color);
    }
}
