package com.pz.beyond.api.event.handle;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.event.custom.PlayerFirstLoggedInEvent;
import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.init.BeyondItems;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.BeyondManager;
import com.pz.beyond.api.system.progress.core.ProgressSession;
import com.pz.beyond.api.system.progress.core.SessionState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
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
            Beyond.debugLog("PlayerFirstLoggedInEvent entity is not ServerPlayer, skipping");
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

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && BeyondAttachInit.isAllowedDimension(serverLevel)) {
            BeyondAPI.getBeyondManager().onChunkLoad(serverLevel, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!BeyondAttachInit.isAllowedDimension(serverLevel)) {
            return;
        }
        BeyondAPI.getBeyondManager().getZoneManager().handlePlayerRightClickBlock(serverPlayer, event.getPos());
    }

    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!BeyondAttachInit.isAllowedDimension(serverLevel)) {
            return;
        }
        BeyondAPI.getBeyondManager().getZoneManager().handleMobTick(mob);
    }

    /** 战利品袋右键：启动一局游戏（INACTIVE → WARMUP → IN_PROGRESS） */
    @SubscribeEvent
    public static void onLootBagUse(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!BeyondAttachInit.isAllowedDimension(serverLevel)) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!stack.is(BeyondItems.LOOT_BAG.get())) {
            return;
        }

        ProgressSession session = BeyondAPI.getBeyondManager().getProgressManager().getSession();

        // 守卫：只能从 INACTIVE 启动
        if (session.getSessionState() != SessionState.INACTIVE) {
            player.displayClientMessage(Component.translatable("beyond.loot_bag.already_active"), false);
            return;
        }

        // 获取当前选中的 Progress ID（从 progressDefinitions 中取第一个）
        BeyondLevelData levelData = BeyondAPI.getBeyondLevelData(serverLevel);
        if (levelData == null || levelData.getProgressDefinitions().isEmpty()) {
            player.displayClientMessage(Component.translatable("beyond.loot_bag.no_progress"), false);
            return;
        }
        ResourceLocation progressId = levelData.getProgressDefinitions().keySet().iterator().next();

        // 启动 Session
        boolean started = session.startSession(progressId, serverLevel);
        if (started) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            player.displayClientMessage(Component.translatable("beyond.loot_bag.start"), false);
        }
    }
}
