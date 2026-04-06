package org.galaxylib.api.init;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.galaxylib.GalaxyLib;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxylib.api.system.loot.LootTableManager;
import org.galaxylib.api.system.loot.core.ILootTableManager;


@EventBusSubscriber
public class GalaxyLibCapInit {

    public static final EntityCapability<ILootTableManager, Void> LOOT_TABLE =
            EntityCapability.createVoid(
                    GalaxyLib.asResource("loot_table_manager"),
                    ILootTableManager.class
            );


    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {

        event.registerEntity(
                LOOT_TABLE,
                EntityType.PLAYER,
                (player, context) -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        return new LootTableManager(GalaxyLibAPI.getPlayerLootTableData(serverPlayer));
                    }
                    return null;
                }
        );


    }
}
