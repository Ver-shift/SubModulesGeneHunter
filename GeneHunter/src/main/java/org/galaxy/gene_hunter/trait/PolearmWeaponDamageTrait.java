package org.galaxy.gene_hunter.trait;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.biotech.api.util.AutoInit;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;

@AutoInit(type = AutoInit.InitType.TRAIT)
public class PolearmWeaponDamageTrait extends WeaponDamageTrait {

    @Override
    public ResourceLocation getId() {
        return GeneHunter.asResource("polearm_weapon_damage_trait");
    }

    @Override
    protected Holder<Attribute> attribute() {
        return GeneHunterAttributeInit.POLEARM_WEAPON_DAMAGE;
    }

    @Override
    protected String descriptionKey() {
        return "trait.gene_hunter.polearm_weapon_damage.description";
    }
}
