package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;

public interface ITransition {

    boolean isSatisfied(ServerLevel level, RogueContext ctx);

    default void onTransition(ServerLevel level, RogueContext ctx) {}
}
