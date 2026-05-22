package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.Phase;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.rogue.player.RoguePlayerManager;

import java.util.List;

/**
 * 玩家结算 Cap —— 场景结束后逐人结算，点击确认，全员确认后传送回安全区。
 * <p>
 * 确认状态使用 {@link PlayerPhase}：{@code PROGRESS_FINISH=待确认, REWARD=已确认}。
 * 状态全部实例化，无静态字段。
 */
@EventBusSubscriber
public class PlayerProgressFinishCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("player_progress_finish");

    private ServerLevel activeLevel;
    private IRogueContext activeCtx;

    public PlayerProgressFinishCap() { super(ID); }

    // ============================================================
    // Phase 事件
    // ============================================================

    @Override
    public void phaseEnter(ServerLevel level, Phase from, Phase to, IRogueContext ctx) {
        if (to != RoguePhase.PROGRESS_FINISH) return;

        activeLevel = level;
        activeCtx = ctx;

        var players = ctx.playersInRogue(level);
        if (players.isEmpty()) return;

        for (var p : players) {
            int reward = RoguePlayerManager.clearInventory(p);
            ctx.setPlayerPhase(p, PlayerPhase.PROGRESS_FINISH);
            p.sendSystemMessage(Component.translatable("beyond.settle.confirm_prompt", reward)
                    .append(Component.literal(" "))
                    .append(Component.translatable("beyond.settle.click_confirm")
                            .withStyle(style -> style
                                    .withClickEvent(new ClickEvent.RunCommand("/beyondsettle confirm"))
                                    .withUnderlined(true)
                                    .withColor(0x55FF55))));
        }

        broadcastProgress();
    }

    // ============================================================
    // 确认逻辑
    // ============================================================

    private void onConfirm(ServerPlayer player) {
        if (activeLevel == null) return;

        if (activeCtx.getPlayerPhase(player) == PlayerPhase.REWARD) {
            player.sendSystemMessage(Component.translatable("beyond.settle.already_confirmed"));
            return;
        }

        activeCtx.setPlayerPhase(player, PlayerPhase.REWARD);

        var players = activeCtx.playersInRogue(activeLevel);
        int total = players.size();
        int done = countReward(players);

        activeLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.settle.player_confirmed", player.getDisplayName(), done, total), false);

        if (done >= total) {
            teleportAllToSafeZone(players);
            activeCtx.setPhase(activeLevel, RoguePhase.LOBBY);
            for (var p : players)
                activeCtx.setPlayerPhase(p, PlayerPhase.LOBBY);
            activeLevel.getServer().getPlayerList().broadcastSystemMessage(
                    Component.translatable("beyond.settle.all_confirmed"), false);
            activeLevel = null;
            activeCtx = null;
        }
    }

    private void broadcastProgress() {
        var players = activeCtx.playersInRogue(activeLevel);
        int total = players.size();
        int done = countReward(players);
        activeLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("beyond.settle.progress", done, total), false);
    }

    private int countReward(List<ServerPlayer> players) {
        int c = 0;
        for (var p : players)
            if (activeCtx.getPlayerPhase(p) == PlayerPhase.REWARD) c++;
        return c;
    }

    // ============================================================
    // 传送
    // ============================================================

    private void teleportAllToSafeZone(List<ServerPlayer> players) {
        var dimData = BeyondAPI.getBeyondDimensionData(activeLevel);
        if (dimData == null) return;

        BlockPos spawnPos = dimData.getSafeZoneStructureData().getSpawnPos();
        if (spawnPos.equals(BlockPos.ZERO)) return;

        for (var p : players) {
            p.teleportTo(activeLevel, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    java.util.Set.of(), p.getYRot(), p.getXRot(), true);
            var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
            cfg.removeRoguePlayer(p.getUUID());
        }
    }

    // ============================================================
    // 查找活跃实例
    // ============================================================

    private static PlayerProgressFinishCap findActive(ServerLevel level) {
        var caps = BeyondAPI.getBeyondDimensionData(level).getRogueData().getRogueCapData();
        for (var cd : caps) {
            if (cd.getCap() instanceof PlayerProgressFinishCap cap && cap.activeLevel != null)
                return cap;
        }
        return null;
    }

    // ============================================================
    // 命令注册
    // ============================================================

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("beyondsettle")
                        .then(Commands.literal("confirm")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        var cap = findActive(player.level());
                                        if (cap != null) cap.onConfirm(player);
                                        return 1;
                                    }
                                    return 0;
                                }))
        );
    }
}
