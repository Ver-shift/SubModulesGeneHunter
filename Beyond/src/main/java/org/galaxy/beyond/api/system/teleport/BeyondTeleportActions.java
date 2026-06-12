package org.galaxy.beyond.api.system.teleport;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.zone.LevelZoneData;
import org.galaxy.beyond.api.system.zone.ZoneType;
import org.galaxy.beyond.api.system.zone.util.PackedChunkPos;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class BeyondTeleportActions {

    private static final int SAFE_SEARCH_RADIUS = 8;

    public static boolean teleport(ServerPlayer player, BeyondTeleportType type) {
        return switch (type) {
            case HOME -> teleportHome(player);
            case ACTIVE_BOUNDARY -> teleportToNearestActiveBoundary(player);
            case ANY_NODE -> teleportToNearestNode(player, "any", false);
            case UNLOCKED_NODE -> teleportToNearestNode(player, "unlocked", false);
            case LOCKED_NODE -> teleportToNearestNode(player, "locked", false);
            case INSIDE_NODE -> teleportToNearestNode(player, "inside", false);
            case OUTSIDE_NODE -> teleportToNearestNode(player, "outside", false);
        };
    }

    public static int teleportHome(CommandSourceStack source) throws CommandSyntaxException {
        return teleportHome(source.getPlayerOrException()) ? 1 : 0;
    }

    public static int teleportToNearestActiveBoundary(CommandSourceStack source) throws CommandSyntaxException {
        return teleportToNearestActiveBoundary(source.getPlayerOrException()) ? 1 : 0;
    }

    public static int teleportToNearestNode(CommandSourceStack source, String type) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        return teleportToNearestNode(player, type, true) ? 1 : 0;
    }

    public static boolean teleportHome(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        var safeZone = BeyondAPI.getSafeZoneStructureData(level);
        if (safeZone == null || safeZone.getInitialized() < 1) {
            player.sendSystemMessage(Component.translatable("commands.beyond.home.not_found"));
            return false;
        }

        BlockPos spawnPos = safeZone.getSpawnPos();
        if (spawnPos.equals(BlockPos.ZERO)) {
            player.sendSystemMessage(Component.translatable("commands.beyond.home.not_found"));
            return false;
        }

        BlockPos safePos = safeLanding(level, spawnPos.getX(), spawnPos.getZ(), spawnPos.getY(), false);
        if (safePos == null) {
            player.sendSystemMessage(Component.translatable("beyond.teleport.unsafe"));
            return false;
        }

        teleportTo(player, level, safePos);
        player.sendSystemMessage(Component.translatable("commands.beyond.home.success"));
        return true;
    }

    public static boolean teleportToNearestActiveBoundary(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        if (!CommonConfig.isRogueDimension(level)) {
            player.sendSystemMessage(Component.translatable("commands.beyond.rogue_dimension.required"));
            return false;
        }
        var data = BeyondAPI.getLevelZoneData(level);
        if (data == null || data.getPacked(ZoneType.Active_Zone).isEmpty()) {
            player.sendSystemMessage(Component.translatable("commands.beyond.activeBoundary.not_found"));
            return false;
        }

        BoundaryTarget target = nearestActiveBoundary(data, data.activeChunks(), player.getX(), player.getZ());
        if (target == null) {
            player.sendSystemMessage(Component.translatable("commands.beyond.activeBoundary.not_found"));
            return false;
        }

        BlockPos safePos = safeLanding(level, target.x(), target.z(), player.blockPosition().getY(), true);
        if (safePos == null) {
            player.sendSystemMessage(Component.translatable("beyond.teleport.unsafe"));
            return false;
        }

        teleportTo(player, level, safePos);
        player.sendSystemMessage(Component.translatable("commands.beyond.activeBoundary.success",
                String.format("%.1f", safePos.getX() + 0.5), String.format("%.1f", safePos.getZ() + 0.5)));
        return true;
    }

    public static boolean teleportToNearestNode(ServerPlayer player, String type, boolean expandIfMissing) {
        ServerLevel level = (ServerLevel) player.level();
        if (!CommonConfig.isRogueDimension(level)) {
            player.sendSystemMessage(Component.translatable("commands.beyond.rogue_dimension.required"));
            return false;
        }
        LevelZoneData data = BeyondAPI.getLevelZoneData(level);
        if (data == null) {
            player.sendSystemMessage(Component.translatable("commands.beyond.node.not_found", type));
            return false;
        }

        String normalized = type.toLowerCase(Locale.ROOT);
        if (!isNodeType(normalized)) {
            player.sendSystemMessage(Component.translatable("commands.beyond.node.invalid_type", type));
            return false;
        }

        NodeTarget best = findNearestNode(level, normalized, player.getX(), player.getZ());
        if (best == null) {
            if (!expandIfMissing) {
                player.sendSystemMessage(Component.translatable("commands.beyond.node.not_found", type));
                return false;
            }
            BeyondAPI.getBeyondManager().getZoneManager().discoverNearestNode(level,
                            player.chunkPosition(), CommonConfig.ACTIVE_ZONE_NODE_MAX_EXPAND_RADIUS.get())
                    .thenRunAsync(() -> teleportDiscoveredNode(player, normalized, type), level.getServer());
            player.sendSystemMessage(Component.translatable("beyond.node.zone_expanding_scan", "|"));
            return true;
        }

        return teleportToNode(player, level, normalized, best);
    }

    private static void teleportDiscoveredNode(ServerPlayer player, String normalized, String inputType) {
        if (player.isRemoved()) return;
        ServerLevel level = (ServerLevel) player.level();
        if (!CommonConfig.isRogueDimension(level)) return;

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

    private static boolean teleportToNode(ServerPlayer player, ServerLevel level, String normalized, NodeTarget target) {
        BlockPos safePos = safeLanding(level, target.x(), target.z(), player.blockPosition().getY(), true);
        if (safePos == null) {
            player.sendSystemMessage(Component.translatable("beyond.teleport.unsafe"));
            return false;
        }

        teleportTo(player, level, safePos);
        player.sendSystemMessage(Component.translatable("commands.beyond.node.teleport.success",
                normalized, String.format("%.1f", safePos.getX() + 0.5), String.format("%.1f", safePos.getZ() + 0.5)));
        return true;
    }

    private static void teleportTo(ServerPlayer player, ServerLevel level, BlockPos pos) {
        player.teleportTo(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }

    private static BlockPos safeLanding(ServerLevel level, double x, double z, int anchorY, boolean requireSky) {
        BlockPos center = surfacePos(level, x, z, anchorY);
        if (canStand(level, center, requireSky)) return center;

        for (int r = 1; r <= SAFE_SEARCH_RADIUS; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    BlockPos pos = surfacePos(level, x + dx, z + dz, anchorY);
                    if (canStand(level, pos, requireSky)) return pos;
                }
            }
        }
        return null;
    }

    private static BlockPos surfacePos(ServerLevel level, double x, double z, int anchorY) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(blockX, 0, blockZ));
        if (top.getY() > level.getMinBuildHeight()) return top;
        return new BlockPos(blockX, anchorY, blockZ);
    }

    private static boolean canStand(ServerLevel level, BlockPos pos, boolean requireSky) {
        if (pos.getY() <= level.getMinBuildHeight()) return false;
        if (requireSky && !level.canSeeSky(pos)) return false;
        if (!isValidGround(level.getBlockState(pos.below()))) return false;
        return isEmpty(level, pos) && isEmpty(level, pos.above());
    }

    private static boolean isValidGround(BlockState state) {
        return state.blocksMotion();
    }

    private static boolean isEmpty(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isNodeType(String type) {
        return switch (type) {
            case "any", "unlocked", "locked", "outside", "inside" -> true;
            default -> false;
        };
    }

    private static boolean matchesNodeType(NodeData nodeData, String type, Set<Long> active) {
        return switch (type) {
            case "any" -> true;
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

    private static BoundaryTarget nearestActiveBoundary(LevelZoneData data, Set<ChunkPos> activeChunks,
                                                        double playerX, double playerZ) {
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

    private BeyondTeleportActions() {
    }
}
