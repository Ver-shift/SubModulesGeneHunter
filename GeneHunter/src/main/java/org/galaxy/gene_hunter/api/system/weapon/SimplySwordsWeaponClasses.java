package org.galaxy.gene_hunter.api.system.weapon;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.sweenus.simplyswords.registry.ItemsRegistry;
import org.galaxy.gene_hunter.api.init.GeneHunterComponentInit;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent.WeaponShape;

import java.util.List;
import java.util.function.Supplier;

public final class SimplySwordsWeaponClasses {

    private SimplySwordsWeaponClasses() {
    }

    public static void apply(ModifyDefaultComponentsEvent event) {
        for (Family family : Family.values()) {
            family.apply(event);
        }
    }

    public enum Family {
        RAPIER(WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_RAPIER, ItemsRegistry.GOLD_RAPIER, ItemsRegistry.DIAMOND_RAPIER,
                ItemsRegistry.NETHERITE_RAPIER, ItemsRegistry.RUNIC_RAPIER)),
        CHAKRAM(WeaponShape.BLADE, List.of(
                ItemsRegistry.IRON_CHAKRAM, ItemsRegistry.GOLD_CHAKRAM, ItemsRegistry.DIAMOND_CHAKRAM,
                ItemsRegistry.NETHERITE_CHAKRAM, ItemsRegistry.RUNIC_CHAKRAM)),
        SAI(WeaponShape.BLADE, List.of(
                ItemsRegistry.IRON_SAI, ItemsRegistry.GOLD_SAI, ItemsRegistry.DIAMOND_SAI,
                ItemsRegistry.NETHERITE_SAI, ItemsRegistry.RUNIC_SAI)),
        CUTLASS(WeaponShape.BLADE, List.of(
                ItemsRegistry.IRON_CUTLASS, ItemsRegistry.GOLD_CUTLASS, ItemsRegistry.DIAMOND_CUTLASS,
                ItemsRegistry.NETHERITE_CUTLASS, ItemsRegistry.RUNIC_CUTLASS)),
        KATANA(WeaponShape.BLADE, List.of(
                ItemsRegistry.IRON_KATANA, ItemsRegistry.GOLD_KATANA, ItemsRegistry.DIAMOND_KATANA,
                ItemsRegistry.NETHERITE_KATANA, ItemsRegistry.RUNIC_KATANA)),

