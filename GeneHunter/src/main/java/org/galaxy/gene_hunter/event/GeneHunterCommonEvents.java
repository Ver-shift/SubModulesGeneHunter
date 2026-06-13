package org.galaxy.gene_hunter.event;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.galaxy.gene_hunter.api.init.GeneHunterComponentInit;
import org.galaxy.gene_hunter.api.init.GeneHunterItemInit;
import org.galaxy.gene_hunter.api.system.weapon.SimplySwordsWeaponClasses;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent;

public final class GeneHunterCommonEvents {

    private GeneHunterCommonEvents() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(GeneHunterCommonEvents::modifyDefaultComponents);
    }

    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        SimplySwordsWeaponClasses.apply(event);
        event.modify(GeneHunterItemInit.TEST_SINGLE_HAND_SWORD.get(), builder -> builder.set(
                GeneHunterComponentInit.WEAPON_CLASS.get(),
                new WeaponClassComponent(
                        WeaponClassComponent.WeaponGrip.ONE_HAND,
                        WeaponClassComponent.WeaponShape.SWORD
                )
        ));
        event.modify(GeneHunterItemInit.TEST_ONE_HAND_DAMAGE_SWORD.get(), builder -> builder.set(
                GeneHunterComponentInit.WEAPON_CLASS.get(),
                new WeaponClassComponent(
                        WeaponClassComponent.WeaponGrip.ONE_HAND,
                        WeaponClassComponent.WeaponShape.SWORD
                )
        ));
    }

}
