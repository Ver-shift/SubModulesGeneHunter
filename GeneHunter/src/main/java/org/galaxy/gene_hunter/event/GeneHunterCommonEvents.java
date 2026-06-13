package org.galaxy.gene_hunter.event;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.galaxy.gene_hunter.api.system.weapon.SimplySwordsWeaponClasses;

public final class GeneHunterCommonEvents {

    private GeneHunterCommonEvents() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(GeneHunterCommonEvents::modifyDefaultComponents);
    }

    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        SimplySwordsWeaponClasses.apply(event);
    }
}
