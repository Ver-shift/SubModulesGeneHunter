package org.galaxy.gene_hunter.event;

import net.bettercombat.api.component.BetterCombatDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterComponentInit;
import org.galaxy.gene_hunter.api.init.GeneHunterItemInit;
import org.galaxy.gene_hunter.api.system.weapon.SimplySwordsWeaponClasses;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent;

public final class GeneHunterCommonEvents {
    private static final ResourceLocation TEST_ONE_HAND_DAMAGE_SWORD_SPIN_PRESET =
            ResourceLocation.fromNamespaceAndPath(GeneHunter.MODID, "test_one_hand_damage_sword_spin");

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
        event.modify(GeneHunterItemInit.TEST_ONE_HAND_DAMAGE_SWORD.get(), builder -> {
            builder.set(
                    GeneHunterComponentInit.WEAPON_CLASS.get(),
                    new WeaponClassComponent(
                            WeaponClassComponent.WeaponGrip.ONE_HAND,
                            WeaponClassComponent.WeaponShape.SWORD
                    )
            );
            builder.set(BetterCombatDataComponents.WEAPON_PRESET_ID, TEST_ONE_HAND_DAMAGE_SWORD_SPIN_PRESET);
        });
    }

}
