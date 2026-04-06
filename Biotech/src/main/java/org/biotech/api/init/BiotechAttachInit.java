package org.biotech.api.init;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.biotech.Biotech;
import org.biotech.api.system.GeneData;

@EventBusSubscriber
public class BiotechAttachInit {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GeneData>> GENE_DATA = ATTACHMENT_TYPES.register(
            "gene_data",
            () -> AttachmentType.builder((holder) -> {
                if (holder instanceof ServerPlayer player) {
                    return new GeneData(player);
                }
                return new GeneData();
            })
                    .serialize(GeneData.CODEC)
                    .sync(GeneData.STREAM_CODEC)
                    .copyOnDeath()
                    .build()
    );

    // ==================== Player Event Handlers (GeneData) ====================

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneData geneData = serverPlayer.getData(GENE_DATA);
            // 设置玩家引用和ID
            geneData.setPlayer(serverPlayer);
            geneData.setPlayerId(serverPlayer.getId());

            serverPlayer.setData(GENE_DATA, geneData);

        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneData geneData = serverPlayer.getData(GENE_DATA);
            // 重生后重新设置玩家引用
            geneData.setPlayer(serverPlayer);
            geneData.setPlayerId(serverPlayer.getId());

            serverPlayer.setData(GENE_DATA, geneData);

        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneData geneData = serverPlayer.getData(GENE_DATA);
            // copyOnDeath() 会自动复制数据，重新设置玩家引用
            geneData.setPlayer(serverPlayer);
            geneData.setPlayerId(serverPlayer.getId());

            serverPlayer.setData(GENE_DATA, geneData);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneData geneData = serverPlayer.getData(GENE_DATA);
        }
    }

}
