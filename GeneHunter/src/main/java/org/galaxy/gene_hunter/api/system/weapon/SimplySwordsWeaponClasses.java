package org.galaxy.gene_hunter.api.system.weapon;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.sweenus.simplyswords.registry.ItemsRegistry;
import org.galaxy.gene_hunter.api.init.GeneHunterComponentInit;
import org.galaxy.gene_hunter.api.system.weapon.WeaponClassComponent.WeaponGrip;
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
        RAPIER(WeaponGrip.ONE_HAND, WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_RAPIER, ItemsRegistry.GOLD_RAPIER, ItemsRegistry.DIAMOND_RAPIER,
                ItemsRegistry.NETHERITE_RAPIER, ItemsRegistry.RUNIC_RAPIER)),
        CHAKRAM(WeaponGrip.ONE_HAND, WeaponShape.HAMMER, List.of(
                ItemsRegistry.IRON_CHAKRAM, ItemsRegistry.GOLD_CHAKRAM, ItemsRegistry.DIAMOND_CHAKRAM,
                ItemsRegistry.NETHERITE_CHAKRAM, ItemsRegistry.RUNIC_CHAKRAM)),
        SAI(WeaponGrip.ONE_HAND, WeaponShape.BLADE, List.of(
                ItemsRegistry.IRON_SAI, ItemsRegistry.GOLD_SAI, ItemsRegistry.DIAMOND_SAI,
                ItemsRegistry.NETHERITE_SAI, ItemsRegistry.RUNIC_SAI)),
        CUTLASS(WeaponGrip.ONE_HAND, WeaponShape.BLADE, List.of(
                ItemsRegistry.IRON_CUTLASS, ItemsRegistry.GOLD_CUTLASS, ItemsRegistry.DIAMOND_CUTLASS,
                ItemsRegistry.NETHERITE_CUTLASS, ItemsRegistry.RUNIC_CUTLASS)),
        KATANA(WeaponGrip.ONE_HAND, WeaponShape.BLADE, List.of(
                ItemsRegistry.IRON_KATANA, ItemsRegistry.GOLD_KATANA, ItemsRegistry.DIAMOND_KATANA,
                ItemsRegistry.NETHERITE_KATANA, ItemsRegistry.RUNIC_KATANA)),

        LONGSWORD(WeaponGrip.TWO_HAND, WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_LONGSWORD, ItemsRegistry.GOLD_LONGSWORD, ItemsRegistry.DIAMOND_LONGSWORD,
                ItemsRegistry.NETHERITE_LONGSWORD, ItemsRegistry.RUNIC_LONGSWORD)),
        CLAYMORE(WeaponGrip.TWO_HAND, WeaponShape.SWORD, List.of(
                ItemsRegistry.IRON_CLAYMORE, ItemsRegistry.GOLD_CLAYMORE, ItemsRegistry.DIAMOND_CLAYMORE,
                ItemsRegistry.NETHERITE_CLAYMORE, ItemsRegistry.RUNIC_CLAYMORE)),
        GREATHAMMER(WeaponGrip.TWO_HAND, WeaponShape.HAMMER, List.of(
                ItemsRegistry.IRON_GREATHAMMER, ItemsRegistry.GOLD_GREATHAMMER, ItemsRegistry.DIAMOND_GREATHAMMER,
                ItemsRegistry.NETHERITE_GREATHAMMER, ItemsRegistry.RUNIC_GREATHAMMER)),
        GREATAXE(WeaponGrip.TWO_HAND, WeaponShape.AXE, List.of(
                ItemsRegistry.IRON_GREATAXE, ItemsRegistry.GOLD_GREATAXE, ItemsRegistry.DIAMOND_GREATAXE,
                ItemsRegistry.NETHERITE_GREATAXE, ItemsRegistry.RUNIC_GREATAXE)),
        TWINBLADE(WeaponGrip.TWO_HAND, WeaponShape.HAMMER, List.of(
                ItemsRegistry.IRON_TWINBLADE, ItemsRegistry.GOLD_TWINBLADE, ItemsRegistry.DIAMOND_TWINBLADE,
                ItemsRegistry.NETHERITE_TWINBLADE, ItemsRegistry.RUNIC_TWINBLADE)),

        SPEAR(WeaponGrip.POLEARM, WeaponShape.HALBERD, List.of(
                ItemsRegistry.IRON_SPEAR, ItemsRegistry.GOLD_SPEAR, ItemsRegistry.DIAMOND_SPEAR,
                ItemsRegistry.NETHERITE_SPEAR, ItemsRegistry.RUNIC_SPEAR)),
        SCYTHE(WeaponGrip.POLEARM, WeaponShape.AXE, List.of(
                ItemsRegistry.IRON_SCYTHE, ItemsRegistry.GOLD_SCYTHE, ItemsRegistry.DIAMOND_SCYTHE,
                ItemsRegistry.NETHERITE_SCYTHE, ItemsRegistry.RUNIC_SCYTHE)),
        HALBERD(WeaponGrip.POLEARM, WeaponShape.HALBERD, List.of(
                ItemsRegistry.IRON_HALBERD, ItemsRegistry.GOLD_HALBERD, ItemsRegistry.DIAMOND_HALBERD,
                ItemsRegistry.NETHERITE_HALBERD, ItemsRegistry.RUNIC_HALBERD)),
        GLAIVE(WeaponGrip.POLEARM, WeaponShape.HALBERD, List.of(
                ItemsRegistry.IRON_GLAIVE, ItemsRegistry.GOLD_GLAIVE, ItemsRegistry.DIAMOND_GLAIVE,
                ItemsRegistry.NETHERITE_GLAIVE, ItemsRegistry.RUNIC_GLAIVE)),
        WARGLAIVE(WeaponGrip.POLEARM, WeaponShape.AXE, List.of(
                ItemsRegistry.IRON_WARGLAIVE, ItemsRegistry.GOLD_WARGLAIVE, ItemsRegistry.DIAMOND_WARGLAIVE,
                ItemsRegistry.NETHERITE_WARGLAIVE, ItemsRegistry.RUNIC_WARGLAIVE));

        private final WeaponGrip grip;
        private final WeaponShape shape;
        private final List<Supplier<? extends Item>> items;
        private final WeaponClassComponent weaponClass;

        Family(WeaponGrip grip, WeaponShape shape, List<Supplier<? extends Item>> items) {
            this.grip = grip;
            this.shape = shape;
            this.items = items;
            this.weaponClass = new WeaponClassComponent(grip, shape);
        }

        public WeaponGrip grip() {
            return grip;
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
