package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.resources.ResourceLocation;

/**
 * Phase 标记接口 —— 纯标识符载体，所有行为已迁移到 RogueCap。
 */
public interface Phase {

    ResourceLocation getId();
}
