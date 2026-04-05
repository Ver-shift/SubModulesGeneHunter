package org.biotech.trait;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.biotech.Biotech;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.api.util.AutoInit;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.List;

/**
 * 增加玩家攻击速度
 */
@AutoInit(type = AutoInit.InitType.TRAIT)
public class AttackSpeedTrait implements ITrait {
    @Override
    public float getValue() {
        return 0.10F;
    }

    @Override
    public ResourceLocation getId() {
        return Biotech.asResource("attack_speed_trait");
    }

    @Override
    public List<MutableComponent> getUniqueInfo() {
        return List.of(
            Component.translatable("trait.biotech.attack_speed.description", (int) (getValue() * 100))
        );
    }



    @Override
    public void modifyAttributes(CurioAttributeModifierEvent event) {
        // 按最终值增加10%攻击速度
        event.addModifier(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(
                getModifierId(event),
                getValue(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        );
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/haste.png");
    }
}
