package org.galaxy.gene_hunter.api.init;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.core.manager.IGeneInventoryManager;
import org.biotech.api.system.gene.inventory.GeneInventoryManager;
import org.biotech.api.system.merge.MergeManager;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.api.system.choice.ChoiceHolderData;
import org.galaxy.gene_hunter.api.system.choice.ChoiceManager;
import org.galaxy.gene_hunter.api.system.choice.core.IChoiceManager;


@EventBusSubscriber
public class GeneHunterCapInit {

    public static final EntityCapability<IChoiceManager, Void> CHOICE_MANAGER =
            EntityCapability.createVoid(
                    GeneHunter.asResource("choice_manager"),
                    IChoiceManager.class
            );

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerEntity(
                CHOICE_MANAGER,
                EntityType.PLAYER,
                (player, context) -> new ChoiceManager(GeneHunterAPI.getGeneHunterData(player))
        );



    }
}
