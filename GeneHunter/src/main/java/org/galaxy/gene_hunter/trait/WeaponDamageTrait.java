package org.galaxy.gene_hunter.trait;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.biotech.api.system.trait.core.ITrait;

import java.util.List;

public abstract class WeaponDamageTrait implements ITrait {

    protected static final float VALUE = 0.15F;

    protected abstract Holder<Attribute> attribute();

    protected abstract String descriptionKey();

    @Override
    public Holder<Attribute> getAttribute() {
        return attribute();
    }

    @Override
    public List<MutableComponent> getUniqueInfo() {
        return List.of(
                Component.translatable(descriptionKey(), (int) (getValue() * 100))
        );
    }

    @Override
    public double getAttributeValue(int traitCount) {
        return getValue(traitCount);
    }

    private double getValue() {
        return VALUE;
    }

    private double getValue(int traitCount) {
        return VALUE * traitCount;
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/strength.png");
    }
}
