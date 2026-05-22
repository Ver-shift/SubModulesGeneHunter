package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.IRogueContext;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;
import org.galaxy.beyond.api.system.zone.ZoneType;

/**
 * 玩家参战管理 Cap。
 * <p>
 * 维护肉鸽玩家列表和暂离玩家列表，通过 {@code changeZone} 事件自动添加/移除，
 * 并通过 {@code @SubscribeEvent} 监听登录、登出、维度切换事件。
 */
@EventBusSubscriber
public class PlayerInGameCap extends RogueCap {

    public static final Identifier ID = Beyond.asResource("player_in_game");

    public PlayerInGameCap() { super(ID); }

    @Override
    public void changeZone(LivingEntity entity, ZoneType from, ZoneType to, IRogueContext ctx) {
        if (!(entity instanceof ServerPlayer player)) return;
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        if (to == ZoneType.Safe_Zone) {
            cfg.addSafeZonePlayer(player.getUUID());
        } else if (from == ZoneType.Safe_Zone && to != ZoneType.Safe_Zone) {
            cfg.addRoguePlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
            cfg.removeRoguePlayer(player.getUUID());
            cfg.removeSafeZonePlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!isRogueDimension(player)) return;
            var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
            var phaseId = BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getPhaseId();
            if (!phaseId.equals(PlayerPhase.LOBBY.getId())) {
                cfg.addRoguePlayer(player.getUUID());
            } else {
                cfg.addSafeZonePlayer(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
            ResourceKey<Level> rogueDim = cfg.getRogueDimension();
            if (event.getTo().equals(rogueDim)) cfg.addSafeZonePlayer(player.getUUID());
            if (event.getFrom().equals(rogueDim) && !event.getTo().equals(rogueDim)) {
                cfg.removeRoguePlayer(player.getUUID());
                cfg.removeSafeZonePlayer(player.getUUID());
            }
        }
    }

    private static boolean isRogueDimension(ServerPlayer player) {
        return player.level().dimension()
                .equals(BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig().getRogueDimension());
    }
}
