package org.galaxy.gene_hunter.trait;

import net.minecraft.resources.ResourceLocation;
import org.biotech.api.util.AutoInit;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.system.weapon.WeaponDamageType;
import org.galaxy.gene_hunter.api.system.weapon.WeaponDamageTypes;

@AutoInit(type = AutoInit.InitType.TRAIT)
public class HalberdWeaponDamageTrait extends WeaponDamageTrait {

    @Override
    public ResourceLocation getId() {
        return GeneHunter.asResource("halberd_weapon_damage_trait");
    }

    @Override
    protected WeaponDamageType type() {
        return WeaponDamageTypes.HALBERD;
    }
}
