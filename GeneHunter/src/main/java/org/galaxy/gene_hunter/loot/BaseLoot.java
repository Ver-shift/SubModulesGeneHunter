package org.galaxy.gene_hunter.loot;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxylib.api.GalaxyLibAPI;

@EventBusSubscriber
public class BaseLoot {


    @SubscribeEvent
    public static void onPlayerLoadIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GalaxyLibAPI.getLootTableManager(player)
                    .merge(BiotechLootTypeInit.GENE_TRAIT_LOOT_TYPE,
                            GeneHunter.asResource("gene_trait_base"));
        }
    }
}
