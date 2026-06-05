package org.galaxy.beyond.comand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

final class BeyondPlayerPhaseCommand {

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("playerPhase")
                .then(Commands.literal("get")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> BeyondCommandSuggestions.onlinePlayers(ctx.getSource(), builder))
                                .executes(ctx -> getPhase(ctx.getSource(), StringArgumentType.getString(ctx, "player")))
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> BeyondCommandSuggestions.onlinePlayers(ctx.getSource(), builder))
                                .then(Commands.argument("phase", StringArgumentType.word())
                                        .suggests((ctx, builder) -> BeyondCommandSuggestions.playerPhases(builder))
                                        .executes(ctx -> setPhase(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "player"),
                                                StringArgumentType.getString(ctx, "phase")))
                                )
                        )
                );
    }

    private static int getPhase(CommandSourceStack source, String name) {
        var player = source.getServer().getPlayerList().getPlayerByName(name);
        if (player == null) {
            source.sendFailure(Component.translatable("commands.beyond.playerPhase.not_found", name));
            return 0;
        }
        PlayerPhase phase = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
        source.sendSuccess(
                () -> Component.translatable("commands.beyond.playerPhase.get", name, phase.name()),
                false);
        return 1;
    }

    private static int setPhase(CommandSourceStack source, String name, String value) {
        var player = source.getServer().getPlayerList().getPlayerByName(name);
        if (player == null) {
            source.sendFailure(Component.translatable("commands.beyond.playerPhase.not_found", name));
            return 0;
        }
        PlayerPhase phase = parsePlayerPhase(value);
        if (phase == null) {
            source.sendFailure(Component.translatable("commands.beyond.playerPhase.invalid", value));
            return 0;
        }
        BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(phase);
        BeyondAPI.syncPlayerData(player);
        source.sendSuccess(
                () -> Component.translatable("commands.beyond.playerPhase.set", name, phase.name()),
                true);
        return 1;
    }

    private static PlayerPhase parsePlayerPhase(String value) {
        for (PlayerPhase phase : PlayerPhase.values()) {
            if (phase.name().equalsIgnoreCase(value)) return phase;
        }
        return null;
    }

    private BeyondPlayerPhaseCommand() {
    }
}
