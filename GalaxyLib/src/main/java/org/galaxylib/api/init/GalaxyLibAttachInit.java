package org.galaxylib.api.init;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.galaxylib.GalaxyLib;
import org.galaxylib.api.system.loot.PlayerLootTableData;

@EventBusSubscriber
public class GalaxyLibAttachInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GalaxyLib.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerLootTableData>> PLAYER_LOOT_TABLE_DATA = ATTACHMENT_TYPES.register(
            "player_loot_table_data",
            () -> AttachmentType.builder((holder) -> new PlayerLootTableData())
                    .serialize(PlayerLootTableData.CODEC)
                    .sync(PlayerLootTableData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    // ==================== Player Event Handlers ====================

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerLootTableData lootTableData = serverPlayer.getData(PLAYER_LOOT_TABLE_DATA);
            // 确保数据已初始化
            if (lootTableData == null) {
                lootTableData = new PlayerLootTableData();
                serverPlayer.setData(PLAYER_LOOT_TABLE_DATA, lootTableData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerLootTableData lootTableData = serverPlayer.getData(PLAYER_LOOT_TABLE_DATA);
            // 重生后确保数据有效
            if (lootTableData == null) {
                lootTableData = new PlayerLootTableData();
                serverPlayer.setData(PLAYER_LOOT_TABLE_DATA, lootTableData);
            }
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // copyOnDeath() 会自动处理数据复制，这里只需确保新玩家数据有效
            PlayerLootTableData lootTableData = serverPlayer.getData(PLAYER_LOOT_TABLE_DATA);
            if (lootTableData == null) {
                serverPlayer.setData(PLAYER_LOOT_TABLE_DATA, new PlayerLootTableData());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // PlayerLootTableData 不持有玩家引用，无需特殊处理
        // 但保留此事件钩子以便后续扩展
    }
}
