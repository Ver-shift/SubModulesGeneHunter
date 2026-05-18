package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.rogue.*;
import org.galaxy.beyond.api.system.zone.CapType;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.zone.ZoneHelper;
import org.galaxy.beyond.api.system.zone.ZoneType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 节点交互 Cap —— 玩家点击节点方块时执行完整交互流程。
 *
 * <pre>
 * LOCKED   → 点击 → PRE_NODE (等待其他玩家)
 * PRE_NODE → 全员就绪 → ON_EVENT (初始化遭遇)
 * PRE_EVENT → 全员就绪 → ON_EVENT (执行事件)
 * ON_EVENT → 最后一个事件 → UNLOCKED
 *          → 非最后一个 → PRE_EVENT (推进下一个)
 * </pre>
 */
public class NodeCap extends ZoneCapType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":node");

    private final IRogueContext ctx = new RogueContext();

    public NodeCap() { super(ID); }

    @Override public int getMaxLevel() { return 1; }
    @Override public CapType getCapType() { return CapType.NORMAL; }

    // ============================================================
    // 入口：玩家点击节点方块
    // ============================================================

    @Override
    public void playerRightClickBlock(ServerPlayer player, Block block) {
        if (!(block instanceof org.galaxy.beyond.block.NodeBlock)) return;
        if (!isInRogue(player)) {
            player.sendSystemMessage(Component.translatable("beyond.node.not_in_rogue"));
            return;
        }
        handleNodeClick(player, player.getOnPos());
    }

    private void handleNodeClick(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        ChunkPos clickedChunk = ChunkPos.containing(pos);

        var rogueData = ctx.getRogueData(level);
        if (rogueData.getCompletedNodeChunks().contains(clickedChunk)) {
            player.sendSystemMessage(Component.translatable("beyond.node.already_unlocked"));
            return;
        }

        // 查找或创建节点数据
        RogueNodeData nodeData = ensureNodeData(level, clickedChunk);
        if (nodeData.getNodeData() == null) return;

        var nodePhase = nodeData.getNodeData().getPhase();
        var totalPlayers = ctx.playersInRogue(level).size();

        if (nodePhase == BeyondPhaseInit.NODE_LOCKED.get()) {
            handleLocked(level, player, nodeData);
        } else if (nodePhase == BeyondPhaseInit.NODE_PRE_NODE.get()) {
            handlePreNode(level, player, nodeData, totalPlayers);
        } else if (nodePhase == BeyondPhaseInit.NODE_PRE_EVENT.get()) {
            handlePreEvent(level, player, nodeData, totalPlayers);
        } else if (nodePhase == BeyondPhaseInit.NODE_ON_EVENT.get()) {
            handleOnEvent(level, nodeData, totalPlayers);
        }
    }

    // ============================================================
    // LOCKED → PRE_NODE
    // ============================================================

    private void handleLocked(ServerLevel level, ServerPlayer player, RogueNodeData nodeData) {
        nodeData.getNodeData().setPhase(BeyondPhaseInit.NODE_PRE_NODE.get());
        nodeData.setEncounterData(null);
        nodeData.setCurrentEventIndex(0);
        ctx.setPlayerPhase(player, BeyondPhaseInit.PLAYER_PRE_NODE.get());
        player.sendSystemMessage(Component.translatable("beyond.node.locked_triggered"));
    }

    // ============================================================
    // PRE_NODE → 全员就绪 → ON_EVENT
    // ============================================================

    private void handlePreNode(ServerLevel level, ServerPlayer player, RogueNodeData nodeData, int total) {
        ctx.setPlayerPhase(player, BeyondPhaseInit.PLAYER_PRE_NODE.get());
        int ready = countPlayersInPhase(level, BeyondPhaseInit.PLAYER_PRE_NODE.get());

        if (ready < total) {
            player.sendSystemMessage(Component.translatable("beyond.node.pre_node_waiting", ready, total));
            return;
        }

        // 全员就绪 → 解析遭遇
        var defMgr = Beyond.MANAGER.getDefinitionManager();
        EncounterType encType = resolveEncounterType(level, nodeData);
        if (encType == null) {
            player.sendSystemMessage(Component.translatable("beyond.node.no_encounter"));
            return;
        }
        EventTask task = defMgr.resolveEvent(level, encType);

        EncounterData encData = new EncounterData();
        encData.setType(encType);
        encData.setEvents(task);
        nodeData.setEncounterData(encData);
        nodeData.setCurrentEventIndex(0);
        nodeData.getNodeData().setPhase(BeyondPhaseInit.NODE_ON_EVENT.get());
        ctx.setAllPlayerPhase(level, BeyondPhaseInit.PLAYER_ON_EVENT.get());

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.encounter_start",
                        Component.translatable(encType.getTranslationKey())), false);
        broadcastCurrentEvent(level, nodeData);
    }

    // ============================================================
    // PRE_EVENT → 全员就绪 → ON_EVENT
    // ============================================================

    private void handlePreEvent(ServerLevel level, ServerPlayer player, RogueNodeData nodeData, int total) {
        ctx.setPlayerPhase(player, BeyondPhaseInit.PLAYER_PRE_EVENT.get());
        int ready = countPlayersInPhase(level, BeyondPhaseInit.PLAYER_PRE_EVENT.get());

        if (ready < total) {
            player.sendSystemMessage(Component.translatable("beyond.node.pre_event_waiting", ready, total));
            return;
        }

        // 全员就绪 → 立即推进事件
        nodeData.getNodeData().setPhase(BeyondPhaseInit.NODE_ON_EVENT.get());
        ctx.setAllPlayerPhase(level, BeyondPhaseInit.PLAYER_ON_EVENT.get());
        broadcastCurrentEvent(level, nodeData);
        handleOnEvent(level, nodeData, total);
    }

    // ============================================================
    // ON_EVENT → 最后一个事件 → UNLOCKED / 否则 → PRE_EVENT
    // ============================================================

    private void handleOnEvent(ServerLevel level, RogueNodeData nodeData, int total) {
        var encData = nodeData.getEncounterData();
        if (encData == null || encData.getEvents().getEvents().isEmpty()) {
            unlockNode(level, nodeData);
            return;
        }

        int eventCount = encData.getEvents().getEvents().size();
        int currentIdx = nodeData.getCurrentEventIndex();

        if (currentIdx >= eventCount - 1) {
            unlockNode(level, nodeData);
        } else {
            nodeData.setCurrentEventIndex(currentIdx + 1);
            nodeData.getNodeData().setPhase(BeyondPhaseInit.NODE_PRE_EVENT.get());
            ctx.setAllPlayerPhase(level, BeyondPhaseInit.PLAYER_PRE_EVENT.get());
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.next_event"), false);
        }
    }

    // ============================================================
    // UNLOCK → 推进进度 + 扩充活跃区域 + 回到 ON_PROGRESS
    // ============================================================

    private void unlockNode(ServerLevel level, RogueNodeData nodeData) {
        nodeData.getNodeData().setPhase(BeyondPhaseInit.NODE_UNLOCKED.get());
        var rogueData = ctx.getRogueData(level);
        if (nodeData.getNodeData() != null) {
            for (ChunkPos chunk : nodeData.getNodeData().getNodeChunks()) {
                rogueData.getCompletedNodeChunks().add(chunk);
            }
        }

        rogueData.setProgressIndex(rogueData.getProgressIndex() + 1);

        int totalScenes = rogueData.getProgressType() != null ? rogueData.getProgressType().getScenes().size() : 0;
        int current = rogueData.getProgressIndex(); // 已递增后的值，即完成的第几个

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.unlocked"), false);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.node.progress_advance", current, totalScenes), false);

        // 最后一个 Scene → 进入结算
        if (current >= totalScenes && totalScenes > 0) {
            ctx.setPhase(level, BeyondPhaseInit.ROGUE_PROGRESS_FINISH.get());
            return;
        }

        // 扩充活跃区域
        BeyondAPI.getBeyondManager().getZoneManager().addActiveZone(level, nodeData);

        // 回到 ON_PROGRESS
        ctx.setPhase(level, BeyondPhaseInit.ROGUE_ON_PROGRESS.get());
        ctx.setAllPlayerPhase(level, BeyondPhaseInit.PLAYER_ON_PROGRESS.get());
    }

    // ============================================================
    // 辅助：节点数据管理
    // ============================================================

    private RogueNodeData ensureNodeData(ServerLevel level, ChunkPos clickedChunk) {
        var rogueData = ctx.getRogueData(level);
        RogueNodeData current = rogueData.getRogueNodeData();

        if (current != null && current.getNodeData() != null) {
            if (clickedChunk.equals(current.getNodeChunk())
                    || current.getNodeData().getNodeChunks().contains(clickedChunk)) {
                return current;
            }
        }

        // 从 nodeDataMap 查找已创建的 NodeData（addNodeZone 时创建）
        List<ChunkPos> cluster = findNodeCluster(level, clickedChunk);
        org.galaxy.beyond.api.system.node.NodeData nodeData = null;
        for (ChunkPos c : cluster) {
            nodeData = rogueData.getNodeDataMap().get(c);
            if (nodeData != null) break;
        }
        if (nodeData == null) {
            nodeData = new org.galaxy.beyond.api.system.node.NodeData(randomNodeColor(level));
            rogueData.getNodeDataMap().put(cluster.getFirst(), nodeData);
        }
        nodeData.getNodeChunks().clear();
        nodeData.getNodeChunks().addAll(cluster);

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

    // ============================================================
    // 辅助：遭遇类型 → 优先查缓存，否则按节点颜色+当前场景生成
    // ============================================================

    private EncounterType resolveEncounterType(ServerLevel level, RogueNodeData nodeData) {
        var rogueData = ctx.getRogueData(level);
        var map = rogueData.getEncounterAssignments();

        for (ChunkPos c : nodeData.getNodeData().getNodeChunks()) {
            EncounterType et = map.get(c);
            if (et != null) {
                nodeData.setNodeChunk(c);
                return et;
            }
        }

        var progressType = rogueData.getProgressType();
        if (progressType == null || progressType.getScenes().isEmpty()) return null;
        int idx = rogueData.getProgressIndex();
        if (idx >= progressType.getScenes().size()) idx = progressType.getScenes().size() - 1;
        SceneType scene = progressType.getScenes().get(idx);

        var et = EncounterType.from(nodeData.getNodeData().getColor(), scene);
        if (et != null) map.put(nodeData.getNodeChunk(), et);
        return et;
    }

    // ============================================================
    // 辅助
    // ============================================================

    private void broadcastCurrentEvent(ServerLevel level, RogueNodeData nodeData) {
        var encData = nodeData.getEncounterData();
        if (encData == null) return;
        var events = encData.getEvents().getEvents();
        int idx = nodeData.getCurrentEventIndex();
        if (idx < events.size()) {
            var eventId = events.get(idx);
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.node.event_trigger",
                            Component.translatable("beyond.event." + eventId.getPath()),
                            idx + 1, events.size()), false);
        }
    }

    private int countPlayersInPhase(ServerLevel level, org.galaxy.beyond.api.system.rogue.core.PlayerPhase target) {
        int c = 0;
        for (var p : ctx.playersInRogue(level)) {
            if (ctx.getPlayerPhase(p) == target) c++;
        }
        return c;
    }

    private static boolean isInRogue(ServerPlayer player) {
        return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase()
                != BeyondPhaseInit.PLAYER_LOBBY.get();
    }

    private static final NodeColor[] COLORS = { NodeColor.GREEN, NodeColor.ORANGE, NodeColor.RED };

    private static NodeColor randomNodeColor(ServerLevel level) {
        return COLORS[level.getRandom().nextInt(COLORS.length)];
    }
}
