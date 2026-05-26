package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 玩家参战管理 Cap。
 * <p>
 * 维护肉鸽玩家列表和暂离玩家列表，通过 {@code changeZone} 事件维护安全区状态，
 * 并通过 {@code @SubscribeEvent} 监听登录、登出、维度切换事件。
 */
@EventBusSubscriber
public class PlayerInGameCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("player_in_game");

    public PlayerInGameCap() { super(ID); }

    @Override
    public void changeZone(LivingEntity entity, ZoneType from, ZoneType to, IRogueContext ctx) {
        if (!(entity instanceof ServerPlayer player)) return;
        var rogueData = BeyondAPI.getRogueData(entity.level());
        boolean changed = false;
        if (to == ZoneType.Safe_Zone) {
            changed = rogueData.addSafeZonePlayer(player.getUUID());
            changed |= rogueData.removeRoguePlayer(player.getUUID());
        }
        if (changed) BeyondAPI.syncGlobalData(player.level());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var rogueData = BeyondAPI.getRogueData(player.level());
            boolean changed = rogueData.removeRoguePlayer(player.getUUID());
            changed |= rogueData.removeSafeZonePlayer(player.getUUID());
            if (changed) BeyondAPI.syncGlobalData(player.level());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!isRogueDimension(player)) return;
            var rogueData = BeyondAPI.getRogueData(player.level());
            var phaseId = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhaseId();
            boolean changed;
            if (!phaseId.equals(PlayerPhase.LOBBY.getId())) {
                changed = rogueData.addRoguePlayer(player.getUUID());
            } else {
                changed = rogueData.addSafeZonePlayer(player.getUUID());
            }
            if (changed) BeyondAPI.syncGlobalData(player.level());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var rogueDim = CommonConfig.getRogueDimension();
            var rogueData = BeyondAPI.getRogueData(BeyondAPI.getOverWorld());
            boolean changed = false;
            if (event.getTo().equals(rogueDim)) changed = rogueData.addSafeZonePlayer(player.getUUID());
            if (event.getFrom().equals(rogueDim) && !event.getTo().equals(rogueDim)) {
                changed |= rogueData.removeRoguePlayer(player.getUUID());
                changed |= rogueData.removeSafeZonePlayer(player.getUUID());
            }
            if (changed) BeyondAPI.syncGlobalData(BeyondAPI.getOverWorld());
        }
    }

    private static boolean isRogueDimension(ServerPlayer player) {
        return player.level().dimension().equals(CommonConfig.getRogueDimension());
    }
}
