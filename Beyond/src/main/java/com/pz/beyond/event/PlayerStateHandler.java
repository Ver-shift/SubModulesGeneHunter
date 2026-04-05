package com.pz.beyond.event;

import com.pz.beyond.data.PlayerStateData;
import com.pz.beyond.data.SafeZoneData;
import com.pz.beyond.event.custom.PlayerStateChangeEvent;
import com.pz.beyond.registry.ModAttachments;
import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import com.pz.beyond.util.SafeZoneRuleUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber
public class PlayerStateHandler {

    private static final List<ISafeZoneRuleListener> RULES = SafeZoneRuleUtil.getRULES();

    @SubscribeEvent
    public static void onPlayerTickEvent(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerStateData data = serverPlayer.getData(ModAttachments.PLAYER_STATE);
            SafeZoneData safeZone = serverPlayer.level().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(SafeZoneData::create, SafeZoneData::load), "SafeZone"
            );
            ISafeZoneRuleListener.SafeZoneContext safeZoneContext = new ISafeZoneRuleListener.SafeZoneContext(serverPlayer);

            /**
             * 对玩家执行安全区规则
             */
            for (ISafeZoneRuleListener rule : RULES) {
                if (data.getPlayerState() == PlayerStateData.PlayerState.INSIDE_SAFETY_ZONE) {
                    rule.onSafeZoneTick(safeZoneContext);
                } else if (data.getPlayerState() == PlayerStateData.PlayerState.IN_GAME) {
                    rule.outSideSafeZoneTick(safeZoneContext);
                }
            }


            /**
             * 处理玩家状态切换，广播对应的事件
             * @see PlayerStateChangeEvent
             */
            boolean isWithinSafeZone = safeZone.isWithinSafeZone(player.getX(), player.getZ());
            PlayerStateData.PlayerState playerState = data.getPlayerState();

            if (isWithinSafeZone && playerState == PlayerStateData.PlayerState.IN_GAME) {
                data.setPlayerState(PlayerStateData.PlayerState.INSIDE_SAFETY_ZONE);
                player.setData(ModAttachments.PLAYER_STATE, data);
                NeoForge.EVENT_BUS.post(new PlayerStateChangeEvent(player, PlayerStateData.PlayerState.IN_GAME, PlayerStateData.PlayerState.INSIDE_SAFETY_ZONE));
            } else if (!isWithinSafeZone && playerState == PlayerStateData.PlayerState.INSIDE_SAFETY_ZONE) {
                data.setPlayerState(PlayerStateData.PlayerState.IN_GAME);
                player.setData(ModAttachments.PLAYER_STATE, data);
                NeoForge.EVENT_BUS.post(new PlayerStateChangeEvent(player, PlayerStateData.PlayerState.INSIDE_SAFETY_ZONE, PlayerStateData.PlayerState.IN_GAME));

            }
        }
    }

    /**
     * 安全区内免伤
     */
    @SubscribeEvent
    public static void onLivingDamageEvent(LivingDamageEvent.Pre event) {
        for (ISafeZoneRuleListener rule : RULES) {
            rule.invincible(event);
        }

    }

}
