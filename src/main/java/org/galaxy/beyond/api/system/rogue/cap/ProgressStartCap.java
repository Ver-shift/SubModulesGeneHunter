package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.init.BeyondItemInit;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.player.RoguePlayerManager;
import org.galaxy.beyond.api.system.zone.CapType;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 游戏启动能力 —— 流式步骤模式。
 */
public class ProgressStartCap extends ZoneCapType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":progress_start");

    private final IRogueContext ctx = new RogueContext();

    public ProgressStartCap() { super(ID); }

    @Override public int getMaxLevel() { return 1; }
    @Override public CapType getCapType() { return CapType.NORMAL; }

    public enum Step {
        COOLDOWN, BAG_GIVEN, PRE_ROGUE_WAITING, GAME_STARTED, RECYCLED, NOT_IN_FLOW, ALREADY_READY
    }

    // ============================================================
    // Step 1-3：离开安全区 → 冷却检查 → 发袋
    // ============================================================

    public Step tryLeaveSafeZone(ServerPlayer player) {
        if (isInRogue(player)) return Step.NOT_IN_FLOW;

        var data = player.getData(org.galaxy.beyond.api.init.BeyondAttachmentInit.PLAYER_DATA.get())
                .getPlayerRogueData();
        long now = player.level().getGameTime();
        long cd = CommonConfig.LOBBY_COOLDOWN_SECONDS.get() * 20L;
        if (now - data.getLastLeaveSafeZoneTime() < cd) {
            long remain = cd - (now - data.getLastLeaveSafeZoneTime());
            player.sendSystemMessage(Component.translatable("beyond.info.leave_too_frequent", (remain + 19) / 20));
            return Step.COOLDOWN;
        }

        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        cfg.addRoguePlayer(player.getUUID());
        RoguePlayerManager.giveItem(player, BeyondItemInit.LOOT_BAG.get());
        data.setLastLeaveSafeZoneTime(now);
        return Step.BAG_GIVEN;
    }

    // ============================================================
    // Step 4-5：打开战利品袋 → 装备 → PreRogue
    // ============================================================

    public Step tryOpenLootBag(ServerPlayer player) {
        if (!isInRogue(player)) return Step.NOT_IN_FLOW;

        PlayerPhase cur = ctx.getPlayerPhase(player);
        if (cur == BeyondPhaseInit.PLAYER_PRE_ROGUE.get()
                || cur == BeyondPhaseInit.PLAYER_ON_PROGRESS.get()) {
            player.sendSystemMessage(Component.translatable("beyond.rogue.already_ready"));
            return Step.ALREADY_READY;
        }

        RoguePlayerManager.giveItem(player, Items.IRON_SWORD);
        ctx.setPlayerPhase(player, BeyondPhaseInit.PLAYER_PRE_ROGUE.get());

        ServerLevel level = player.level();
        int total = ctx.playersInRogue(level).size();
        int ready = countReady(level);
        player.sendSystemMessage(Component.translatable("beyond.rogue.player_ready", ready, total));

        if (ready >= total) {
            ctx.setPhase(level, BeyondPhaseInit.ROGUE_INIT.get());
            return Step.GAME_STARTED;
        }
        player.sendSystemMessage(Component.translatable("beyond.rogue.waiting_players", total - ready));
        return Step.PRE_ROGUE_WAITING;
    }

    // ============================================================
    // Step 6-7：全员就绪 → 关卡初始化
    // ============================================================

    public Step tryStartGame(ServerLevel level) {
        if (countReady(level) < ctx.playersInRogue(level).size())
            return Step.PRE_ROGUE_WAITING;
        ctx.setPhase(level, BeyondPhaseInit.ROGUE_INIT.get());
        return Step.GAME_STARTED;
    }

    // ============================================================
    // 分支：返回安全区 → 回收
    // ============================================================

    public int returnToSafeZone(ServerPlayer player) {
        int val = RoguePlayerManager.clearInventory(player);
        if (val > 0) {
            player.sendSystemMessage(Component.translatable("beyond.info.back_to_safe_zone", val));
        }
        ctx.setPlayerPhase(player, BeyondPhaseInit.PLAYER_LOBBY.get());
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        cfg.removeRoguePlayer(player.getUUID());

        // 全体退出 → 重置全局 Phase
        if (cfg.getRoguePlayerIds().isEmpty()) {
            ctx.setPhase(player.level(), BeyondPhaseInit.ROGUE_LOBBY.get());
        }
        return val;
    }

    // ============================================================
    // 列表查询
    // ============================================================

    private static boolean isInRogue(ServerPlayer player) {
        return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhase()
                != BeyondPhaseInit.PLAYER_LOBBY.get();
    }

    // ============================================================
    // Cap 事件 hook
    // ============================================================

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        if (from == ZoneType.Safe_Zone && to != ZoneType.Safe_Zone) {
            tryLeaveSafeZone(player);
        } else if (from != ZoneType.Safe_Zone && to == ZoneType.Safe_Zone) {
            returnToSafeZone(player);
        }
    }

    @Override
    public void playerUseItem(ServerPlayer player, Item item) {
        if (item == BeyondItemInit.LOOT_BAG.get()) {
            tryOpenLootBag(player);
        }
    }

    // ============================================================
    // 查询
    // ============================================================

    public boolean isAllReady(ServerLevel level) {
        return countReady(level) >= ctx.playersInRogue(level).size();
    }

    public int countReady(ServerLevel level) {
        int c = 0;
        for (var p : ctx.playersInRogue(level))
            if (ctx.getPlayerPhase(p) == BeyondPhaseInit.PLAYER_PRE_ROGUE.get()) c++;
        return c;
    }
}
