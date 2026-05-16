package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.rogue.RogueContext;

public interface IRoguePhase {

    default void enter(ServerLevel level, RogueContext ctx) {}

    default void tick(ServerLevel level, RogueContext ctx) {}

    default void exit(ServerLevel level, RogueContext ctx) {}
}
