package org.galaxy.beyond.comand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueData;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;

import java.util.concurrent.CompletableFuture;

public class BeyondCommand {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("beyond")
                        .then(Commands.literal("home")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
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
                                })
                        )
                        .then(BeyondCreativeCommand.create())
                        .then(Commands.literal("config")
                                .then(Commands.literal("currentProgress")
                                        .then(Commands.argument("value", StringArgumentType.string())
                                                .suggests((ctx, builder) -> {
                                                    var globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
                                                    if (globalData == null) return builder.buildFuture();
                                                    var def = globalData.getRogueDefinition();
                                                    if (def == null) return builder.buildFuture();
                                                    def.getRogueProgress().keySet().forEach(k -> builder.suggest("\"" + k + "\""));
                                                    return builder.buildFuture();
                                                })
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
                                )
                        )
                        .then(Commands.literal("playerPhase")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> suggestOnlinePlayers(ctx.getSource(), builder))
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "player");
                                                    var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                                                    if (player == null) {
                                                        ctx.getSource().sendFailure(Component.translatable("commands.beyond.playerPhase.not_found", name));
                                                        return 0;
                                                    }
                                                    PlayerPhase phase = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.playerPhase.get", name, phase.name()),
                                                            false);
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("player", StringArgumentType.word())
                                                .suggests((ctx, builder) -> suggestOnlinePlayers(ctx.getSource(), builder))
                                                .then(Commands.argument("phase", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            for (PlayerPhase phase : PlayerPhase.values()) {
                                                                builder.suggest(phase.name());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            String name = StringArgumentType.getString(ctx, "player");
                                                            var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                                                            if (player == null) {
                                                                ctx.getSource().sendFailure(Component.translatable("commands.beyond.playerPhase.not_found", name));
                                                                return 0;
                                                            }
                                                            String value = StringArgumentType.getString(ctx, "phase");
                                                            PlayerPhase phase = parsePlayerPhase(value);
                                                            if (phase == null) {
                                                                ctx.getSource().sendFailure(Component.translatable("commands.beyond.playerPhase.invalid", value));
                                                                return 0;
                                                            }
                                                            BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(phase);
                                                            BeyondAPI.syncPlayerData(player);
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable("commands.beyond.playerPhase.set", name, phase.name()),
                                                                    true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
        );
    }

    private static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandSourceStack source, SuggestionsBuilder builder) {
        for (var player : source.getServer().getPlayerList().getPlayers()) {
            builder.suggest(player.getName().getString());
        }
        return builder.buildFuture();
    }

    private static PlayerPhase parsePlayerPhase(String value) {
        for (PlayerPhase phase : PlayerPhase.values()) {
            if (phase.name().equalsIgnoreCase(value)) return phase;
        }
        return null;
    }

    private static RogueData getRogueData() {
        return BeyondAPI.getRogueData(BeyondAPI.getOverWorld());
    }

    private static void syncRogueData() {
        BeyondAPI.syncGlobalData(BeyondAPI.getOverWorld());
    }
}
