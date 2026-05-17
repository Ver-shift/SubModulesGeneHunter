package org.galaxy.beyond.api.system.rogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueNodeManager;
import org.galaxy.beyond.api.system.rogue.definition.DefinitionManager;

public class RogueNodeManager implements IRogueNodeManager {

    private final IRogueManager rogueManager;
    private final DefinitionManager definitionManager = new DefinitionManager();

    public RogueNodeManager(IRogueManager rogueManager) {
        this.rogueManager = rogueManager;
    }

    @Override
    public void tick(ServerLevel level) {
        NodeState state = getNodeState(level);
        switch (state) {
            case PRE_EVENT -> handlePreEvent(level);
            case ON_EVENT -> handleOnEvent(level);
            default -> {}
        }
    }

    @Override
    public void handleOnEvent(ServerLevel level) {
        RogueNodeData data = getOrInitRogueNodeData(level);
        EncounterData enc = data.getEncounterData();
        if (enc == null || enc.getEvents() == null) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.no_encounter"), false);
            return;
        }

        int idx = rogueManager.getProgressManager().getCurrentProgressIndex(level);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.event_progress", idx + 1), false);

        // 执行事件链
        var ctx = new RogueEventType.Context(enc.getType(), level);
        for (RogueEventType event : enc.getEvents().getEvents()) {
            event.cast(ctx);
        }
    }

    @Override
    public void handlePreEvent(ServerLevel level) {
        RogueNodeData data = getOrInitRogueNodeData(level);
        ChunkPos chunk = data.getNodeChunk();
        if (chunk == null) return;

        // 查当前区块的遭遇类型
        EncounterType encType = rogueManager.getProgressManager().getEncounterType(level, chunk);
        if (encType == null) return;

        // 从定义中抽取事件
        EventTask task = definitionManager.resolveEvent(level, encType);

        EncounterData encData = new EncounterData();
        encData.setType(encType);
        encData.setEvents(task);
        data.setEncounterData(encData);

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.encounter_resolved", encType.name()), false);
    }

    @Override
    public NodeState getNodeState(ServerLevel level) {
        RogueNodeData data = getRogueNodeData(level);
        if (data == null || data.getNodeData() == null) return NodeState.LOCKED;
        return data.getNodeData().getState();
    }

    @Override
    public void setNodeState(ServerLevel level, NodeState state) {
        RogueNodeData data = getOrInitRogueNodeData(level);
        if (data.getNodeData() == null) {
            data.setNodeData(new org.galaxy.beyond.api.system.node.NodeData());
        }
        data.getNodeData().setState(state);
    }

    private RogueNodeData getOrInitRogueNodeData(ServerLevel level) {
        var rogueData = BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData();
        if (rogueData.getRogueNodeData() == null) {
            rogueData.setRogueNodeData(new RogueNodeData());
        }
        return rogueData.getRogueNodeData();
    }

    @Override
    public RogueNodeData getRogueNodeData(ServerLevel level) {
        return BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData().getRogueNodeData();
    }
}
