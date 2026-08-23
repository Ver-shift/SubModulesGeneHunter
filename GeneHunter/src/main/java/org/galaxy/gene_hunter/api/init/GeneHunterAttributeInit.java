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
public final class GeneHunterAttributeInit {

    private static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, GeneHunter.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    public record PlayerAttribute(DeferredHolder<Attribute, Attribute> holder) {
    }

    private static PlayerAttribute playerAttribute(String id, double defaultValue, double minValue, double maxValue) {
        DeferredHolder<Attribute, Attribute> holder = ATTRIBUTES.register(id, () -> new RangedAttribute(
                "attribute." + GeneHunter.MODID + "." + id, defaultValue, minValue, maxValue).setSyncable(true));
        return new PlayerAttribute(holder);
    }

    // 武器伤害属性
    /**
     * 使用单手武器时提供的额外伤害。
     */
    public static final PlayerAttribute ONE_HAND_WEAPON_DAMAGE = playerAttribute("one_hand_weapon_damage", 0.0, 0.0, 10000.0);

    /**
     * 使用双手武器时提供的额外伤害。
     */
    public static final PlayerAttribute TWO_HAND_WEAPON_DAMAGE = playerAttribute("two_hand_weapon_damage", 0.0, 0.0, 10000.0);

    /**
     * 使用长杆武器时提供的额外伤害。
     */
    public static final PlayerAttribute POLEARM_WEAPON_DAMAGE = playerAttribute("polearm_weapon_damage", 0.0, 0.0, 10000.0);

    /** 使用刀类武器时提供的额外伤害。 */
    public static final PlayerAttribute BLADE_WEAPON_DAMAGE = playerAttribute("blade_weapon_damage", 0.0, 0.0, 10000.0);

    /** 使用剑类武器时提供的额外伤害。 */
    public static final PlayerAttribute SWORD_WEAPON_DAMAGE = playerAttribute("sword_weapon_damage", 0.0, 0.0, 10000.0);

    /** 使用戟类武器时提供的额外伤害。 */
    public static final PlayerAttribute HALBERD_WEAPON_DAMAGE = playerAttribute("halberd_weapon_damage", 0.0, 0.0, 10000.0);

    /** 使用斧类武器时提供的额外伤害。 */
    public static final PlayerAttribute AXE_WEAPON_DAMAGE = playerAttribute("axe_weapon_damage", 0.0, 0.0, 10000.0);

    /** 使用锤类武器时提供的额外伤害。 */
    public static final PlayerAttribute HAMMER_WEAPON_DAMAGE = playerAttribute("hammer_weapon_damage", 0.0, 0.0, 10000.0);

    /** 未匹配武器形制伤害加成转换为最终伤害的比例。 */
    public static final PlayerAttribute WEAPON_DAMAGE_CONVERSION_RATE = playerAttribute("weapon_damage_conversion_rate", 0.0, 0.0, 1.0);

    // 攻击爆炸属性
    /** 攻击时在被击中目标位置触发爆炸的概率。 */
    public static final PlayerAttribute ATTACK_EXPLOSION_TRIGGER_CHANCE = playerAttribute("attack_explosion_trigger_chance", 0.0, 0.0, 1.0);

    /** 爆炸效果的触发冷却，单位为 tick。 */
    public static final PlayerAttribute ATTACK_EXPLOSION_COOLDOWN = playerAttribute("attack_explosion_cooldown", 5.0, 0.0, 3600.0);

    /** 攻击爆炸的范围半径，单位为格。 */
    public static final PlayerAttribute ATTACK_EXPLOSION_RADIUS = playerAttribute("attack_explosion_radius", 10.0, 0.0, 64.0);

    /** 攻击爆炸对范围内每个目标造成的固定伤害。 */
    public static final PlayerAttribute ATTACK_EXPLOSION_DAMAGE = playerAttribute("attack_explosion_damage", 100.0, 0.0, 100000.0);

