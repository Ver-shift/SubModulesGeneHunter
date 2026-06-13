package org.galaxy.gene_hunter.api.system.weapon;

import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;

import java.util.List;

public final class WeaponDamageTypes {
    public static final WeaponDamageType ONE_HAND = new WeaponDamageType(
            "one_hand_weapon_damage",
            GeneHunterTags.ONE_HAND_WEAPON,
            GeneHunterAttributeInit.ONE_HAND_WEAPON_DAMAGE,
            "trait.gene_hunter.one_hand_weapon_damage.description",
            WeaponDamageType.Channel.GRIP
    );
    public static final WeaponDamageType TWO_HAND = new WeaponDamageType(
            "two_hand_weapon_damage",
            GeneHunterTags.TWO_HAND_WEAPON,
            GeneHunterAttributeInit.TWO_HAND_WEAPON_DAMAGE,
            "trait.gene_hunter.two_hand_weapon_damage.description",
            WeaponDamageType.Channel.GRIP
    );
    public static final WeaponDamageType POLEARM = new WeaponDamageType(
            "polearm_weapon_damage",
            GeneHunterTags.POLEARM_WEAPON,
            GeneHunterAttributeInit.POLEARM_WEAPON_DAMAGE,
            "trait.gene_hunter.polearm_weapon_damage.description",
            WeaponDamageType.Channel.GRIP
    );

    public static final WeaponDamageType BLADE = new WeaponDamageType(
            "blade_weapon_damage",
            GeneHunterTags.BLADE_WEAPON,
            GeneHunterAttributeInit.BLADE_WEAPON_DAMAGE,
            "trait.gene_hunter.blade_weapon_damage.description",
            WeaponDamageType.Channel.SHAPE
    );
    public static final WeaponDamageType SWORD = new WeaponDamageType(
            "sword_weapon_damage",
            GeneHunterTags.SWORD_WEAPON,
            GeneHunterAttributeInit.SWORD_WEAPON_DAMAGE,
            "trait.gene_hunter.sword_weapon_damage.description",
            WeaponDamageType.Channel.SHAPE
    );
    public static final WeaponDamageType HALBERD = new WeaponDamageType(
            "halberd_weapon_damage",
            GeneHunterTags.HALBERD_WEAPON,
            GeneHunterAttributeInit.HALBERD_WEAPON_DAMAGE,
            "trait.gene_hunter.halberd_weapon_damage.description",
            WeaponDamageType.Channel.SHAPE
    );
    public static final WeaponDamageType AXE = new WeaponDamageType(
            "axe_weapon_damage",
            GeneHunterTags.AXE_WEAPON,
            GeneHunterAttributeInit.AXE_WEAPON_DAMAGE,
            "trait.gene_hunter.axe_weapon_damage.description",
            WeaponDamageType.Channel.SHAPE
    );
    public static final WeaponDamageType HAMMER = new WeaponDamageType(
            "hammer_weapon_damage",
            GeneHunterTags.HAMMER_WEAPON,
            GeneHunterAttributeInit.HAMMER_WEAPON_DAMAGE,
            "trait.gene_hunter.hammer_weapon_damage.description",
            WeaponDamageType.Channel.SHAPE
    );

    private static final List<WeaponDamageType> VALUES = List.of(
            ONE_HAND,
            TWO_HAND,
            POLEARM,
            BLADE,
            SWORD,
            HALBERD,
            AXE,
            HAMMER
    );

    private WeaponDamageTypes() {
    }

    public static List<WeaponDamageType> values() {
        return VALUES;
    }
}
