package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.BeyondAPI;
import org.galaxy.beyond.api.system.zone.CapType;
import org.galaxy.beyond.api.system.zone.ZoneCapType;
import org.galaxy.beyond.api.system.zone.ZoneType;

@EventBusSubscriber
public class PlayerInGameCap extends ZoneCapType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":player_in_game");

    public PlayerInGameCap() { super(ID); }

    @Override public int getMaxLevel() { return 1; }
    @Override public CapType getCapType() { return CapType.NORMAL; }

    // ============================================================
    // Zone 切换 → 列表切换
    // ============================================================

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
        if (to == ZoneType.Safe_Zone) {
            cfg.addSafeZonePlayer(player.getUUID());
        } else if (from == ZoneType.Safe_Zone && to != ZoneType.Safe_Zone) {
            cfg.addRoguePlayer(player.getUUID());
        }
    }

    // ============================================================
    // 登出 → 暂离列表（保证多人判定正确）
    // ============================================================

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();
            cfg.removeRoguePlayer(player.getUUID());
            cfg.removeSafeZonePlayer(player.getUUID());
        }
    }

    // ============================================================
    // 登录 → 恢复退出前的列表状态
    // ============================================================

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (!isRogueDimension(player)) return;
            var cfg = BeyondAPI.getGlobalData(BeyondAPI.getOverWorld()).getRogueConfig();

            // 读取持久化的列表状态恢复
            boolean wasInRogue = cfg.getRoguePlayerIds().contains(player.getUUID());
            boolean wasInSafe  = cfg.getSafeZonePlayerIds().contains(player.getUUID());
            if (wasInRogue) { cfg.addRoguePlayer(player.getUUID()); return; }
            if (wasInSafe)  { cfg.addSafeZonePlayer(player.getUUID()); return; }
            cfg.addSafeZonePlayer(player.getUUID());
        }
    }

    // ============================================================
    // 维度切换
    // ============================================================

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
