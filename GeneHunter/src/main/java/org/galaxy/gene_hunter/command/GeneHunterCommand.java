package org.galaxy.gene_hunter.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxy.gene_hunter.api.system.choice.ChoiceManager;

public class GeneHunterCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("gene_hunter")
                .requires(source -> source.hasPermission(2)) // 需要权限等级2（OP）
                .then(Commands.literal("start_roll")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("loottype", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                builder.suggest("weapon");
                                builder.suggest("xene");
                                return builder.buildFuture();
                            })
                            .executes(GeneHunterCommand::executeStartRoll)
                        )
                    )
                )
        );
    }

    private static int executeStartRoll(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            String lootType = StringArgumentType.getString(context, "loottype");

            var manager = GeneHunterAPI.getChoiceManager(targetPlayer);
            if (!(manager instanceof ChoiceManager choiceManager)) {
                context.getSource().sendFailure(
                    net.minecraft.network.chat.Component.translatable("commands.gene_hunter.start_roll.failed.no_manager")
                );
                return 0;
            }

            switch (lootType.toLowerCase()) {
                case "weapon":
                    choiceManager.startRoll(GeneHunterLootInit.WEAPON_LOOT_TYPE.get());
                    context.getSource().sendSuccess(
                        () -> net.minecraft.network.chat.Component.translatable(
                            "commands.gene_hunter.start_roll.weapon.success",
                            targetPlayer.getName().getString()
                        ),
                        true
                    );
                    return 1;

                case "xene":
                    choiceManager.startRoll(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get());
                    context.getSource().sendSuccess(
                        () -> net.minecraft.network.chat.Component.translatable(
                            "commands.gene_hunter.start_roll.xene.success",
                            targetPlayer.getName().getString()
                        ),
                        true
                    );
                    return 1;

                default:
                    context.getSource().sendFailure(
                        net.minecraft.network.chat.Component.translatable(
                            "commands.gene_hunter.start_roll.failed.unknown_type",
                            lootType
                        )
                    );
                    return 0;
            }

        } catch (Exception e) {
            context.getSource().sendFailure(
                net.minecraft.network.chat.Component.translatable("commands.gene_hunter.start_roll.failed.error", e.getMessage())
            );
            e.printStackTrace();
            return 0;
        }
    }
}
