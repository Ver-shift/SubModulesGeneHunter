package com.pz.beyond.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 玩家正在锁定的节点区域内，
 *
 */
public class NodeEffectLock extends MobEffect {
    protected NodeEffectLock(MobEffectCategory category, int color) {
        super(category, color);
    }
}
