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
 * 增加玩家移动速度
 */
//@LDLibPlugin
@AutoInit(type = AutoInit.InitType.TRAIT)
public class SpeedTrait implements ITrait {
    @Override
    public float getValue() {
        return 0.10F;
    }

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
    public void modifyAttributes(CurioAttributeModifierEvent event) {
        // 按最终值增加10%移动速度
        event.addModifier(
            Attributes.MOVEMENT_SPEED,
            new AttributeModifier(
                getModifierId(event),
                getValue(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        );
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/speed.png");
    }
}
