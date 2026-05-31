package org.galaxy.beyond.comand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueData;

final class BeyondConfigCommand {

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("config")
                .then(Commands.literal("currentProgress")
                        .then(Commands.argument("value", StringArgumentType.string())
                                .suggests((ctx, builder) -> BeyondCommandSuggestions.currentProgress(builder))
                                .executes(ctx -> {
                                    var id = ResourceLocation.parse(StringArgumentType.getString(ctx, "value"));
                                    var def = BeyondAPI.getRogueDefinition(BeyondAPI.getOverWorld());
                                    var error = def.validateProgress(id);
                                    if (error != null) {
                                        ctx.getSource().sendFailure(error);
                                        return 0;
                                    }
                                    getRogueData().setProgressId(id);
                                    syncRogueData();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable("commands.beyond.config.set.currentProgress", id.toString()),
                                            true);
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            var id = getRogueData().getProgressId();
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("commands.beyond.config.get.currentProgress",
                                            id != null ? id.toString() : "-"),
                                    false);
                            return 1;
                        })
                );
    }

    private static RogueData getRogueData() {
        return BeyondAPI.getRogueData(BeyondAPI.getOverWorld());
    }

    private static void syncRogueData() {
        BeyondAPI.syncGlobalData(BeyondAPI.getOverWorld());
    }

    private BeyondConfigCommand() {
    }
}
