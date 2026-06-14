package org.biotech.api.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.biotech.Biotech;

/**
 * 属性初始化类
 * <p>
 * 注册控制战利品抽取次数的属性
 */
@EventBusSubscriber
public class BiotechAttributeInit {

    private static final DeferredRegister<Attribute> ATTRIBUTES = 
        DeferredRegister.create(Registries.ATTRIBUTE, Biotech.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    /**
     * 将属性附加到玩家身上
     */
    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        // 为玩家添加自定义属性
        event.add(EntityType.PLAYER, TRAIT_ROLL_COUNT);
        event.add(EntityType.PLAYER, GENE_TRAIT_ROLL_COUNT);
        event.add(EntityType.PLAYER, XENE_TRAIT_ROLL_COUNT);
    }


    /**
     * 词条抽取次数属性
     * <p>
     * 基础值：1.0
     * <p>
     * 计算方式与 ITEM_ROLL_COUNT 相同
     */
    public static final DeferredHolder<Attribute, Attribute> TRAIT_ROLL_COUNT = ATTRIBUTES.register("trait_roll_count",
        () -> new RangedAttribute("attribute.biotech.trait_roll_count", 1.0, 0.0, 100.0)
            .setSyncable(true));


    public static final DeferredHolder<Attribute, Attribute> GENE_TRAIT_ROLL_COUNT = ATTRIBUTES.register("gene_trait_roll_count",
        () -> new RangedAttribute("attribute.biotech.gene_trait_roll_count", 3.0, 1.0, 100.0)
            .setSyncable(true));

    public static final DeferredHolder<Attribute,Attribute> XENE_TRAIT_ROLL_COUNT = ATTRIBUTES.register("xene_trait_roll_count",
        () -> new RangedAttribute("attribute.biotech.xene_trait_roll_count", 1.1, 1.0, 100.0)
            .setSyncable(true));
}