        LONGSWORD(WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_LONGSWORD, ItemsRegistry.GOLD_LONGSWORD, ItemsRegistry.DIAMOND_LONGSWORD,
                ItemsRegistry.NETHERITE_LONGSWORD, ItemsRegistry.RUNIC_LONGSWORD)),
        CLAYMORE(WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_CLAYMORE, ItemsRegistry.GOLD_CLAYMORE, ItemsRegistry.DIAMOND_CLAYMORE,
                ItemsRegistry.NETHERITE_CLAYMORE, ItemsRegistry.RUNIC_CLAYMORE)),
        GREATHAMMER(WeaponShape.HAMMER, List.of(
                ItemsRegistry.IRON_GREATHAMMER, ItemsRegistry.GOLD_GREATHAMMER, ItemsRegistry.DIAMOND_GREATHAMMER,
                ItemsRegistry.NETHERITE_GREATHAMMER, ItemsRegistry.RUNIC_GREATHAMMER)),
        GREATAXE(WeaponShape.AXE, List.of(
                ItemsRegistry.IRON_GREATAXE, ItemsRegistry.GOLD_GREATAXE, ItemsRegistry.DIAMOND_GREATAXE,
                ItemsRegistry.NETHERITE_GREATAXE, ItemsRegistry.RUNIC_GREATAXE)),
        TWINBLADE(WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_TWINBLADE, ItemsRegistry.GOLD_TWINBLADE, ItemsRegistry.DIAMOND_TWINBLADE,
                ItemsRegistry.NETHERITE_TWINBLADE, ItemsRegistry.RUNIC_TWINBLADE)),

        SPEAR(WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_SPEAR, ItemsRegistry.GOLD_SPEAR, ItemsRegistry.DIAMOND_SPEAR,
                ItemsRegistry.NETHERITE_SPEAR, ItemsRegistry.RUNIC_SPEAR)),
        SCYTHE(WeaponShape.AXE, List.of(
                ItemsRegistry.IRON_SCYTHE, ItemsRegistry.GOLD_SCYTHE, ItemsRegistry.DIAMOND_SCYTHE,
                ItemsRegistry.NETHERITE_SCYTHE, ItemsRegistry.RUNIC_SCYTHE)),
        HALBERD(WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_HALBERD, ItemsRegistry.GOLD_HALBERD, ItemsRegistry.DIAMOND_HALBERD,
                ItemsRegistry.NETHERITE_HALBERD, ItemsRegistry.RUNIC_HALBERD)),
        GLAIVE(WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_GLAIVE, ItemsRegistry.GOLD_GLAIVE, ItemsRegistry.DIAMOND_GLAIVE,
                ItemsRegistry.NETHERITE_GLAIVE, ItemsRegistry.RUNIC_GLAIVE)),
        WARGLAIVE(WeaponShape.AXE, List.of(
                ItemsRegistry.IRON_WARGLAIVE, ItemsRegistry.GOLD_WARGLAIVE, ItemsRegistry.DIAMOND_WARGLAIVE,
                ItemsRegistry.NETHERITE_WARGLAIVE, ItemsRegistry.RUNIC_WARGLAIVE)),

        // Simply Swords unique/high-tier weapons. These follow the Better Combat
        // weapon_attributes category (and inherited parent) supplied by Simply Swords,
        // rather than being inferred from their registry names.
        UNIQUE_BLADE(WeaponShape.BLADE, List.of(
                ItemsRegistry.BRAMBLETHORN, ItemsRegistry.CHOMPOLOTL, ItemsRegistry.LIVYATAN,
                ItemsRegistry.MOLTEN_EDGE, ItemsRegistry.SHADOWSTING, ItemsRegistry.SOULSTEALER,
                ItemsRegistry.TEMPEST, ItemsRegistry.WRAITHFANG)),
        UNIQUE_AXE(WeaponShape.AXE, List.of(
                ItemsRegistry.WATCHING_WARGLAIVE, ItemsRegistry.MAGISCYTHE, ItemsRegistry.DECAYING_RELIC,
                ItemsRegistry.EMBERLASH, ItemsRegistry.SOULPYRE, ItemsRegistry.SOULRENDER)),
        UNIQUE_HAMMER(WeaponShape.HAMMER, List.of(
                ItemsRegistry.FROSTFALL, ItemsRegistry.HEARTHFLAME, ItemsRegistry.HIVEHEART,
                ItemsRegistry.MJOLNIR, ItemsRegistry.SOULKEEPER)),
        UNIQUE_SWORD(WeaponShape.SWORD, List.of(
                ItemsRegistry.ARCANETHYST, ItemsRegistry.AWAKENED_LICHBLADE, ItemsRegistry.BRIMSTONE_CLAYMORE,
                ItemsRegistry.CAELESTIS, ItemsRegistry.DORMANT_RELIC, ItemsRegistry.EMBERBLADE,
                ItemsRegistry.ENIGMA, ItemsRegistry.FLAMEWIND, ItemsRegistry.HARBINGER,
                ItemsRegistry.ICEWHISPER, ItemsRegistry.MAGIBLADE, ItemsRegistry.MAGISPEAR,
                ItemsRegistry.RIBBONCLEAVER, ItemsRegistry.RIGHTEOUS_RELIC, ItemsRegistry.SLUMBERING_LICHBLADE,
                ItemsRegistry.STARS_EDGE, ItemsRegistry.STORMBRINGER, ItemsRegistry.STORMS_EDGE,
                ItemsRegistry.SUNFIRE, ItemsRegistry.SWORD_ON_A_STICK, ItemsRegistry.TAINTED_RELIC,
                ItemsRegistry.THUNDERBRAND, ItemsRegistry.TOXIC_LONGSWORD, ItemsRegistry.TWISTED_BLADE,
                ItemsRegistry.WAKING_LICHBLADE, ItemsRegistry.WATCHER_CLAYMORE,
                ItemsRegistry.WAXWEAVER, ItemsRegistry.WHISPERWIND, ItemsRegistry.WICKPIERCER));

        private final WeaponShape shape;
        private final List<Supplier<? extends Item>> items;
        private final WeaponClassComponent weaponClass;

        Family(WeaponShape shape, List<Supplier<? extends Item>> items) {
            this.shape = shape;
            this.items = items;
            this.weaponClass = new WeaponClassComponent(shape);
        }

        public WeaponShape shape() {
            return shape;
        }

        public List<Supplier<? extends Item>> items() {
            return items;
        }

        private void apply(ModifyDefaultComponentsEvent event) {
            for (Supplier<? extends Item> item : items) {
                event.modify(item.get(), builder -> builder.set(GeneHunterComponentInit.WEAPON_CLASS.get(), weaponClass));
            }
        }
    }
}
