package com.pz.beyond.api.event.handle;

import com.pz.beyond.api.init.BeyondAttachInit;
import com.pz.beyond.api.system.BeyondData;
import com.pz.beyond.api.system.zone.LevelZoneData;
import com.pz.beyond.api.system.zone.zones.PlayerZoneData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 区域事件处理器
 * <p>
 * 注意：安全区初始化逻辑已移至 {@link com.pz.beyond.api.system.zone.LevelZoneData} 构造函数中
 */
@EventBusSubscriber
public class ZoneEventHandle {

    /**
     * 安全区初始化延迟（ticks）
     * 100 ticks = 5 秒
     */
    private static final long SAFE_ZONE_INIT_DELAY_TICKS = 100L;

    /**
     * 玩家区域检测间隔（ticks）
     * 4 ticks ≈ 0.2 秒
     */
    private static final int PLAYER_ZONE_CHECK_INTERVAL = 4;

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        // 延迟到 LevelTick 再初始化，避免世界加载早期结构查询不稳定。
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!BeyondAttachInit.isAllowedDimension(level)) return;
        if (level.getGameTime() < SAFE_ZONE_INIT_DELAY_TICKS) return;

        LevelZoneData zoneData = BeyondAttachInit.getLevelZoneData(level);
        if (zoneData == null) return;

        // 初始化安全区
        if (!zoneData.isSafeZoneInitialized()) {
            zoneData.initializeSafeZoneIfNeeded(level);
        }
        
        // 初始化 PendingZone（安全区初始化后）
        if (zoneData.isSafeZoneInitialized() && !zoneData.isPendingZoneInitialized()) {
            zoneData.initializePendingZone(level);
        }
    }

    /**
     * 区块加载事件 - 将区块添加到 PendingZone
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!BeyondAttachInit.isAllowedDimension(level)) return;
        
        LevelZoneData zoneData = BeyondAttachInit.getLevelZoneData(level);
        if (zoneData == null) return;
        
        // 只有在 PendingZone 初始化后才开始处理新区块
        if (!zoneData.isPendingZoneInitialized()) return;
        
        // 将新加载的区块添加到 PendingZone
        long chunkKey = event.getChunk().getPos().toLong();
        zoneData.addChunkToPendingZone(chunkKey);
    }

    /**
     * 玩家移动检测 - 每4 tick检测一次区域变化
     */
    @SubscribeEvent
    public static void onPlayerMove(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 每4 tick检测一次，减少性能开销
        if (player.tickCount % PLAYER_ZONE_CHECK_INTERVAL != 0) return;

        // 获取区域数据
        LevelZoneData levelZoneData = BeyondAttachInit.getLevelZoneData(player.level());
        if (levelZoneData == null) return;

        // 获取玩家数据并检测区域变化
        BeyondData beyondData = BeyondAttachInit.getBeyondData(player);
        if (beyondData != null) {
            beyondData.getPlayerZoneData().zoneTypeHandle(levelZoneData);
        }
    }

    /**
     * 玩家克隆（重生）事件 - 重新设置玩家引用
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 获取区域数据
        LevelZoneData levelZoneData = BeyondAttachInit.getLevelZoneData(player.level());
        if (levelZoneData == null) return;

        // 重生后立即检测区域
        BeyondData beyondData = BeyondAttachInit.getBeyondData(player);
        if (beyondData != null) {
            beyondData.getPlayerZoneData().zoneTypeHandle(levelZoneData);
        }
    }

    /**
     * 玩家传送事件 - 立即检测区域变化
     */
    @SubscribeEvent
    public static void onPlayerTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 获取区域数据
        LevelZoneData levelZoneData = BeyondAttachInit.getLevelZoneData(player.level());
        if (levelZoneData == null) return;

        // 传送后立即检测区域
        BeyondData beyondData = BeyondAttachInit.getBeyondData(player);
        if (beyondData != null) {
            beyondData.getPlayerZoneData().zoneTypeHandle(levelZoneData);
        }
    }

}
