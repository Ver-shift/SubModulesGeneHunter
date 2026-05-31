package org.galaxy.beyond.comand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.config.CommonConfig;
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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BeyondCreativeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("creative")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("safezone")
                        .then(Commands.literal("expand")
                                .then(Commands.argument("chunkSize", IntegerArgumentType.integer(2, 256))
                                        .executes(ctx -> expandSafeZone(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "chunkSize")))
                                )
                        )
                )
                .then(Commands.literal("node")
                        .then(Commands.literal("unlockCurrent")
                                .executes(ctx -> unlockCurrentNode(ctx.getSource()))
                        )
                )
                .then(Commands.literal("activeBoundary")
                        .then(Commands.literal("teleport")
                                .executes(ctx -> teleportToNearestActiveBoundary(ctx.getSource()))
                        )
                );
    }

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

        ChunkPos playerChunk = new ChunkPos(player.blockPosition());
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

        player.teleportTo(level, target.x(), player.getY(), target.z(), player.getYRot(), player.getXRot());
        source.sendSuccess(() -> Component.translatable("commands.beyond.activeBoundary.success",
                String.format("%.1f", target.x()), String.format("%.1f", target.z())), true);
        return 1;
    }

    static int teleportToNearestNode(CommandSourceStack source, String type) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        if (data == null) {
            source.sendFailure(Component.translatable("commands.beyond.node.not_found", type));
            return 0;
        }

        String normalized = type.toLowerCase(Locale.ROOT);
        if (!isNodeType(normalized)) {
            source.sendFailure(Component.translatable("commands.beyond.node.invalid_type", type));
            return 0;
        }

        NodeTarget best = findNearestNode(level, normalized, player.getX(), player.getZ());
        if (best == null) {
            BeyondAPI.getBeyondManager().getZoneManager().discoverNearestNode(level,
                            player.chunkPosition(), CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get())
                    .thenRunAsync(() -> teleportDiscoveredNode(player, normalized, type), level.getServer());
            source.sendSuccess(() -> Component.translatable("beyond.node.zone_expanding_scan", "|"), false);
            return 1;
        }

        teleportToNode(player, level, normalized, best);
        return 1;
    }

    private static void teleportDiscoveredNode(ServerPlayer player, String normalized, String inputType) {
        if (player.isRemoved()) return;
        ServerLevel level = (ServerLevel) player.level();
        NodeTarget best = findNearestNode(level, normalized, player.getX(), player.getZ());
        if (best == null) {
            player.sendSystemMessage(Component.translatable("commands.beyond.node.not_found", inputType));
            return;
        }
        teleportToNode(player, level, normalized, best);
    }

    private static NodeTarget findNearestNode(ServerLevel level, String type, double playerX, double playerZ) {
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        Set<Long> active = new HashSet<>(data.getPacked(ZoneType.Active_Zone));
        NodeTarget best = null;
        for (NodeData nodeData : BeyondAPI.getNodeDatas(level)) {
            if (nodeData.getNodeChunks().isEmpty()) continue;
            if (!matchesNodeType(nodeData, type, active)) continue;

            NodeTarget target = NodeTarget.from(nodeData, playerX, playerZ);
            if (best == null || target.distanceSqr() < best.distanceSqr()) best = target;
        }
        return best;
    }

    private static void teleportToNode(ServerPlayer player, ServerLevel level, String normalized, NodeTarget target) {
        String targetX = String.format("%.1f", target.x());
        String targetZ = String.format("%.1f", target.z());
        player.teleportTo(level, target.x(), player.getY(), target.z(), player.getYRot(), player.getXRot());
        player.sendSystemMessage(Component.translatable("commands.beyond.node.teleport.success",
                normalized, targetX, targetZ));
    }

    private static boolean isNodeType(String type) {
        return switch (type) {
            case "unlocked", "locked", "outside", "inside" -> true;
            default -> false;
        };
    }

    private static boolean matchesNodeType(NodeData nodeData, String type, Set<Long> active) {
        return switch (type) {
            case "unlocked" -> nodeData.getPhase() == NodePhase.UNLOCKED;
            case "locked" -> nodeData.getPhase() != NodePhase.UNLOCKED;
            case "outside" -> !touchesActive(nodeData.getNodeChunks(), active);
            case "inside" -> touchesActive(nodeData.getNodeChunks(), active);
            default -> false;
        };
    }

    private static boolean touchesActive(Iterable<Long> chunks, Set<Long> active) {
        for (long chunk : chunks) {
            if (active.contains(chunk)) return true;
            int x = PackedChunkPos.x(chunk);
            int z = PackedChunkPos.z(chunk);
            if (active.contains(PackedChunkPos.pack(x + 1, z))) return true;
            if (active.contains(PackedChunkPos.pack(x - 1, z))) return true;
            if (active.contains(PackedChunkPos.pack(x, z + 1))) return true;
            if (active.contains(PackedChunkPos.pack(x, z - 1))) return true;
        }
        return false;
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
        best = checkBoundary(data, new ChunkPos(pos.x - 1, pos.z), pos.getMinBlockX(), pos.getMinBlockZ() + 8.0, playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        best = checkBoundary(data, new ChunkPos(pos.x + 1, pos.z), pos.getMaxBlockX() + 1.0, pos.getMinBlockZ() + 8.0, playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        best = checkBoundary(data, new ChunkPos(pos.x, pos.z - 1), pos.getMinBlockX() + 8.0, pos.getMinBlockZ(), playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        return checkBoundary(data, new ChunkPos(pos.x, pos.z + 1), pos.getMinBlockX() + 8.0, pos.getMaxBlockZ() + 1.0, playerX, playerZ, best, bestDistance);
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

    private record NodeTarget(double x, double z, double distanceSqr) {
        static NodeTarget from(NodeData nodeData, double playerX, double playerZ) {
            int minX = Integer.MAX_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxZ = Integer.MIN_VALUE;
            for (long chunk : nodeData.getNodeChunks()) {
                int x = PackedChunkPos.x(chunk);
                int z = PackedChunkPos.z(chunk);
                if (x < minX) minX = x;
                if (z < minZ) minZ = z;
                if (x > maxX) maxX = x;
                if (z > maxZ) maxZ = z;
            }
            double x = (minX + maxX + 1) * 8.0;
            double z = (minZ + maxZ + 1) * 8.0;
            double dx = x - playerX;
            double dz = z - playerZ;
            return new NodeTarget(x, z, dx * dx + dz * dz);
        }
    }

    private BeyondCreativeCommand() {
    }
}