    /** 刀类武器攻击触发爆炸的概率；大于零时覆盖通用爆炸触发概率。 */
    public static final PlayerAttribute BLADE_ATTACK_EXPLOSION_TRIGGER_CHANCE = playerAttribute("blade_attack_explosion_trigger_chance", 0.0, 0.0, 1.0);
    /** 剑类武器攻击触发爆炸的概率；大于零时覆盖通用爆炸触发概率。 */
    public static final PlayerAttribute SWORD_ATTACK_EXPLOSION_TRIGGER_CHANCE = playerAttribute("sword_attack_explosion_trigger_chance", 0.0, 0.0, 1.0);
    /** 斧类武器攻击触发爆炸的概率；大于零时覆盖通用爆炸触发概率。 */
    public static final PlayerAttribute AXE_ATTACK_EXPLOSION_TRIGGER_CHANCE = playerAttribute("axe_attack_explosion_trigger_chance", 0.0, 0.0, 1.0);
    /** 锤类武器攻击触发爆炸的概率；大于零时覆盖通用爆炸触发概率。 */
    public static final PlayerAttribute HAMMER_ATTACK_EXPLOSION_TRIGGER_CHANCE = playerAttribute("hammer_attack_explosion_trigger_chance", 0.0, 0.0, 1.0);

    // 连锁闪电属性
    /** 攻击触发连锁闪电效果的概率。 */
    public static final PlayerAttribute ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE = playerAttribute("attack_chain_lightning_trigger_chance", 0.0, 0.0, 1.0);

    /** 连锁闪电可额外传导到的目标次数。 */
    public static final PlayerAttribute ATTACK_CHAIN_LIGHTNING_CHAIN_COUNT = playerAttribute("attack_chain_lightning_chain_count", 10.0, 0.0, 100.0);

    /** 每次连锁闪电命中造成的固定伤害。 */
    public static final PlayerAttribute ATTACK_CHAIN_LIGHTNING_DAMAGE = playerAttribute("attack_chain_lightning_damage", 3.0, 0.0, 100000.0);

    /** 连锁闪电每次传导之间的等待时间，单位为 tick。 */
    public static final PlayerAttribute ATTACK_CHAIN_LIGHTNING_PROPAGATION_INTERVAL = playerAttribute("attack_chain_lightning_propagation_interval", 1.0, 1.0, 100.0);

    /** 连锁闪电寻找下一个目标的范围，单位为格。 */
    public static final PlayerAttribute ATTACK_CHAIN_LIGHTNING_RANGE = playerAttribute("attack_chain_lightning_range", 8.0, 0.0, 64.0);

    /** 刀类武器攻击触发连锁闪电的概率；大于零时覆盖通用连锁闪电触发概率。 */
    public static final PlayerAttribute BLADE_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE = playerAttribute("blade_attack_chain_lightning_trigger_chance", 0.0, 0.0, 1.0);
    /** 剑类武器攻击触发连锁闪电的概率；大于零时覆盖通用连锁闪电触发概率。 */
    public static final PlayerAttribute SWORD_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE = playerAttribute("sword_attack_chain_lightning_trigger_chance", 0.0, 0.0, 1.0);
    /** 斧类武器攻击触发连锁闪电的概率；大于零时覆盖通用连锁闪电触发概率。 */
    public static final PlayerAttribute AXE_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE = playerAttribute("axe_attack_chain_lightning_trigger_chance", 0.0, 0.0, 1.0);
    /** 锤类武器攻击触发连锁闪电的概率；大于零时覆盖通用连锁闪电触发概率。 */
    public static final PlayerAttribute HAMMER_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE = playerAttribute("hammer_attack_chain_lightning_trigger_chance", 0.0, 0.0, 1.0);

    // 攻击温度属性
    /** 玩家每次成功攻击施加给怪物的温度值。 */
    public static final PlayerAttribute ATTACK_TEMPERATURE = playerAttribute("attack_temperature", -100.0, -300.0, 300.0);

    /** 怪物温度高于零时，每 tick 降低的温度值。 */
    public static final PlayerAttribute ATTACK_TEMPERATURE_COOLING_RATE = playerAttribute("attack_temperature_cooling_rate", 5.0, 0.0, 10000.0);

    /** 怪物温度低于零时，每 tick 回升的温度值。 */
    public static final PlayerAttribute ATTACK_TEMPERATURE_RECOVERY_RATE = playerAttribute("attack_temperature_recovery_rate", 5.0, 0.0, 10000.0);

    /** 怪物处于高温状态时，每 20 tick 造成的伤害。 */
    public static final PlayerAttribute ATTACK_TEMPERATURE_BURN_DAMAGE = playerAttribute("attack_temperature_burn_damage", 2.0, 0.0, 100000.0);

    /** 怪物处于低温状态时施加的移速降低比例。 */
    public static final PlayerAttribute ATTACK_TEMPERATURE_COLD_SLOWDOWN = playerAttribute("attack_temperature_cold_slowdown", 0.30, 0.0, 1.0);

