package org.biotech.trait;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.biotech.Biotech;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.util.AutoInit;

import java.util.List;

/**
 * 增加玩家移动速度
 */
//@LDLibPlugin
@AutoInit(type = AutoInit.InitType.TRAIT)
public class SpeedTrait implements ITrait {
    private static final float VALUE = 0.10F;

    @Override
    public ResourceLocation getId() {
        return Biotech.asResource("speed_trait");
    }

    @Override
    public List<MutableComponent> getUniqueInfo() {
        return List.of(
                Component.translatable("trait.biotech.speed.description", (int) (getValue() * 100))
        );
    }


    @Override
    public Holder<Attribute> getAttribute() {
        return Attributes.MOVEMENT_SPEED;
    }

    @Override
    public double getAttributeValue(int traitCount) {
        return getValue(traitCount);
    }

    @Override
    public AttributeModifier.Operation getAttributeOperation() {
        return AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
    }

    private double getValue() {
        return VALUE;
    }

    private double getValue(int traitCount) {
        return VALUE * traitCount;
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/speed.png");
    }
}
