package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.block.NodeBlock;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.*;
import org.galaxy.beyond.api.system.rogue.core.*;

/**
 * 节点交互 Cap —— 入口分发，具体步进逻辑委托给 {@link RogueEncounterRunner}。
 */
@EventBusSubscriber
public class NodeCap extends RogueCap {

    public static final ResourceLocation ID = Beyond.asResource("node");

    public NodeCap() {
        super(ID);
    }

    // ---- 方块点击 ----

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getLevel().getBlockState(event.getPos()).getBlock()
                instanceof NodeBlock)) return;
        if (!isInRogue(player)) {
            player.sendSystemMessage(Component.translatable("beyond.node.not_in_rogue"));
            return;
        }
        handleNodeClick(player, event.getPos());
    }

    private static void handleNodeClick(ServerPlayer player, BlockPos pos) {
        ServerLevel level = (ServerLevel) player.level();
        IRogueContext ctx = new RogueContext();
        BlockPos nodePos = normalizeNodePos(level, pos);
        RogueNodeData nodeData = ensureNodeData(level, nodePos, ctx);
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
        else if (runner.isOnEvent()) runner.tryAutoAdvance();
    }

    // ---- ensureNodeData ----

    private static RogueNodeData ensureNodeData(ServerLevel level, BlockPos clickedPos, IRogueContext ctx) {
        ChunkPos clickedChunk = org.galaxy.beyond.api.util.CompatUtil.chunkPos(clickedPos);
        var rogueData = ctx.getRogueData(level);
        RogueNodeData current = rogueData.getRogueNodeData();

        if (current != null && current.getNodeData() != null) {
            if (clickedChunk.equals(current.getNodeChunk())
                    || current.getNodeData().containsChunk(clickedChunk)) {
                current.setNodePos(clickedPos);
                return current;
            }
        }

        NodeData nodeData = BeyondAPI.findNodeData(level, clickedChunk);
        if (nodeData == null) return new RogueNodeData();

        RogueNodeData next = new RogueNodeData();
        next.setNodeData(nodeData);
        next.setNodeChunk(clickedChunk);
        next.setNodePos(clickedPos);
        rogueData.setRogueNodeData(next);
        BeyondAPI.syncGlobalData(level);
        return next;
    }

    private static BlockPos normalizeNodePos(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof NodeBlock
                && state.getValue(NodeBlock.HALF) == DoubleBlockHalf.UPPER) {
            return pos.below();
        }
        return pos;
    }

    private static boolean isInRogue(ServerPlayer player) {
        return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase() != PlayerPhase.LOBBY;
    }

}
