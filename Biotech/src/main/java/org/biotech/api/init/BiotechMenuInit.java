package org.biotech.api.init;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;
import org.biotech.container.GeneMenu;
import org.biotech.container.GeneInventoryContainer;

public class BiotechMenuInit {



    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Biotech.MODID);




    public static void register(IEventBus modEventBus) {
        registerMenu(modEventBus);
    }

    /**
     * NeoForge menu registry entry.
     */
    public static void registerMenu(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }



    // ldlib2
    public static final PlayerUIMenuType.PlayerUIHolder GENE_MENU_CONTAINER;

    // neoforge
    public static final DeferredHolder<MenuType<?>, MenuType<GeneMenu>> GENE_MENU;

    static {

        GENE_MENU_CONTAINER = GeneInventoryContainer::init;

        // neoforge
        GENE_MENU = MENUS.register("gene_menu",
                () -> IMenuTypeExtension.create((windowId, inv, data) -> new GeneMenu(windowId, inv, GENE_MENU_CONTAINER)));
    }

}
