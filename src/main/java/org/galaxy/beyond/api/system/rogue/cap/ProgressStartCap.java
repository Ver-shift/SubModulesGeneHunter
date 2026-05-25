package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.BeyondItemInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.Phase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.rogue.player.RoguePlayerManager;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 战利品袋流程 Cap。
 * <p>
 * 管理肉鸽开局流程：离开安全区 → 冷却检查 → 发放战利品袋 → 开袋装备 → PRE_ROGUE 就绪检测 → 推进到 INIT。
 * <p>
 * 通过 {@code @SubscribeEvent} 监听右键物品（战利品袋），
 * 通过 {@code phaseTick} 轮询 PRE_ROGUE 就绪状态。
 */
@EventBusSubscriber
public class ProgressStartCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("progress_start");

    private int preRogueTickCounter;

    public ProgressStartCap() { super(ID); }

    // ============================================================
    // Zone 事件
    // ============================================================

    @Override
    public void changeZone(LivingEntity entity, ZoneType from, ZoneType to, IRogueContext ctx) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (from == ZoneType.Safe_Zone && to != ZoneType.Safe_Zone) {
            tryLeaveSafeZone(player);
        } else if (from != ZoneType.Safe_Zone && to == ZoneType.Safe_Zone) {
            returnToSafeZone(player, ctx);
        }
    }

    // ============================================================
    // Phase 事件
    // ============================================================

    @Override
    public void phaseEnter(ServerLevel level, Phase from, Phase to, IRogueContext ctx) {
        if (to == RoguePhase.PRE_ROGUE) {
            preRogueTickCounter = 0;
        }
    }

    @Override
    public void phaseTick(ServerLevel level, Phase phase, IRogueContext ctx) {
        if (phase != RoguePhase.PRE_ROGUE) return;

        preRogueTickCounter++;
        if (preRogueTickCounter % 20 != 0) return;

        var players = ctx.playersInRogue(level);
        if (players.isEmpty()) return;

        int total = players.size();
        int ready = countReady(level, ctx);
        if (ready < total) {
            for (var p : players) {
                p.sendSystemMessage(Component.translatable("beyond.rogue.ready_status", ready, total));
            }
            return;
        }

        if (!ctx.getRogueData(level).hasProgressId()) {
            level.getServer().getPlayerList()
                    .broadcastSystemMessage(Component.translatable("beyond.rogue.start.no_progress"), false);
            return;
        }
        ctx.setPhase(level, RoguePhase.INIT);
    }

    // ============================================================
    // 战利品袋使用 → @SubscribeEvent
    // ============================================================

    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItemStack().is(BeyondItemInit.LOOT_BAG.get())) {
            tryOpenLootBag(player, new RogueContext());
        }
    }

    // ============================================================
    // 离开安全区 → 冷却检查 → 发放战利品袋
    // ============================================================

    public Step tryLeaveSafeZone(ServerPlayer player) {
        if (isInRogue(player)) return Step.NOT_IN_ROGUE;

        var data = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
        long now = player.level().getGameTime();
        long cd = CommonConfig.LOBBY_COOLDOWN_SECONDS.get() * 20L;
        if (data.getLastSafeZoneReturnTime() > 0 && now - data.getLastSafeZoneReturnTime() < cd) {
            long remain = cd - (now - data.getLastSafeZoneReturnTime());
            player.sendSystemMessage(Component.translatable("beyond.info.leave_too_frequent", (remain + 19) / 20));
            return Step.COOLDOWN;
        }

        var rogueData = BeyondAPI.getRogueData(player.level());
        if (!rogueData.hasProgressId()) {
            player.sendSystemMessage(Component.translatable("beyond.rogue.start.no_progress"));
            return Step.NO_PROGRESS;
        }

        rogueData.addRoguePlayer(player.getUUID());
        BeyondAPI.syncGlobalData((ServerLevel) player.level());
        RoguePlayerManager.giveItem(player, BeyondItemInit.LOOT_BAG.get());
        return Step.BAG_GIVEN;
    }

    // ============================================================
    // 打开战利品袋 → 装备 → 全员就绪检测
    // ============================================================

    public static Step tryOpenLootBag(ServerPlayer player, IRogueContext ctx) {
        // 不在肉鸽列表
        if (!isInRogue(player)) return Step.NOT_IN_ROGUE;

        PlayerPhase playerPhase = ctx.getPlayerPhase(player);

        // 已经开过袋
        if (playerPhase == PlayerPhase.PRE_ROGUE) {
            player.sendSystemMessage(Component.translatable("beyond.rogue.already_opened"));
            return Step.ALREADY_OPENED;
        }

        // 游戏已在进行中
        if (playerPhase == PlayerPhase.ON_PROGRESS) {
            player.sendSystemMessage(Component.translatable("beyond.rogue.game_in_progress"));
            return Step.GAME_IN_PROGRESS;
        }

        // 全局 phase 已离开 PRE_ROGUE（游戏已由其他人触发开始）
        ServerLevel level = player.level();
        RoguePhase globalPhase = ctx.getPhase(level);
        if (globalPhase != RoguePhase.PRE_ROGUE && globalPhase != RoguePhase.LOBBY) {
            player.sendSystemMessage(Component.translatable("beyond.rogue.game_already_started"));
            return Step.GAME_ALREADY_STARTED;
        }

        // 首次开袋：给装备，设为 PRE_ROGUE
        RoguePlayerManager.giveItem(player, Items.IRON_SWORD);
        ctx.setPlayerPhase(player, PlayerPhase.PRE_ROGUE);

        // 确保全局 phase 进入 PRE_ROGUE（第一个人开袋时触发）
        if (globalPhase != RoguePhase.PRE_ROGUE) {
            ctx.setPhase(level, RoguePhase.PRE_ROGUE);
        }

        int total = ctx.playersInRogue(level).size();
        int ready = countReady(level, ctx);
        player.sendSystemMessage(Component.translatable("beyond.rogue.player_ready", ready, total));

        if (ready >= total) {
            ctx.setPhase(level, RoguePhase.INIT);
            return Step.ALL_READY;
        }

        player.sendSystemMessage(Component.translatable("beyond.rogue.waiting_players", total - ready));
        return Step.WAITING_OTHERS;
    }

    // ============================================================
    // 返回安全区 → 回收
    // ============================================================

    public int returnToSafeZone(ServerPlayer player, IRogueContext ctx) {
        int val = RoguePlayerManager.clearInventory(player);
        if (val > 0) {
            player.sendSystemMessage(Component.translatable("beyond.info.back_to_safe_zone", val));
        }
        var data = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData();
        data.setLastSafeZoneReturnTime(player.level().getGameTime());
        ctx.setPlayerPhase(player, PlayerPhase.LOBBY);
        var rogueData = ctx.getRogueData(player.level());
        rogueData.removeRoguePlayer(player.getUUID());
        BeyondAPI.syncGlobalData(player.level());

        if (rogueData.getRoguePlayerIds().isEmpty()) {
            rogueData.setProgressActive(false);
            BeyondAPI.syncGlobalData(player.level());
            ctx.setPhase(player.level(), RoguePhase.LOBBY);
        }
        return val;
    }

    // ============================================================
    // 查询
    // ============================================================

    private static boolean isInRogue(ServerPlayer player) {
        return BeyondAPI.getRogueData(player.level())
                .getRoguePlayerIds().contains(player.getUUID());
    }

    public static int countReady(ServerLevel level, IRogueContext ctx) {
        int c = 0;
        for (var p : ctx.playersInRogue(level))
            if (ctx.getPlayerPhase(p) == PlayerPhase.PRE_ROGUE) c++;
        return c;
    }

    // ============================================================
    // 结果枚举 —— 每个分支都有明确语义
    // ============================================================

    public enum Step {
        /** 冷却中，离开安全区太频繁 */
        COOLDOWN,
        /** 未设置关卡 */
        NO_PROGRESS,
        /** 已发放战利品袋 */
        BAG_GIVEN,
        /** 不在肉鸽玩家列表中 */
        NOT_IN_ROGUE,
        /** 已经开过战利品袋 */
        ALREADY_OPENED,
        /** 游戏已在进行中 */
        GAME_IN_PROGRESS,
        /** 游戏已被其他玩家触发开始 */
        GAME_ALREADY_STARTED,
        /** 等待其他玩家开袋 */
        WAITING_OTHERS,
        /** 全员就绪，游戏开始 */
        ALL_READY,
        /** 已回收物品，返回安全区 */
        RECYCLED
    }
}
