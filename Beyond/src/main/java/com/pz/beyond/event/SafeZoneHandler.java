package com.pz.beyond.event;

import com.pz.beyond.data.PlayerStateData;
import com.pz.beyond.data.SafeZoneData;
import com.pz.beyond.event.custom.PlayerStateChangeEvent;
import com.pz.beyond.network.SafeZonePayload;
import com.pz.beyond.registry.ModAttachments;
import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import com.pz.beyond.util.SafeZonePayloadUtil;
import com.pz.beyond.util.SafeZoneRuleUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber
public class SafeZoneHandler {

    private static final List<ISafeZoneRuleListener> RULES = SafeZoneRuleUtil.getRULES();
    /**
     * 这个事件控制 安全区的初始数据
     * @param event
     */
    @SubscribeEvent
    public static void onServerStartedEvent(ServerStartedEvent event) {
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        SafeZoneData safeZone = overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(SafeZoneData::create, SafeZoneData::load), "SafeZone");
        if (!safeZone.isFirst()){
            safeZone.updateCenter(overworld.getLevel().getSharedSpawnPos(),event.getServer().overworld());
            safeZone.addRadius(15,event.getServer().overworld());
            safeZone.setFirst(true);
        }
    }



    /**
     * 处理玩家登录事件，将安全区数据同步给新加入的玩家
     * @param event
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            SafeZoneData data = serverPlayer.getServer().getLevel(Level.OVERWORLD).getDataStorage()
                    .computeIfAbsent(new SavedData.Factory<>(SafeZoneData::create, SafeZoneData::load), "SafeZone");
            SafeZonePayloadUtil.safeZoneToPlayer(serverPlayer,data);
        }
    }

    @SubscribeEvent
    public static void onMobSpawnEvent(MobSpawnEvent.PositionCheck event) {
        for (ISafeZoneRuleListener rule : RULES) {
            rule.onMobSpawn(event);
        }
    }
}
