package org.galaxy.gene_hunter.api.init;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.system.GeneHunterData;
import org.galaxy.gene_hunter.api.system.gateway.GeneHunterDynamicGatewayData;
import org.galaxy.gene_hunter.api.system.temperature.MobTemperatureData;

@EventBusSubscriber
public class GeneHunterAttachInit {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, GeneHunter.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GeneHunterData>> GENE_HUNTER_DATA = ATTACHMENT_TYPES.register(
            "gene_hunter_data",
            () -> AttachmentType.builder((holder) -> {
                        if (holder instanceof ServerPlayer player) {
                            return new GeneHunterData(player);
                        }
                        return new GeneHunterData();
                    })
                    .serialize(GeneHunterData.CODEC)
                    .sync(GeneHunterData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GeneHunterDynamicGatewayData>> DYNAMIC_GATEWAY_DATA = ATTACHMENT_TYPES.register(
            "dynamic_gateway_data",
            () -> AttachmentType.builder(GeneHunterDynamicGatewayData::new)
                    .serialize(GeneHunterDynamicGatewayData.CODEC.codec())
                    .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> ATTACK_EXPLOSION_COOLDOWN_END_TICK = ATTACHMENT_TYPES.register(
            "attack_explosion_cooldown_end_tick",
            () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG)
                    .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobTemperatureData>> MOB_TEMPERATURE = ATTACHMENT_TYPES.register(
            "mob_temperature",
            () -> AttachmentType.builder(() -> new MobTemperatureData())
                    .serialize(MobTemperatureData.CODEC)
                    .build()
    );

    // ==================== Player Event Handlers ====================

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneHunterData hunterData = serverPlayer.getData(GENE_HUNTER_DATA);
            // 设置玩家引用和ID
            hunterData.setPlayer(serverPlayer);
            hunterData.setPlayerId(serverPlayer.getId());
            // 确保嵌套数据已初始化
            hunterData.getChoiceHolderData().setPlayer(serverPlayer);
            serverPlayer.setData(GENE_HUNTER_DATA, hunterData);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneHunterData hunterData = serverPlayer.getData(GENE_HUNTER_DATA);
            // 重生后重新设置玩家引用
            hunterData.setPlayer(serverPlayer);
            hunterData.setPlayerId(serverPlayer.getId());
            hunterData.getChoiceHolderData().setPlayer(serverPlayer);
            serverPlayer.setData(GENE_HUNTER_DATA, hunterData);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneHunterData hunterData = serverPlayer.getData(GENE_HUNTER_DATA);
            // copyOnDeath() 会自动复制数据，重新设置玩家引用
            hunterData.setPlayer(serverPlayer);
            hunterData.setPlayerId(serverPlayer.getId());
            hunterData.getChoiceHolderData().setPlayer(serverPlayer);
            serverPlayer.setData(GENE_HUNTER_DATA, hunterData);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneHunterData hunterData = serverPlayer.getData(GENE_HUNTER_DATA);
            // 清除玩家引用，释放资源

        }
    }
}
