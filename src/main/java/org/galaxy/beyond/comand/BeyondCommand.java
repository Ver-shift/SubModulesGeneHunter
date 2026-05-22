package org.galaxy.beyond.comand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.init.BeyondAttachmentInit;

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

                                    var dimData = BeyondAPI.getBeyondDimensionData(level);
                                    if (dimData == null || dimData.getSafeZoneStructureData().getInitialized() < 1) {
                                        source.sendFailure(Component.translatable("commands.beyond.home.not_found"));
                                        return 0;
                                    }

                                    BlockPos spawnPos = dimData.getSafeZoneStructureData().getSpawnPos();
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
                                // ---- debug ----
                                .then(Commands.literal("debug")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    boolean v = BoolArgumentType.getBool(ctx, "value");
                                                    var cfg = cfg(ctx);
                                                    cfg.setDebugMode(v);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.set.debug", v),
                                                            true);
                                                    syncCfg();
                                                    return 1;
                                                })
                                        )
                                        .executes(ctx -> {
                                                    boolean v = cfg(ctx).isDebugMode();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.get.debug", v),
                                                            false);
                                                    return 1;
                                        })
                                )
                                // ---- safeZoneSize ----
                                .then(Commands.literal("safeZoneSize")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(4, 256))
                                                .executes(ctx -> {
                                                    int v = IntegerArgumentType.getInteger(ctx, "value");
                                                    cfg(ctx).setSafeZoneSize(v);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.set.safeZoneSize", v),
                                                            true);
                                                    syncCfg();
                                                    return 1;
                                                })
                                        )
                                        .executes(ctx -> {
                                                    int v = cfg(ctx).getSafeZoneSize();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.get.safeZoneSize", v),
                                                            false);
                                                    return 1;
                                        })
                                )
                                // ---- minNodeExpandCount ----
                                .then(Commands.literal("minNodeExpandCount")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 1000))
                                                .executes(ctx -> {
                                                    int v = IntegerArgumentType.getInteger(ctx, "value");
                                                    cfg(ctx).setMinNodeExpandCount(v);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.set.minNodeExpandCount", v),
                                                            true);
                                                    syncCfg();
                                                    return 1;
                                                })
                                        )
                                        .executes(ctx -> {
                                                    int v = cfg(ctx).getMinNodeExpandCount();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.get.minNodeExpandCount", v),
                                                            false);
                                                    return 1;
                                        })
                                )
                                // ---- minNodeExpandChunks ----
                                .then(Commands.literal("minNodeExpandChunks")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> {
                                                    int v = IntegerArgumentType.getInteger(ctx, "value");
                                                    cfg(ctx).setMinNodeExpandChunks(v);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.set.minNodeExpandChunks", v),
                                                            true);
                                                    syncCfg();
                                                    return 1;
                                                })
                                        )
                                        .executes(ctx -> {
                                                    int v = cfg(ctx).getMinNodeExpandChunks();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.get.minNodeExpandChunks", v),
                                                            false);
                                                    return 1;
                                        })
                                )
                                // ---- maxNodeExpandRange ----
                                .then(Commands.literal("maxNodeExpandRange")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 200))
                                                .executes(ctx -> {
                                                    int v = IntegerArgumentType.getInteger(ctx, "value");
                                                    cfg(ctx).setMaxNodeExpandRange(v);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.set.maxNodeExpandRange", v),
                                                            true);
                                                    syncCfg();
                                                    return 1;
                                                })
                                        )
                                        .executes(ctx -> {
                                                    int v = cfg(ctx).getMaxNodeExpandRange();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.get.maxNodeExpandRange", v),
                                                            false);
                                                    return 1;
                                        })
                                )
                                // ---- rogueDimension ----
                                .then(Commands.literal("rogueDimension")
                                        .then(Commands.argument("value", StringArgumentType.string())
                                                .suggests((ctx, builder) -> {
                                                    for (var level : ctx.getSource().getServer().getAllLevels()) {
                                                        builder.suggest("\"" + level.dimension().identifier().toString() + "\"");
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    String s = StringArgumentType.getString(ctx, "value");
                                                    var id = Identifier.parse(s);
                                                    var rk = net.minecraft.resources.ResourceKey.create(Registries.DIMENSION, id);
                                                    cfg(ctx).setRogueDimension(rk);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.set.rogueDimension", s),
                                                            true);
                                                    syncCfg();
                                                    return 1;
                                                })
                                        )
                                        .executes(ctx -> {
                                                    var rk = cfg(ctx).getRogueDimension();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.get.rogueDimension",
                                                                    rk.identifier().toString()),
                                                            false);
                                                    return 1;
                                        })
                                )
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
                                                    var def = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueDefinition();
                                                    var error = def.validateProgress(id);
                                                    if (error != null) {
                                                        ctx.getSource().sendFailure(error);
                                                        return 0;
                                                    }
                                                    cfg(ctx).setCurrentProgress(id);
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.set.currentProgress", id.toString()),
                                                            true);
                                                    syncCfg();
                                                    return 1;
                                                })
                                        )
                                        .executes(ctx -> {
                                                    var id = cfg(ctx).getCurrentProgress();
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
                                                            boolean added = cfg(ctx).addRoguePlayer(player.getUUID());
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable(added
                                                                                    ? "commands.beyond.config.playerList.added"
                                                                                    : "commands.beyond.config.playerList.already_exists",
                                                                            name),
                                                                    true);
                                                            syncCfg();
                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("player", StringArgumentType.word())
                                                        .suggests((ctx, builder) -> {
                                                            for (UUID id : cfg(ctx).getRoguePlayerIds()) {
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
                                                            boolean removed = cfg(ctx).removeRoguePlayer(id);
                                                            ctx.getSource().sendSuccess(
                                                                    () -> Component.translatable(removed
                                                                                    ? "commands.beyond.config.playerList.removed"
                                                                                    : "commands.beyond.config.playerList.not_in_list",
                                                                            input),
                                                                    true);
                                                            syncCfg();
                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("list")
                                                .executes(ctx -> {
                                                    Set<UUID> ids = cfg(ctx).getRoguePlayerIds();
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
                                                            boolean added = cfg(ctx).addSafeZonePlayer(player.getUUID());
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
                                                            for (UUID id : cfg(ctx).getSafeZonePlayerIds()) {
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
                                                            boolean removed = cfg(ctx).removeSafeZonePlayer(id);
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
                                                    Set<UUID> ids = cfg(ctx).getSafeZonePlayerIds();
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
                                // ---- reset ----
                                .then(Commands.literal("reset")
                                        .executes(ctx -> {
                                                    cfg(ctx).reset();
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.translatable("commands.beyond.config.reset"),
                                                            true);
                                                    return 1;
                                        })
                                )
                        )
        );
    }

    private static org.galaxy.beyond.api.config.RogueConfig cfg(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        return BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
    }

    private static void syncCfg() {
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        if (cfg.isDirty()) {
            BeyondAPI.getOverWorld().syncData(BeyondAttachmentInit.GLOBAL_DATA.get());
            cfg.markSynced();
        }
    }
}
