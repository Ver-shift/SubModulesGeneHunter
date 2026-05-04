package org.galaxy.beyond.api.system.rogue;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueListener;

public class RogueManager implements IRogueManager {



    @Override
    public void addExtension(RogueState rogueState, IRogueListener extension) {

    }

    @Override
    public void removeExtension(RogueState rogueState, IRogueListener extension) {
    }

    @Override
    public void clearExtensions(RogueState rogueState) {
    }



    @Override
    public void setRogueLevel(ResourceKey<Level> level) {

    }

    @Override
    public void tick(ServerLevel level) {
        // TODO: 框架占位
    }

    @Override
    public void tryStratRogue(ServerLevel level) {
        // TODO: 框架占位
    }

    @Override
    public void startRogue(ServerLevel level) {

    }

    @Override
    public void tryStratNode(ServerLevel level) {
        // TODO: 框架占位
    }

    @Override
    public void tryFinishRogue(ServerLevel level) {
        // TODO: 框架占位
    }
}
