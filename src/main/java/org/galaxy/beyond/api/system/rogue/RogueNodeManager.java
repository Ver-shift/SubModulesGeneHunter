package org.galaxy.beyond.api.system.rogue;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.node.core.NodeState;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueNodeManager;
import org.galaxy.beyond.api.system.rogue.definition.DefinitionManager;

import java.util.List;

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

        List<RogueEventType> events = enc.getEvents().getEvents();
        if (events.isEmpty()) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.no_encounter"), false);
            return;
        }

        int idx = Math.max(0, Math.min(data.getCurrentEventIndex(), events.size() - 1));
        if (idx != data.getCurrentEventIndex()) {
            data.setCurrentEventIndex(idx);
        }
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.event_progress", idx + 1), false);

        var ctx = new RogueEventType.Context(enc.getType(), level);
        RogueEventType event = events.get(idx);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.rogue.event_triggered", event.getId().toString()), false);
        event.cast(ctx);
    }

    @Override
    public void handlePreEvent(ServerLevel level) {
        RogueNodeData data = getOrInitRogueNodeData(level);
        ChunkPos chunk = data.getNodeChunk();
        if (chunk == null) return;

        EncounterType encType = rogueManager.getProgressManager().getEncounterType(level, chunk);
        if (encType == null && data.getNodeData() != null) {
            for (ChunkPos candidate : data.getNodeData().getNodeChunks()) {
                encType = rogueManager.getProgressManager().getEncounterType(level, candidate);
                if (encType != null) {
                    data.setNodeChunk(candidate);
                    break;
                }
            }
        }
        if (encType == null && data.getNodeData() != null && !data.getNodeData().getNodeChunks().isEmpty()) {
            long seed = BeyondAPI.getBeyondDimensionData(BeyondAPI.getOverWorld()).getRogueData().getGameSeed();
            rogueManager.getProgressManager().generateEncounterTypes(level, data.getNodeData().getNodeChunks(), seed);
            for (ChunkPos candidate : data.getNodeData().getNodeChunks()) {
                encType = rogueManager.getProgressManager().getEncounterType(level, candidate);
                if (encType != null) {
                    data.setNodeChunk(candidate);
                    break;
                }
            }
        }
        if (encType == null) return;

        EventTask task = definitionManager.resolveEvent(level, encType);

        if (task == null || task.getEvents() == null || task.getEvents().isEmpty()) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.no_encounter"), false);
            return;
        }

        EncounterData encData = new EncounterData();
        encData.setType(encType);
        encData.setEvents(task);
        data.setEncounterData(encData);
        data.setCurrentEventIndex(0);

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
