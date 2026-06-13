package org.galaxy.gene_hunter.api.init;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.item.TestOneHandDamageSwordItem;
import org.galaxy.gene_hunter.item.TestSingleHandSwordItem;

public class GeneHunterItemInit {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GeneHunter.MODID);

    public static final DeferredItem<TestSingleHandSwordItem> TEST_SINGLE_HAND_SWORD =
            ITEMS.register("test_single_hand_sword", key -> new TestSingleHandSwordItem(new Item.Properties()));

    public static final DeferredItem<TestOneHandDamageSwordItem> TEST_ONE_HAND_DAMAGE_SWORD =
            ITEMS.register("test_one_hand_damage_sword", key -> new TestOneHandDamageSwordItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