    // 攻击斩波属性
    /** 玩家挥击时发射斩波的触发概率。 */
    public static final PlayerAttribute ATTACK_SLASH_TRIGGER_CHANCE = playerAttribute("attack_slash_trigger_chance", 1.0, 0.0, 1.0);

    /** 斩波命中每个目标时造成的固定伤害。 */
    public static final PlayerAttribute ATTACK_SLASH_DAMAGE = playerAttribute("attack_slash_damage", 10.0, 0.0, 100000.0);

    /** 斩波的最大传播距离，单位为格。 */
    public static final PlayerAttribute ATTACK_SLASH_DISTANCE = playerAttribute("attack_slash_distance", 8.0, 0.0, 64.0);

    /** 刀类武器挥击发射斩波的概率；大于零时覆盖通用斩波触发概率。 */
    public static final PlayerAttribute BLADE_ATTACK_SLASH_TRIGGER_CHANCE = playerAttribute("blade_attack_slash_trigger_chance", 0.0, 0.0, 1.0);
    /** 剑类武器挥击发射斩波的概率；大于零时覆盖通用斩波触发概率。 */
    public static final PlayerAttribute SWORD_ATTACK_SLASH_TRIGGER_CHANCE = playerAttribute("sword_attack_slash_trigger_chance", 0.0, 0.0, 1.0);
    /** 斧类武器挥击发射斩波的概率；大于零时覆盖通用斩波触发概率。 */
    public static final PlayerAttribute AXE_ATTACK_SLASH_TRIGGER_CHANCE = playerAttribute("axe_attack_slash_trigger_chance", 0.0, 0.0, 1.0);
    /** 锤类武器挥击发射斩波的概率；大于零时覆盖通用斩波触发概率。 */
    public static final PlayerAttribute HAMMER_ATTACK_SLASH_TRIGGER_CHANCE = playerAttribute("hammer_attack_slash_trigger_chance", 0.0, 0.0, 1.0);

    /**
     * 将属性附加到玩家身上
     */
    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        // 为玩家添加自定义属性
        event.add(EntityType.PLAYER, ONE_HAND_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, TWO_HAND_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, POLEARM_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, BLADE_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, SWORD_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, HALBERD_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, AXE_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, HAMMER_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, WEAPON_DAMAGE_CONVERSION_RATE.holder());
        event.add(EntityType.PLAYER, ATTACK_EXPLOSION_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, ATTACK_EXPLOSION_COOLDOWN.holder());
        event.add(EntityType.PLAYER, ATTACK_EXPLOSION_RADIUS.holder());
        event.add(EntityType.PLAYER, ATTACK_EXPLOSION_DAMAGE.holder());
        event.add(EntityType.PLAYER, BLADE_ATTACK_EXPLOSION_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, SWORD_ATTACK_EXPLOSION_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, AXE_ATTACK_EXPLOSION_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, HAMMER_ATTACK_EXPLOSION_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, ATTACK_CHAIN_LIGHTNING_CHAIN_COUNT.holder());
        event.add(EntityType.PLAYER, ATTACK_CHAIN_LIGHTNING_DAMAGE.holder());
        event.add(EntityType.PLAYER, ATTACK_CHAIN_LIGHTNING_PROPAGATION_INTERVAL.holder());
        event.add(EntityType.PLAYER, ATTACK_CHAIN_LIGHTNING_RANGE.holder());
        event.add(EntityType.PLAYER, BLADE_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, SWORD_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, AXE_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, HAMMER_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, ATTACK_TEMPERATURE.holder());
        event.add(EntityType.PLAYER, ATTACK_TEMPERATURE_COOLING_RATE.holder());
        event.add(EntityType.PLAYER, ATTACK_TEMPERATURE_RECOVERY_RATE.holder());
        event.add(EntityType.PLAYER, ATTACK_TEMPERATURE_BURN_DAMAGE.holder());
        event.add(EntityType.PLAYER, ATTACK_TEMPERATURE_COLD_SLOWDOWN.holder());
        event.add(EntityType.PLAYER, ATTACK_SLASH_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, ATTACK_SLASH_DAMAGE.holder());
        event.add(EntityType.PLAYER, ATTACK_SLASH_DISTANCE.holder());
        event.add(EntityType.PLAYER, BLADE_ATTACK_SLASH_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, SWORD_ATTACK_SLASH_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, AXE_ATTACK_SLASH_TRIGGER_CHANCE.holder());
        event.add(EntityType.PLAYER, HAMMER_ATTACK_SLASH_TRIGGER_CHANCE.holder());

    }
}
