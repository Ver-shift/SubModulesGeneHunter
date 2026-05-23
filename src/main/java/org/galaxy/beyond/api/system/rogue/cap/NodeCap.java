package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.*;
import org.galaxy.beyond.api.system.rogue.core.*;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点交互 Cap —— 入口分发，具体步进逻辑委托给 {@link RogueEncounterRunner}。
 */
@EventBusSubscriber
public class NodeCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("node");

    public NodeCap() { super(ID); }

    // ---- 方块点击 ----

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getLevel().getBlockState(event.getPos()).getBlock()
                instanceof org.galaxy.beyond.block.NodeBlock)) return;
        if (!isInRogue(player)) {
            player.sendSystemMessage(Component.translatable("beyond.node.not_in_rogue"));
            return;
        }
        handleNodeClick(player, event.getPos());
    }

    private static void handleNodeClick(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        IRogueContext ctx = new RogueContext();
        RogueNodeData nodeData = ensureNodeData(level, ChunkPos.containing(pos), ctx);
        if (nodeData.getNodeData() == null) return;

        if (ctx.getPhase(level) == RoguePhase.LOBBY) {
            player.sendSystemMessage(Component.translatable("beyond.node.game_not_started"));
            return;
        }

        RogueEncounterRunner runner = new RogueEncounterRunner(level, ctx, nodeData);
        var phase = nodeData.getNodePhase();

        switch (phase) {
            case UNLOCKED -> player.sendSystemMessage(Component.translatable("beyond.node.already_unlocked"));
            case LOCKED -> runner.handleLocked(player);
            case PRE_NODE -> ctx.setPlayerPhase(player, PlayerPhase.PRE_NODE);
            case PRE_EVENT -> ctx.setPlayerPhase(player, PlayerPhase.PRE_EVENT);
            case ON_EVENT -> runner.advanceEvent(player);
        }
    }

    // ---- Phase 事件 ----

    @Override
    public void phaseEnter(ServerLevel level, Phase from, Phase to, IRogueContext ctx) {
        var nodeData = ctx.getRogueData(level).getRogueNodeData();
        if (nodeData == null || nodeData.getNodeData() == null) return;

        if (to == NodePhase.PRE_NODE) {
            new RogueEncounterRunner(level, ctx, nodeData).tryResolveAndAdvance();
        }
    }

    @Override
    public void phaseTick(ServerLevel level, Phase phase, IRogueContext ctx) {
        var nodeData = ctx.getRogueData(level).getRogueNodeData();
        if (nodeData == null || nodeData.getNodeData() == null) return;

        var runner = new RogueEncounterRunner(level, ctx, nodeData);
        if (runner.isPreNode()) runner.tryResolveAndAdvance();
        else if (runner.isPreEvent()) runner.tryReadyPreEvent();
    }

    // ---- ensureNodeData ----

    private static RogueNodeData ensureNodeData(ServerLevel level, ChunkPos clickedChunk, IRogueContext ctx) {
        var rogueData = ctx.getRogueData(level);
        RogueNodeData current = rogueData.getRogueNodeData();

        if (current != null && current.getNodeData() != null) {
            if (clickedChunk.equals(current.getNodeChunk())
                    || current.getNodeData().containsChunk(clickedChunk)) {
                return current;
            }
        }

        List<ChunkPos> cluster = findNodeCluster(level, clickedChunk);
        NodeData nodeData = null;
        for (ChunkPos c : cluster) {
            nodeData = rogueData.findNodeData(c);
            if (nodeData != null) break;
        }
        if (nodeData == null) {
            nodeData = new NodeData(randomNodeColor(level));
            cluster.forEach(nodeData::addChunkPos);
            rogueData.addNodeData(nodeData);
        } else {
            nodeData.getNodeChunks().clear();
            cluster.forEach(nodeData::addChunkPos);
        }

        RogueNodeData next = new RogueNodeData();
        next.setNodeData(nodeData);
        next.setNodeChunk(clickedChunk);
        rogueData.setRogueNodeData(next);
        return next;
    }

    private static List<ChunkPos> findNodeCluster(ServerLevel level, ChunkPos seed) {
        var entries = BeyondAPI.getBeyondDimensionData(level).getLevelZoneData().getZoneEntries();
        var nodeChunks = entries.stream()
                .filter(e -> e.getValue().matches(ZoneType.Node_Zone.mask()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        for (var comp : ZoneHelper.findConnectedComponents(nodeChunks)) {
            if (comp.contains(seed)) return new ArrayList<>(comp);
        }
        return new ArrayList<>(List.of(seed));
    }

    private static boolean isInRogue(ServerPlayer player) {
        return BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig()
                .getRoguePlayerIds().contains(player.getUUID());
    }

    private static NodeColor randomNodeColor(ServerLevel level) {
        int g = org.galaxy.beyond.api.config.CommonConfig.NODE_COLOR_GREEN_WEIGHT.get();
        int o = org.galaxy.beyond.api.config.CommonConfig.NODE_COLOR_ORANGE_WEIGHT.get();
        int r = org.galaxy.beyond.api.config.CommonConfig.NODE_COLOR_RED_WEIGHT.get();
        int total = g + o + r;
        if (total <= 0) return NodeColor.ORANGE;
        int roll = level.getRandom().nextInt(total);
        if (roll < g) return NodeColor.GREEN;
        if (roll < g + o) return NodeColor.ORANGE;
        return NodeColor.RED;
    }
}
