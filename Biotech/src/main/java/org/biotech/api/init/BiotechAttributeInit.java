package org.biotech.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;

/**
 * 属性初始化类
 * <p>
 * 注册控制战利品抽取次数的属性
 */
public class BiotechAttributeInit {

    private static final DeferredRegister<Attribute> ATTRIBUTES = 
        DeferredRegister.create(Registries.ATTRIBUTE, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

}
