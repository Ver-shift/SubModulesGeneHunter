package org.galaxy.beyond.comand;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueEncounterRunner;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneType;
import org.galaxy.beyond.api.system.zone.util.PackedChunkPos;
import org.galaxy.beyond.api.util.CompatUtil;

import java.util.Set;

public class BeyondCreativeCommand {

    static int expandSafeZone(CommandSourceStack source, int chunkSize) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        if (chunkSize % 2 != 0) {
            source.sendFailure(Component.translatable("commands.beyond.safezone.expand.odd"));
            return 0;
        }

        BeyondAPI.getBeyondManager().getZoneManager().addSafeZone(level, chunkSize, player.blockPosition());
        source.sendSuccess(() -> Component.translatable("commands.beyond.safezone.expand.success", chunkSize), true);
        return 1;
    }

    static int unlockCurrentNode(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        var rogueData = BeyondAPI.getRogueData(level);
        if (BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase() == PlayerPhase.LOBBY) {
            source.sendFailure(Component.translatable("beyond.node.not_in_rogue"));
            return 0;
        }
        if (rogueData.getPhase() != RoguePhase.ON_PROGRESS) {
            source.sendFailure(Component.translatable("beyond.node.game_not_started"));
            return 0;
        }

        ChunkPos playerChunk = CompatUtil.chunkPos(player.blockPosition());
        var nodeData = BeyondAPI.findNodeData(level, playerChunk);
        if (nodeData == null) {
            source.sendFailure(Component.translatable("commands.beyond.unlockCurrentNode.not_in_node"));
            return 0;
        }
        if (nodeData.getPhase() == NodePhase.UNLOCKED) {
            source.sendFailure(Component.translatable("commands.beyond.unlockCurrentNode.already_unlocked"));
            return 0;
        }

        RogueNodeData rogueNodeData = new RogueNodeData();
        rogueNodeData.setNodeData(nodeData);
        rogueNodeData.setNodeChunk(playerChunk);
        rogueData.setRogueNodeData(rogueNodeData);

        new RogueEncounterRunner(level, new RogueContext(), rogueNodeData).forceUnlockNode();
        source.sendSuccess(() -> Component.translatable("commands.beyond.unlockCurrentNode.success"), true);
        return 1;
    }

    static int teleportToNearestActiveBoundary(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        var data = BeyondAPI.getLevelZoneData(level);
        if (data == null || data.getPacked(ZoneType.Active_Zone).isEmpty()) {
            source.sendFailure(Component.translatable("commands.beyond.activeBoundary.not_found"));
            return 0;
        }

        BoundaryTarget target = nearestActiveBoundary(data, data.activeChunks(), player.getX(), player.getZ());
        if (target == null) {
            source.sendFailure(Component.translatable("commands.beyond.activeBoundary.not_found"));
            return 0;
        }

        player.teleportTo(level, target.x(), player.getY(), target.z(),
                Set.of(), player.getYRot(), player.getXRot(), false);
        source.sendSuccess(() -> Component.translatable("commands.beyond.activeBoundary.success",
                String.format("%.1f", target.x()), String.format("%.1f", target.z())), true);
        return 1;
    }

    static int teleportToNearestNode(CommandSourceStack source, String type) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        NodeData target = nearestNode(level, CompatUtil.chunkPos(player.blockPosition()), type);
        if (target == null || target.getNodeChunks().isEmpty()) {
            source.sendFailure(Component.translatable("commands.beyond.node.not_found", type));
            return 0;
        }

        ChunkPos chunk = PackedChunkPos.unpack(target.getNodeChunks().iterator().next());
        player.teleportTo(level, chunk.getMinBlockX() + 8.0, player.getY(), chunk.getMinBlockZ() + 8.0,
                Set.of(), player.getYRot(), player.getXRot(), false);
        source.sendSuccess(() -> Component.translatable("commands.beyond.node.teleport.success",
                type, chunk.x(), chunk.z()), true);
        return 1;
    }

    private static NodeData nearestNode(ServerLevel level, ChunkPos center, String type) {
        NodeData best = null;
        long bestDistance = Long.MAX_VALUE;
        Set<Long> active = Set.copyOf(BeyondAPI.getLevelZoneData(level).getPacked(ZoneType.Active_Zone));
        for (NodeData nodeData : BeyondAPI.getNodeDatas(level)) {
            if (!matches(nodeData, active, type)) continue;
            long distance = nearestDistance(nodeData, center);
            if (distance < bestDistance) {
                best = nodeData;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static boolean matches(NodeData nodeData, Set<Long> active, String type) {
        if ("unlocked".equalsIgnoreCase(type)) return nodeData.getPhase() == NodePhase.UNLOCKED;
        if ("locked".equalsIgnoreCase(type)) return nodeData.getPhase() != NodePhase.UNLOCKED;
        if ("inside".equalsIgnoreCase(type)) return isInsideActiveZone(nodeData, active);
        if ("outside".equalsIgnoreCase(type)) return !isInsideActiveZone(nodeData, active);
        return true;
    }

    private static boolean isInsideActiveZone(NodeData nodeData, Set<Long> active) {
        for (long packed : nodeData.getNodeChunks()) {
            if (active.contains(packed)) return true;
        }
        return false;
    }

    private static long nearestDistance(NodeData nodeData, ChunkPos center) {
        long best = Long.MAX_VALUE;
        for (long packed : nodeData.getNodeChunks()) {
            int x = ChunkPos.getX(packed);
            int z = ChunkPos.getZ(packed);
            long dx = x - center.x();
            long dz = z - center.z();
            long distance = dx * dx + dz * dz;
            if (distance < best) best = distance;
        }
        return best;
    }

    private static BoundaryTarget nearestActiveBoundary(LevelZoneData data, Set<ChunkPos> activeChunks, double playerX, double playerZ) {
        BoundaryTarget best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ChunkPos pos : activeChunks) {
            best = nearestBoundary(data, pos, playerX, playerZ, best, bestDistance);
            if (best != null) bestDistance = best.distanceSqr();
        }
        return best;
    }

    private static BoundaryTarget nearestBoundary(LevelZoneData data, ChunkPos pos,
                                                  double playerX, double playerZ,
                                                  BoundaryTarget best, double bestDistance) {
        best = checkBoundary(data, new ChunkPos(pos.x() - 1, pos.z()), pos.getMinBlockX(), pos.getMinBlockZ() + 8.0, playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        best = checkBoundary(data, new ChunkPos(pos.x() + 1, pos.z()), pos.getMaxBlockX() + 1.0, pos.getMinBlockZ() + 8.0, playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        best = checkBoundary(data, new ChunkPos(pos.x(), pos.z() - 1), pos.getMinBlockX() + 8.0, pos.getMinBlockZ(), playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        return checkBoundary(data, new ChunkPos(pos.x(), pos.z() + 1), pos.getMinBlockX() + 8.0, pos.getMaxBlockZ() + 1.0, playerX, playerZ, best, bestDistance);
    }

    private static BoundaryTarget checkBoundary(LevelZoneData data, ChunkPos neighbor,
                                                double x, double z, double playerX, double playerZ,
                                                BoundaryTarget best, double bestDistance) {
        if (data.hasAny(neighbor)) return best;
        double dx = x - playerX;
        double dz = z - playerZ;
        double distance = dx * dx + dz * dz;
        return distance < bestDistance ? new BoundaryTarget(x, z, distance) : best;
    }

    private record BoundaryTarget(double x, double z, double distanceSqr) {
    }

    private BeyondCreativeCommand() {
    }
}
