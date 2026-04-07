package org.galaxy.gene_hunter.loot;


import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.sweenus.simplyswords.SimplySwords;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxylib.api.GalaxyLibAPI;

@EventBusSubscriber
public class BaseLoot {

//    SimplySwords
    @SubscribeEvent
    public static void onPlayerLoadIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            //gene配置
            GalaxyLibAPI.getLootTableManager(player)
                    .merge(BiotechLootTypeInit.GENE_TRAIT_LOOT_TYPE,
                            GeneHunter.asResource("gene_trait_base"));
            //xene配置
            GalaxyLibAPI.getLootTableManager(player)
                    .merge(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE,
                    GeneHunter.asResource("xene_trait_base"));

            //武器配置
            GalaxyLibAPI.getLootTableManager(player)
                    .merge(GeneHunterLootInit.WEAPON_LOOT_TYPE,
                            GeneHunter.asResource("one_hand_weapon_base"),
                            GeneHunter.asResource("two_hand_weapon_base"),
                            GeneHunter.asResource("polearm_weapon_base")

                    );



        }
    }
}
