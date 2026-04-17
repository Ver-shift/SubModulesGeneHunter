package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.BeyondData;
import com.pz.beyond.api.system.progress.ProgressCatalog;
import com.pz.beyond.api.system.progress.ProgressManager;
import com.pz.beyond.api.system.zone.LevelZoneData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class BeyondAttachInit {

    // 附件类型注册器
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Beyond.MODID);

    /**
     * 允许挂载数据的维度白名单
     */
    private static final List<ResourceKey<Level>> ALLOWED_DIMENSIONS = List.of(
            Level.OVERWORLD
    );

    // ==================== 玩家附件 ====================

    /**
     * 玩家Beyond数据，挂载在 Player 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BeyondData>> BEYOND_DATA =
            ATTACHMENT_TYPES.register("beyond_data", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerPlayer player) {
                                    return new BeyondData(player);
                                }
                                return new BeyondData();
                            })
                            .serialize(BeyondData.CODEC)
                            .sync(BeyondData.STREAM_CODEC)
                            .copyOnDeath()
                            .build()
            );

    // ==================== Level 附件 ====================

    /**
     * 进度目录数据，挂载在 Level 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ProgressCatalog>> PROGRESS_CATALOG =
            ATTACHMENT_TYPES.register("progress_catalog", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerLevel serverLevel && isAllowedDimension(serverLevel)) {
                                    return new ProgressCatalog();
                                }
                                return new ProgressCatalog();
                            })
                            .serialize(ProgressCatalog.CODEC)
                            .sync(ProgressCatalog.STREAM_CODEC)
                            .build()
            );

    /**
     * 区域数据，挂载在 Level 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LevelZoneData>> LEVEL_ZONE_DATA =
            ATTACHMENT_TYPES.register("level_zone_data", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerLevel serverLevel && isAllowedDimension(serverLevel)) {
                                    return new LevelZoneData(serverLevel);
                                }
                                return new LevelZoneData();
                            })
                            .serialize(LevelZoneData.CODEC)
                            .sync(LevelZoneData.STREAM_CODEC)
                            .build()
            );

    /**
     * 进度管理器，挂载在 Level 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ProgressManager>> PROGRESS_MANAGER =
            ATTACHMENT_TYPES.register("progress_manager", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerLevel serverLevel && isAllowedDimension(serverLevel)) {
                                    return new ProgressManager(serverLevel);
                                }
                                return new ProgressManager();
                            })
                            .build()
            );

    /**
     * 判断维度是否在白名单中
     */
    public static boolean isAllowedDimension(Level level) {
        return ALLOWED_DIMENSIONS.contains(level.dimension());
    }

    // ==================== 数据访问接口 ====================

    /**
     * 获取 Player 的 BeyondData 数据
     */
    public static BeyondData getBeyondData(Player player) {
        return player.getData(BEYOND_DATA);
    }

    /**
     * 获取 Level 的 ProgressCatalog 数据
     */
    public static ProgressCatalog getProgressCatalog(Level level) {
        if (!isAllowedDimension(level)) {
            return null;
        }
        return level.getData(PROGRESS_CATALOG);
    }

    /**
     * 获取 Level 的 LevelZoneData 数据
     */
    public static LevelZoneData getLevelZoneData(Level level) {
        if (!isAllowedDimension(level)) {
            return null;
        }
        return level.getData(LEVEL_ZONE_DATA);
    }

    /**
     * 获取 Level 的 ProgressManager
     */
    public static ProgressManager getProgressManager(Level level) {
        if (!isAllowedDimension(level)) {
            return null;
        }
        return level.getData(PROGRESS_MANAGER);
    }

    /**
     * 注册到事件总线
     */
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    // ==================== Player Event Handlers ====================

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BeyondData beyondData = serverPlayer.getData(BEYOND_DATA);
            serverPlayer.setData(BEYOND_DATA, beyondData);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BeyondData beyondData = serverPlayer.getData(BEYOND_DATA);

            serverPlayer.setData(BEYOND_DATA, beyondData);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BeyondData beyondData = serverPlayer.getData(BEYOND_DATA);

            serverPlayer.setData(BEYOND_DATA, beyondData);
        }
    }


}
