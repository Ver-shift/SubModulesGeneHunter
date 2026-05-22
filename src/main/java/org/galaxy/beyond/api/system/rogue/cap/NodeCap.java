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
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.Phase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点交互 Cap。
 * <p>
 * 管理节点全流程：{@code LOCKED → PRE_NODE → ON_EVENT → PRE_EVENT → UNLOCKED}。
 * <p>
 * 通过 {@code @SubscribeEvent} 监听右键节点方块，驱动遭遇解析、事件推进、进度索引，
 * 并在最后一个事件完成后解锁节点并推进全局进度 phase。
 */
@EventBusSubscriber
public class NodeCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("node");

    public NodeCap() { super(ID); }

    // ============================================================
    // 节点方块点击 → @SubscribeEvent
    // ============================================================

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
        ChunkPos clickedChunk = ChunkPos.containing(pos);
        IRogueContext ctx = new RogueContext();

        var rogueData = ctx.getRogueData(level);
        if (rogueData.getCompletedNodeChunks().contains(clickedChunk)) {
            player.sendSystemMessage(Component.translatable("beyond.node.already_unlocked"));
            return;
        }

        RogueNodeData nodeData = ensureNodeData(level, clickedChunk, ctx);
        if (nodeData.getNodeData() == null) return;

        var nodePhase = nodeData.getNodeData().getPhase();

        if (nodePhase == NodePhase.LOCKED) {
            handleLocked(level, player, nodeData, ctx);
        } else if (nodePhase == NodePhase.PRE_NODE) {
            ctx.setPlayerPhase(player, PlayerPhase.PRE_NODE);
        } else if (nodePhase == NodePhase.PRE_EVENT) {
            ctx.setPlayerPhase(player, PlayerPhase.PRE_EVENT);
        } else if (nodePhase == NodePhase.ON_EVENT) {
            advanceEvent(level, player, nodeData, ctx);
        }
    }

    // ============================================================
    // Phase 事件：NodePreNodePhase + NodePreEventPhase 的逻辑
    // ============================================================

    @Override
    public void phaseEnter(ServerLevel level, Phase from, Phase to, IRogueContext ctx) {
        if (to == NodePhase.PRE_NODE) {
            var nodeData = ctx.getRogueData(level).getRogueNodeData();
            if (nodeData == null || nodeData.getNodeData() == null) return;

            int total = ctx.playersInRogue(level).size();
            if (countPreNode(level, ctx) >= total) {
                resolveAndAdvance(level, ctx, nodeData);
            }
        }
    }

    @Override
    public void phaseTick(ServerLevel level, Phase phase, IRogueContext ctx) {
        if (phase == NodePhase.PRE_NODE) {
            var nodeData = ctx.getRogueData(level).getRogueNodeData();
            if (nodeData == null || nodeData.getNodeData() == null) return;

            int total = ctx.playersInRogue(level).size();
            if (countPreNode(level, ctx) < total) return;
            resolveAndAdvance(level, ctx, nodeData);
        } else if (phase == NodePhase.PRE_EVENT) {
            var nodeData = ctx.getRogueData(level).getRogueNodeData();
            if (nodeData == null || nodeData.getNodeData() == null) return;

            int total = ctx.playersInRogue(level).size();
            if (countPreEvent(level, ctx) < total) return;

            nodeData.getNodeData().setPhase(NodePhase.ON_EVENT);
            ctx.setAllPlayerPhase(level, PlayerPhase.ON_EVENT);
        }
    }

    // ============================================================
    // LOCKED → PRE_NODE
    // ============================================================

    private static void handleLocked(ServerLevel level, ServerPlayer player, RogueNodeData nodeData, IRogueContext ctx) {
        nodeData.getNodeData().setPhase(NodePhase.PRE_NODE);
        nodeData.setEncounterData(null);
        nodeData.setCurrentEventIndex(0);
        ctx.setPlayerPhase(player, PlayerPhase.PRE_NODE);
        player.sendSystemMessage(Component.translatable("beyond.node.locked_triggered"));
    }

    // ============================================================
    // 事件推进
    // ============================================================

    private static void advanceEvent(ServerLevel level, ServerPlayer player, RogueNodeData nodeData, IRogueContext ctx) {
        var encData = nodeData.getEncounterData();
        if (encData == null || !encData.getEvents().hasEvents()) return;

        int eventCount = encData.getEvents().eventCount();
        int idx = nodeData.getCurrentEventIndex();

        if (idx >= eventCount - 1) {
            nodeData.getNodeData().setPhase(NodePhase.UNLOCKED);
            var rogueData = ctx.getRogueData(level);
            for (var c : nodeData.getNodeData().getNodeChunkPosList())
                rogueData.getCompletedNodeChunks().add(c);
            rogueData.setProgressIndex(rogueData.getProgressIndex() + 1);

            int totalScenes = rogueData.getProgressType() != null ? rogueData.getProgressType().getScenes().size() : 0;
            int current = rogueData.getProgressIndex();
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.unlocked"), false);
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.progress_advance", current, totalScenes), false);

            BeyondAPI.getBeyondManager().getZoneManager().addActiveZone(level, nodeData);
            level.syncData(org.galaxy.beyond.api.init.BeyondAttachmentInit.GLOBAL_DATA.get());

            if (current >= totalScenes && totalScenes > 0) {
                ctx.setPhase(level, RoguePhase.PROGRESS_FINISH);
                return;
            }
            ctx.setPhase(level, RoguePhase.ON_PROGRESS);
            ctx.setAllPlayerPhase(level, PlayerPhase.ON_PROGRESS);
            return;
        }

        int nextIdx = idx + 1;
        nodeData.setCurrentEventIndex(nextIdx);
        var ids = encData.getEvents().getEvents();
        String eventPath = nextIdx < ids.size() ? ids.get(nextIdx).getPath() : "unknown";
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.event_trigger",
                        Component.translatable("beyond.event." + eventPath),
                        nextIdx + 1, eventCount), false);

        if (nextIdx >= eventCount - 1) {
            nodeData.getNodeData().setPhase(NodePhase.ON_EVENT);
            ctx.setAllPlayerPhase(level, PlayerPhase.ON_EVENT);
        } else {
            nodeData.getNodeData().setPhase(NodePhase.PRE_EVENT);
            ctx.setPlayerPhase(player, PlayerPhase.PRE_EVENT);
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.next_event"), false);
        }
    }

    // ============================================================
    // PRE_NODE / PRE_EVENT：就绪检查 → 推进
    // ============================================================

    private static void resolveAndAdvance(ServerLevel level, IRogueContext ctx, RogueNodeData nodeData) {
        if (nodeData.getNodeData().getPhase() != NodePhase.PRE_NODE) return;

        EncounterType encType = resolveEncounter(level, ctx, nodeData);
        if (encType == null) return;

        EventTask task = Beyond.MANAGER.getDefinitionManager().resolveEvent(level, encType);
        EncounterData encData = new EncounterData();
        encData.setType(encType);
        encData.setEvents(task);
        nodeData.setEncounterData(encData);
        nodeData.setCurrentEventIndex(0);
        nodeData.getNodeData().setPhase(NodePhase.ON_EVENT);
        ctx.setAllPlayerPhase(level, PlayerPhase.ON_EVENT);

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.encounter_start",
                        Component.translatable(encType.getTranslationKey())), false);

        broadcastCurrentEvent(level, nodeData);
    }

    private static void broadcastCurrentEvent(ServerLevel level, RogueNodeData nodeData) {
        var encData = nodeData.getEncounterData();
        if (encData == null || !encData.getEvents().hasEvents()) return;
        int idx = nodeData.getCurrentEventIndex();
        var ids = encData.getEvents().getEvents();
        int eventCount = encData.getEvents().eventCount();
        if (idx < ids.size()) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.event_trigger",
                            Component.translatable("beyond.event." + ids.get(idx).getPath()),
                            idx + 1, eventCount), false);
        }
    }

    // ============================================================
    // 辅助
    // ============================================================

    private static int countPreNode(ServerLevel level, IRogueContext ctx) {
        int c = 0;
        for (var p : ctx.playersInRogue(level))
            if (ctx.getPlayerPhase(p) == PlayerPhase.PRE_NODE) c++;
        return c;
    }

    private static int countPreEvent(ServerLevel level, IRogueContext ctx) {
        int c = 0;
        for (var p : ctx.playersInRogue(level))
            if (ctx.getPlayerPhase(p) == PlayerPhase.PRE_EVENT) c++;
        return c;
    }

    private static EncounterType resolveEncounter(ServerLevel level, IRogueContext ctx, RogueNodeData nodeData) {
        var rogueData = ctx.getRogueData(level);
        var map = rogueData.getEncounterAssignments();
        for (var c : nodeData.getNodeData().getNodeChunkPosList()) {
            var et = map.get(c);
            if (et != null) { nodeData.setNodeChunk(c); return et; }
        }
        var pt = rogueData.getProgressType();
        if (pt == null || pt.getScenes().isEmpty()) return null;
        int idx = rogueData.getProgressIndex();
        if (idx >= pt.getScenes().size()) idx = pt.getScenes().size() - 1;
        var et = EncounterType.from(nodeData.getNodeData().getColor(), pt.getScenes().get(idx));
        if (et != null) map.put(nodeData.getNodeChunk(), et);
        return et;
    }

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

    private static final NodeColor[] COLORS = { NodeColor.GREEN, NodeColor.ORANGE, NodeColor.RED };

    private static NodeColor randomNodeColor(ServerLevel level) {
        return COLORS[level.getRandom().nextInt(COLORS.length)];
    }
}
