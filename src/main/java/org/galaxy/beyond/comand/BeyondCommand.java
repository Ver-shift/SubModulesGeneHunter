package org.galaxy.beyond.comand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.RogueData;

import java.util.Set;
import java.util.UUID;

public class BeyondCommand {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("beyond")
                        .then(Commands.literal("home")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    ServerPlayer player = source.getPlayerOrException();
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
                                            Set.of(), player.getYRot(), player.getXRot(), true);
                                    source.sendSuccess(
                                            () -> Component.translatable("commands.beyond.home.success"),
                                            true
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("safezone")
                                .then(Commands.literal("expand")
                                        .then(Commands.argument("chunkSize", IntegerArgumentType.integer(2, 256))
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    ServerPlayer player = source.getPlayerOrException();
                                                    ServerLevel level = (ServerLevel) player.level();
                                                    int chunkSize = IntegerArgumentType.getInteger(ctx, "chunkSize");

                                                    if (chunkSize % 2 != 0) {
                                                        source.sendFailure(Component.translatable("commands.beyond.safezone.expand.odd"));
                                                        return 0;
                                                    }

                                                    var zoneManager = BeyondAPI.getBeyondManager().getZoneManager();
                                                    zoneManager.addSafeZone(level, chunkSize, player.blockPosition());
                                                    source.sendSuccess(
                                                            () -> Component.translatable("commands.beyond.safezone.expand.success", chunkSize),
                                                            true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("config")
                                // ---- currentProgress ----
                                .then(Commands.literal("currentProgress")
                                        .then(Commands.argument("value", StringArgumentType.string())
                                                .suggests((ctx, builder) -> {
                                                    var globalData = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld());
                                                    if (globalData == null) return builder.buildFuture();
                                                    var def = globalData.getRogueDefinition();
                                                    if (def == null) return builder.buildFuture();
                                                    def.getRogueProgress().keySet().forEach(k -> builder.suggest("\"" + k.toString() + "\""));
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    var id = Identifier.parse(StringArgumentType.getString(ctx, "value"));
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
                                // ---- playerList ----
                                .then(Commands.literal("playerList")
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("player", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            for (var p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                                                builder.suggest(p.getName().getString());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            String name = StringArgumentType.getString(ctx, "player");
                                                            var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                                                            if (player == null) {
                                                                ctx.getSource().sendFailure(Component.translatable("commands.beyond.config.playerList.not_found", name));
                                                                return 0;
                                                            }
                                                            boolean added = getRogueData().addRoguePlayer(player.getUUID());
                                                            if (added) syncRogueData();
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable(added
                                                                                    ? "commands.beyond.config.playerList.added"
                                                                                    : "commands.beyond.config.playerList.already_exists",
                                                                            name),
                                                                    true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("player", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            for (UUID id : getRogueData().getRoguePlayerIds()) {
                                                                builder.suggest(id.toString());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            String input = StringArgumentType.getString(ctx, "player");
                                                            UUID id;
                                                            try { id = UUID.fromString(input); }
                                                            catch (IllegalArgumentException e) {
                                                                var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(input);
                                                                if (player == null) {
                                                                    ctx.getSource().sendFailure(Component.translatable("commands.beyond.config.playerList.not_found", input));
                                                                    return 0;
                                                                }
                                                                id = player.getUUID();
                                                            }
                                                            boolean removed = getRogueData().removeRoguePlayer(id);
                                                            if (removed) syncRogueData();
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable(removed
                                                                                    ? "commands.beyond.config.playerList.removed"
                                                                                    : "commands.beyond.config.playerList.not_in_list",
                                                                            input),
                                                                    true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("list")
                                                .executes(ctx -> {
                                                    Set<UUID> ids = getRogueData().getRoguePlayerIds();
                                                    if (ids.isEmpty()) {
                                                        ctx.getSource().sendSuccess(
                                                                () -> Component.translatable("commands.beyond.config.playerList.empty"),
                                                                false);
                                                        return 1;
                                                    }
                                                    StringBuilder sb = new StringBuilder();
                                                    for (UUID id : ids) {
                                                        if (!sb.isEmpty()) sb.append(", ");
                                                        sb.append(id.toString());
                                                    }
                                                    String list = sb.toString();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.playerList.list", list),
                                                            false);
                                                    return 1;
                                                })
                                        )
                                )
                                // ---- safeZoneList ----
                                .then(Commands.literal("safeZoneList")
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("player", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            for (var p : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                                                                builder.suggest(p.getName().getString());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            String name = StringArgumentType.getString(ctx, "player");
                                                            var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(name);
                                                            if (player == null) {
                                                                ctx.getSource().sendFailure(Component.translatable("commands.beyond.config.safeZoneList.not_found", name));
                                                                return 0;
                                                            }
                                                            boolean added = getRogueData().addSafeZonePlayer(player.getUUID());
                                                            if (added) syncRogueData();
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable(added
                                                                                    ? "commands.beyond.config.safeZoneList.added"
                                                                                    : "commands.beyond.config.safeZoneList.already_exists",
                                                                            name),
                                                                    true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("player", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            for (UUID id : getRogueData().getSafeZonePlayerIds()) {
                                                                builder.suggest(id.toString());
                                                            }
                                                            return builder.buildFuture();
                                                        })
                                                        .executes(ctx -> {
                                                            String input = StringArgumentType.getString(ctx, "player");
                                                            UUID id;
                                                            try { id = UUID.fromString(input); }
                                                            catch (IllegalArgumentException e) {
                                                                var player = ctx.getSource().getServer().getPlayerList().getPlayerByName(input);
                                                                if (player == null) {
                                                                    ctx.getSource().sendFailure(Component.translatable("commands.beyond.config.safeZoneList.not_found", input));
                                                                    return 0;
                                                                }
                                                                id = player.getUUID();
                                                            }
                                                            boolean removed = getRogueData().removeSafeZonePlayer(id);
                                                            if (removed) syncRogueData();
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable(removed
                                                                                    ? "commands.beyond.config.safeZoneList.removed"
                                                                                    : "commands.beyond.config.safeZoneList.not_in_list",
                                                                            input),
                                                                    true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("list")
                                                .executes(ctx -> {
                                                    Set<UUID> ids = getRogueData().getSafeZonePlayerIds();
                                                    if (ids.isEmpty()) {
                                                        ctx.getSource().sendSuccess(
                                                                () -> Component.translatable("commands.beyond.config.safeZoneList.empty"),
                                                                false);
                                                        return 1;
                                                    }
                                                    StringBuilder sb = new StringBuilder();
                                                    for (UUID id : ids) {
                                                        if (!sb.isEmpty()) sb.append(", ");
                                                        sb.append(id.toString());
                                                    }
                                                    String list = sb.toString();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.safeZoneList.list", list),
                                                            false);
                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }

    private static RogueData getRogueData() {
        return BeyondAPI.getRogueData(BeyondAPI.getOverWorld());
    }

    private static void syncRogueData() {
        BeyondAPI.syncGlobalData(BeyondAPI.getOverWorld());
    }
}
