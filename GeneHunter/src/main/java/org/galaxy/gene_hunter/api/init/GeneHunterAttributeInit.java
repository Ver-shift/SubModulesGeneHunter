package org.galaxy.gene_hunter.api.init;

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
import org.galaxy.gene_hunter.GeneHunter;

/**
 * GeneHunter 属性注册
 */
@EventBusSubscriber(modid = GeneHunter.MODID)
public class GeneHunterAttributeInit {

    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, GeneHunter.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    /**
     * 单手武器伤害加成属性
     * <p>
     * 基础值：0.0（无加成）
     * <p>
     * 数值格式：9.0 = 对应武器最终伤害 +9 点
     */
    public static final DeferredHolder<Attribute, Attribute> ONE_HAND_WEAPON_DAMAGE = ATTRIBUTES.register("one_hand_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.one_hand_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    /**
     * 双手武器伤害加成属性
     * <p>
     * 数值格式：9.0 = 对应武器最终伤害 +9 点
     */
    public static final DeferredHolder<Attribute, Attribute> TWO_HAND_WEAPON_DAMAGE = ATTRIBUTES.register("two_hand_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.two_hand_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    /**
     * 长杆武器伤害加成属性
     * <p>
     * 数值格式：9.0 = 对应武器最终伤害 +9 点
     */
    public static final DeferredHolder<Attribute, Attribute> POLEARM_WEAPON_DAMAGE = ATTRIBUTES.register("polearm_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.polearm_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BLADE_WEAPON_DAMAGE = ATTRIBUTES.register("blade_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.blade_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SWORD_WEAPON_DAMAGE = ATTRIBUTES.register("sword_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.sword_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> HALBERD_WEAPON_DAMAGE = ATTRIBUTES.register("halberd_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.halberd_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> AXE_WEAPON_DAMAGE = ATTRIBUTES.register("axe_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.axe_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> HAMMER_WEAPON_DAMAGE = ATTRIBUTES.register("hammer_weapon_damage",
            () -> new RangedAttribute("attribute.gene_hunter.hammer_weapon_damage", 0.0, 0.0, 10000.0)
                    .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> WEAPON_DAMAGE_CONVERSION_RATE = ATTRIBUTES.register("weapon_damage_conversion_rate",
            () -> new RangedAttribute("attribute.gene_hunter.weapon_damage_conversion_rate", 0.0, 0.0, 1.0)
                    .setSyncable(true));

    /**
     * 将属性附加到玩家身上
     */
    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        // 为玩家添加自定义属性
        event.add(EntityType.PLAYER, ONE_HAND_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, TWO_HAND_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, POLEARM_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, BLADE_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, SWORD_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, HALBERD_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, AXE_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, HAMMER_WEAPON_DAMAGE);
        event.add(EntityType.PLAYER, WEAPON_DAMAGE_CONVERSION_RATE);
    }
}
