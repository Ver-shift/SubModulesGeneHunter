package org.galaxy.beyond.comand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.galaxy.beyond.api.system.teleport.BeyondTeleportActions;

final class BeyondTeleportCommand {

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("teleport")
                .then(Commands.literal("home")
                        .executes(ctx -> BeyondTeleportActions.teleportHome(ctx.getSource()))
                )
                .then(Commands.literal("activeBoundary")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> BeyondTeleportActions.teleportToNearestActiveBoundary(ctx.getSource()))
                )
                .then(Commands.literal("node")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> BeyondCommandSuggestions.nodeTypes(builder))
                                .executes(ctx -> BeyondTeleportActions.teleportToNearestNode(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "type")))
                        )
                );
    }

    private BeyondTeleportCommand() {
    }
}
