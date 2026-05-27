package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;

/**
 * 结算标记 Cap —— 不包含具体逻辑，实际结算流程由 {@link PlayerProgressFinishCap} 处理。
 */
public class RogueProgressFinishCap extends RogueCap {

    public static final ResourceLocation ID = Beyond.asResource("rogue_progress_finish");

    public RogueProgressFinishCap() { super(ID); }
}
