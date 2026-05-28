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
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueEncounterRunner;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.zone.ZoneType;

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

    private static int expandSafeZone(CommandSourceStack source, int chunkSize) throws CommandSyntaxException {
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

    private static int unlockCurrentNode(CommandSourceStack source) throws CommandSyntaxException {
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

    private static int teleportToNearestActiveBoundary(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        var data = BeyondAPI.getLevelZoneData(level);
        if (data == null || data.getPacked(ZoneType.Active_Zone).isEmpty()) {
            source.sendFailure(Component.translatable("commands.beyond.activeBoundary.not_found"));
            return 0;
        }

        BoundaryTarget target = nearestActiveBoundary(data.activeChunks(), player.getX(), player.getZ());
        if (target == null) {
            source.sendFailure(Component.translatable("commands.beyond.activeBoundary.not_found"));
            return 0;
        }

        player.teleportTo(level, target.x(), player.getY(), target.z(), player.getYRot(), player.getXRot());
        source.sendSuccess(() -> Component.translatable("commands.beyond.activeBoundary.success",
                String.format("%.1f", target.x()), String.format("%.1f", target.z())), true);
        return 1;
    }

    private static BoundaryTarget nearestActiveBoundary(Set<ChunkPos> activeChunks, double playerX, double playerZ) {
        BoundaryTarget best = null;
        double bestDistance = Double.MAX_VALUE;
        for (ChunkPos pos : activeChunks) {
            best = nearestBoundary(activeChunks, pos, playerX, playerZ, best, bestDistance);
            if (best != null) bestDistance = best.distanceSqr();
        }
        return best;
    }

    private static BoundaryTarget nearestBoundary(Set<ChunkPos> activeChunks, ChunkPos pos,
                                                  double playerX, double playerZ,
                                                  BoundaryTarget best, double bestDistance) {
        best = checkBoundary(activeChunks, new ChunkPos(pos.x - 1, pos.z), pos.getMinBlockX(), pos.getMinBlockZ() + 8.0, playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        best = checkBoundary(activeChunks, new ChunkPos(pos.x + 1, pos.z), pos.getMaxBlockX() + 1.0, pos.getMinBlockZ() + 8.0, playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        best = checkBoundary(activeChunks, new ChunkPos(pos.x, pos.z - 1), pos.getMinBlockX() + 8.0, pos.getMinBlockZ(), playerX, playerZ, best, bestDistance);
        if (best != null) bestDistance = best.distanceSqr();
        return checkBoundary(activeChunks, new ChunkPos(pos.x, pos.z + 1), pos.getMinBlockX() + 8.0, pos.getMaxBlockZ() + 1.0, playerX, playerZ, best, bestDistance);
    }

    private static BoundaryTarget checkBoundary(Set<ChunkPos> activeChunks, ChunkPos neighbor,
                                                double x, double z, double playerX, double playerZ,
                                                BoundaryTarget best, double bestDistance) {
        if (activeChunks.contains(neighbor)) return best;
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
