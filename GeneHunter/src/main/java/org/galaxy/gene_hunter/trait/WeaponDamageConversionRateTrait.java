package org.galaxy.gene_hunter.trait;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.util.AutoInit;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;

import java.util.List;

@AutoInit(type = AutoInit.InitType.TRAIT)
public class WeaponDamageConversionRateTrait implements ITrait {
    private static final float VALUE = 0.15F;

    @Override
    public ResourceLocation getId() {
        return GeneHunter.asResource("weapon_damage_conversion_rate_trait");
    }

    @Override
    public Holder<Attribute> getAttribute() {
        return GeneHunterAttributeInit.WEAPON_DAMAGE_CONVERSION_RATE;
    }

    @Override
    public List<MutableComponent> getUniqueInfo() {
        return List.of(Component.translatable(
                "trait.gene_hunter.weapon_damage_conversion_rate.description",
                (int) (VALUE * 100)
        ));
    }

    @Override
    public double getAttributeValue(int traitCount) {
        return VALUE * traitCount;
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/strength.png");
    }
}
