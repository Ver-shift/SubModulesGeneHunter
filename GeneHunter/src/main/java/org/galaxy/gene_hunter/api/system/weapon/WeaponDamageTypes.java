package org.galaxy.gene_hunter.api.system.weapon;

import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;

import java.util.List;

public final class WeaponDamageTypes {
    public static final WeaponDamageType BLADE = new WeaponDamageType(
            "blade_weapon_damage",
            GeneHunterTags.BLADE_WEAPON,
            GeneHunterAttributeInit.BLADE_WEAPON_DAMAGE,
            GeneHunterAttributeInit.BLADE_WEAPON_DAMAGE_RATE
    );
    public static final WeaponDamageType SWORD = new WeaponDamageType(
            "sword_weapon_damage",
            GeneHunterTags.SWORD_WEAPON,
            GeneHunterAttributeInit.SWORD_WEAPON_DAMAGE,
            GeneHunterAttributeInit.SWORD_WEAPON_DAMAGE_RATE
    );
    public static final WeaponDamageType AXE = new WeaponDamageType(
            "axe_weapon_damage",
            GeneHunterTags.AXE_WEAPON,
            GeneHunterAttributeInit.AXE_WEAPON_DAMAGE,
            GeneHunterAttributeInit.AXE_WEAPON_DAMAGE_RATE
    );
    public static final WeaponDamageType HAMMER = new WeaponDamageType(
            "hammer_weapon_damage",
            GeneHunterTags.HAMMER_WEAPON,
            GeneHunterAttributeInit.HAMMER_WEAPON_DAMAGE,
            GeneHunterAttributeInit.HAMMER_WEAPON_DAMAGE_RATE
    );

    private static final List<WeaponDamageType> VALUES = List.of(
            BLADE,
            SWORD,
            AXE,
            HAMMER
    );

    private WeaponDamageTypes() {
    }

    public static List<WeaponDamageType> values() {
        return VALUES;
    }
}
