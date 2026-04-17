package com.pz.beyond.api.init;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.BeyondLevelData;
import com.pz.beyond.api.system.BeyondPlayerData;
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
    /**
     * 判断维度是否在白名单中
     */
    public static boolean isAllowedDimension(Level level) {
        return ALLOWED_DIMENSIONS.contains(level.dimension());
    }

    /**
     * 注册到事件总线
     */
    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }


    // ==================== 玩家附件 ====================

    /**
     * 玩家Beyond数据，挂载在 Player 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BeyondPlayerData>> BEYOND_DATA =
            ATTACHMENT_TYPES.register("beyond_data", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerPlayer player) {
                                    return new BeyondPlayerData(player);
                                }
                                return new BeyondPlayerData();
                            })
                            .serialize(BeyondPlayerData.CODEC)
                            .sync(BeyondPlayerData.STREAM_CODEC)
                            .copyOnDeath()
                            .build()
            );

    // ==================== Level 附件 ====================

    /**
     * 关卡数据，挂载在 Level 上
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BeyondLevelData>> LEVEL_DATA =
            ATTACHMENT_TYPES.register("level_data", () ->
                    AttachmentType.builder(holder -> {
                                if (holder instanceof ServerLevel serverLevel && isAllowedDimension(serverLevel)) {
                                    return new BeyondLevelData();
                                }
                                return new BeyondLevelData();
                            })
                            .serialize(BeyondLevelData.CODEC)
                            .sync(BeyondLevelData.STREAM_CODEC)
                            .build()
            );















}
