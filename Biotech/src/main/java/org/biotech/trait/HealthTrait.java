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
 * 增加玩家血量
 */
@AutoInit(type = AutoInit.InitType.TRAIT)
public class HealthTrait implements ITrait {

    @Override
    public float getValue() {
        return 10.0f;
    }

    @Override
    public ResourceLocation getId() {
        return Biotech.asResource("health_trait");
    }

    @Override
    public List<MutableComponent> getUniqueInfo() {
        return List.of(
            Component.translatable("trait.biotech.health.description", (int) getValue())
        );
    }

    @Override
    public void modifyAttributes(CurioAttributeModifierEvent event) {
        // 添加最大生命值属性修饰器
        event.addModifier(
            Attributes.MAX_HEALTH,
            new AttributeModifier(
                getModifierId(event),
                getValue(),      // 增加的血量值
                AttributeModifier.Operation.ADD_VALUE  // 直接增加值
            )
        );
    }

    @Override
    public ResourceLocation getTexture() {
        return ResourceLocation.withDefaultNamespace("textures/mob_effect/health_boost.png");
    }
}
