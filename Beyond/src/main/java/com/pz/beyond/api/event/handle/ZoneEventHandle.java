package com.pz.beyond.api.event.handle;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.event.custom.PlayerFirstLoggedInEvent;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.BeyondManager;
import com.pz.beyond.api.system.structure.StructureData;
import net.blay09.mods.balm.api.event.LevelLoadingEvent;
import net.blay09.mods.balm.api.event.PlayerLoginEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber
public class ZoneEventHandle {


    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!BeyondAttachInit.isAllowedDimension(serverLevel)) {
            return;
        }
        BeyondManager manager = BeyondAPI.getBeyondManager();
        manager.levelTick(serverLevel);
    }

    @SubscribeEvent
    public static void onPlayerFirstLoggedIn(PlayerFirstLoggedInEvent event) {
        // 步骤1: 验证实体是否为 ServerPlayer
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            Beyond.LOGGER.debug("PlayerFirstLoggedInEvent entity is not ServerPlayer, skipping");
            return;
        }
        BeyondAPI.getBeyondManager().playerFirstLoad(serverPlayer);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && BeyondAttachInit.isAllowedDimension(serverLevel)) {
            BeyondAPI.getBeyondManager().loadLevel(serverLevel);
        }
    }
}
