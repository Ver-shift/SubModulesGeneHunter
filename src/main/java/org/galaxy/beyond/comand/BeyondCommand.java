package org.galaxy.beyond.comand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.galaxy.beyond.api.BeyondAPI;

import java.util.Set;

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
        );
    }
}
