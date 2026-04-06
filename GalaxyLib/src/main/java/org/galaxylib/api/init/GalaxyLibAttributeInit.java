package org.galaxylib.api.init;

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
import org.galaxylib.GalaxyLib;

@EventBusSubscriber
public class GalaxyLibAttributeInit {

    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, GalaxyLib.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    /**
     * 将属性附加到玩家身上
     */
    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        // 为玩家添加自定义属性
        event.add(EntityType.PLAYER, ITEM_ROLL_COUNT);
    }

    /**
     * 物品抽取次数属性
     * <p>
     * 基础值：1.0
     * <p>
     * 计算方式：
     * - 1.0 = 抽取1次
     * - 2.0 = 抽取2次
     * - 1.35 = 1次 + 35%概率额外1次
     * - 2.35 = 2次 + 35%概率额外1次
     */
    public static final DeferredHolder<Attribute, Attribute> ITEM_ROLL_COUNT = ATTRIBUTES.register("item_roll_count",
            () -> new RangedAttribute("attribute.galaxy_lib.item_roll_count", 1.0, 0.0, 100.0)
                    .setSyncable(true));

}
