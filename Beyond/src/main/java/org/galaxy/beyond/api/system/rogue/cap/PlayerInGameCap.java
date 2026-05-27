package org.galaxy.beyond.api.system.rogue.cap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.config.CommonConfig;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.PlayerPhase;
import org.galaxy.beyond.api.system.rogue.core.RogueCap;

/**
 * 玩家参战管理 Cap。
 * <p>
 * 通过 {@code @SubscribeEvent} 监听维度切换事件，离开肉鸽维度时重置玩家状态。
 */
@EventBusSubscriber
public class PlayerInGameCap extends RogueCap {

    public static final ResourceLocation ID = Beyond.asResource("player_in_game");

    public PlayerInGameCap() { super(ID); }

    @SubscribeEvent
    public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var rogueDim = CommonConfig.getRogueDimension();
            if (event.getFrom().equals(rogueDim) && !event.getTo().equals(rogueDim)) {
                BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setPhase(PlayerPhase.LOBBY);
                BeyondAPI.syncPlayerData(player);
            }
        }
    }
}
