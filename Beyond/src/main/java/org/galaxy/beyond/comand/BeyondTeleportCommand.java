package org.galaxy.beyond.comand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.BeyondAPI;

final class BeyondTeleportCommand {

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("teleport")
                .then(Commands.literal("home")
                        .executes(ctx -> teleportHome(ctx.getSource()))
                )
                .then(Commands.literal("activeBoundary")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> BeyondCreativeCommand.teleportToNearestActiveBoundary(ctx.getSource()))
                )
                .then(Commands.literal("node")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> BeyondCommandSuggestions.nodeTypes(builder))
                                .executes(ctx -> BeyondCreativeCommand.teleportToNearestNode(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "type")))
                        )
                );
    }

    private static int teleportHome(CommandSourceStack source) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();

        var safeZone = BeyondAPI.getSafeZoneStructureData(level);
        if (safeZone == null || safeZone.getInitialized() < 1) {
            source.sendFailure(Component.translatable("commands.beyond.home.not_found"));
            return 0;
        }

        BlockPos spawnPos = safeZone.getSpawnPos();
        if (spawnPos.equals(BlockPos.ZERO)) {
            source.sendFailure(Component.translatable("commands.beyond.home.not_found"));
            return 0;
        }

        player.teleportTo(level, spawnPos.getX() + 0.5, spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        source.sendSuccess(
                () -> Component.translatable("commands.beyond.home.success"),
                true
        );
        return 1;
    }

    private BeyondTeleportCommand() {
    }
}
