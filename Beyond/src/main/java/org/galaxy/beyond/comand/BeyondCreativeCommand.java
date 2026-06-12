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
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueNodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueEncounterRunner;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.teleport.BeyondTeleportActions;

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
                                .executes(ctx -> BeyondTeleportActions.teleportToNearestActiveBoundary(ctx.getSource()))
                        )
                );
    }

    static int expandSafeZone(CommandSourceStack source, int chunkSize) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        if (!CommonConfig.isRogueDimension(level)) {
            source.sendFailure(Component.translatable("commands.beyond.rogue_dimension.required"));
            return 0;
        }
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
        if (!CommonConfig.isRogueDimension(level)) {
            source.sendFailure(Component.translatable("commands.beyond.rogue_dimension.required"));
            return 0;
        }
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

    private BeyondCreativeCommand() {
    }
}
