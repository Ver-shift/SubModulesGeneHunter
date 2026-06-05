package org.galaxy.beyond.comand;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.galaxy.beyond.api.util.CompatUtil;

final class BeyondRootCommand {

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("beyond")
                .then(BeyondTeleportCommand.create())
                .then(Commands.literal("safezone")
                        .requires(source -> CompatUtil.hasPermission(source, 2))
                        .then(Commands.argument("chunkSize", IntegerArgumentType.integer(2, 256))
                                .executes(ctx -> BeyondCreativeCommand.expandSafeZone(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "chunkSize")))
                        )
                )
                .then(Commands.literal("unlockNode")
                        .requires(source -> CompatUtil.hasPermission(source, 2))
                        .executes(ctx -> BeyondCreativeCommand.unlockCurrentNode(ctx.getSource()))
                )
                .then(BeyondConfigCommand.create())
                .then(BeyondPlayerPhaseCommand.create());
    }

    private BeyondRootCommand() {
    }
}
