package org.galaxy.gene_hunter.rogue_cap;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.galaxy.beyond.api.event.custom.LootBagOpenEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxylib.api.system.loot.LootManager;

import java.util.List;

@EventBusSubscriber(modid = GeneHunter.MODID)
public class GeneHunterLootBagHandler {

    private static final List<net.minecraft.resources.ResourceLocation> WEAPON_TABLES = List.of(
            GeneHunter.asResource("one_hand_weapon_base"),
            GeneHunter.asResource("polearm_weapon_base"),
            GeneHunter.asResource("two_hand_weapon_base")
    );

    @SubscribeEvent
    public static void onLootBagOpen(LootBagOpenEvent event) {
        event.clearRewards();
        event.addReward(rollWeapon(event));
        event.addReward(new ItemStack(Items.BREAD, 10));
        event.addReward(new ItemStack(Items.TORCH, 16));
    }

    private static ItemStack rollWeapon(LootBagOpenEvent event) {
        LootManager.Request request = LootManager.Request.builder()
                .lootType(GeneHunterLootInit.WEAPON_LOOT_TYPE.get())
                .tables(WEAPON_TABLES)
                .replacement(false)
                .rolls(1)
                .build();

        return GalaxyLibAPI.getLootManager()
                .roll(request, LootManager.Context.of(event.getPlayer(), request.getRandomId()))
                .stack();
    }
}
