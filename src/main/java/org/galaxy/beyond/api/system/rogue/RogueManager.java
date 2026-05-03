package org.galaxy.beyond.api.system.rogue;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.system.rogue.core.IRougeManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueExtension;
import org.galaxy.beyond.api.system.rogue.event.RogueStartEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class RogueManager implements IRougeManager {

    private final Map<RogueState, List<IRogueExtension>> extensions = new EnumMap<>(RogueState.class);
    private RogueState currentState = RogueState.LOBBY;

    @Override
    public void addExtension(RogueState rogueState, IRogueExtension extension) {
        extensions.computeIfAbsent(rogueState, k -> new CopyOnWriteArrayList<>()).add(extension);
    }

    @Override
    public void removeExtension(RogueState rogueState, IRogueExtension extension) {
        extensions.getOrDefault(rogueState, List.of()).remove(extension);
    }

    @Override
    public void clearExtensions(RogueState rogueState) {
        extensions.remove(rogueState);
    }

    public void setState(RogueState state) {
        this.currentState = state;
    }

    public RogueState getState() {
        return currentState;
    }

    @Override
    public void setRogueLevel(ResourceKey<Level> level) {
        // TODO: 框架占位
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
        // 1. 核心逻辑占位

        // 2. 执行当前状态的内置扩展（修饰器模式）
        List<IRogueExtension> stateExtensions = extensions.getOrDefault(currentState, List.of());
        for (IRogueExtension extension : stateExtensions) {
            extension.onStartRogue(level);
        }

        // 3. 发布 NeoForge 事件（供外部模组监听）
        NeoForge.EVENT_BUS.post(new RogueStartEvent(level, currentState));
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
