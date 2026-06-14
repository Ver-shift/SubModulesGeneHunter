package org.galaxy.gene_hunter.trait;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.util.AutoInit;
import org.galaxy.gene_hunter.GeneHunter;

import java.util.List;

@AutoInit(type = AutoInit.InitType.TRAIT)
public class AttackDamageTrait implements ITrait {
    private static final float VALUE = 0.10F;

    @Override
    public ResourceLocation getId() {
        return GeneHunter.asResource("attack_damage_trait");
    }

    @Override
    public List<MutableComponent> getUniqueInfo() {
        return List.of(Component.translatable(
                "trait.gene_hunter.attack_damage.description",
                (int) (VALUE * 100)
        ));
    }

    @Override
    public Holder<Attribute> getAttribute() {
        return Attributes.ATTACK_DAMAGE;
    }

    @Override
    public double getAttributeValue(int traitCount) {
        return VALUE * traitCount;
    }

    @Override
    public AttributeModifier.Operation getAttributeOperation() {
        return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/strength.png");
    }
}
