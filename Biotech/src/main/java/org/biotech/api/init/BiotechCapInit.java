package org.biotech.api.init;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.biotech.Biotech;
import org.biotech.api.BiotechAPI;
import org.biotech.api.system.gene.core.manager.IGeneInventoryManager;
import org.biotech.api.system.gene.inventory.GeneInventoryManager;
import org.galaxylib.api.system.loot.LootTableManager;
import org.galaxylib.api.system.loot.core.ILootTableManager;
import org.biotech.api.system.merge.MergeManager;
import org.biotech.api.system.merge.core.IMergeManager;

@EventBusSubscriber
public class BiotechCapInit {




    public static final EntityCapability<IGeneInventoryManager, Void> GENE_INVENTORY =
            EntityCapability.createVoid(
                    Biotech.asResource("gene_inventory"),
                    IGeneInventoryManager.class
            );

    public static final EntityCapability<IMergeManager, Void> MERGE_MANAGER =
            EntityCapability.createVoid(
                    Biotech.asResource("merge_manager"),
                    IMergeManager.class
            );

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerEntity(
                GENE_INVENTORY,
                EntityType.PLAYER,
                (player, context) -> new GeneInventoryManager(BiotechAPI.getGeneData(player))
        );

        event.registerEntity(
                MERGE_MANAGER,
                EntityType.PLAYER,
                (player, context) -> new MergeManager(BiotechAPI.getGeneData(player))
        );

    }
}
