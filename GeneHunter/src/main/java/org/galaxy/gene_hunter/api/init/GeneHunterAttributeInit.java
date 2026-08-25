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
    /** 使用刀类武器时提供的攻击力加成，可同时包含固定值与百分比。 */
    public static final PlayerAttribute BLADE_WEAPON_DAMAGE = playerAttribute("blade_weapon_damage", 0.0, 0.0, 10000.0);
    /** 刀类武器伤害倍率，数值按百分比存储。 */
    public static final PlayerAttribute BLADE_WEAPON_DAMAGE_RATE = playerAttribute("blade_weapon_damage_rate", 0.0, -10000.0, 10000.0);

    /** 使用剑类武器时提供的攻击力加成，可同时包含固定值与百分比。 */
    public static final PlayerAttribute SWORD_WEAPON_DAMAGE = playerAttribute("sword_weapon_damage", 0.0, 0.0, 10000.0);
    /** 剑类武器伤害倍率，数值按百分比存储。 */
    public static final PlayerAttribute SWORD_WEAPON_DAMAGE_RATE = playerAttribute("sword_weapon_damage_rate", 0.0, -10000.0, 10000.0);

    /** 使用斧类武器时提供的攻击力加成，可同时包含固定值与百分比。 */
    public static final PlayerAttribute AXE_WEAPON_DAMAGE = playerAttribute("axe_weapon_damage", 0.0, 0.0, 10000.0);
    /** 斧类武器伤害倍率，数值按百分比存储。 */
    public static final PlayerAttribute AXE_WEAPON_DAMAGE_RATE = playerAttribute("axe_weapon_damage_rate", 0.0, -10000.0, 10000.0);

    /** 使用锤类武器时提供的攻击力加成，可同时包含固定值与百分比。 */
    public static final PlayerAttribute HAMMER_WEAPON_DAMAGE = playerAttribute("hammer_weapon_damage", 0.0, 0.0, 10000.0);
    /** 锤类武器伤害倍率，数值按百分比存储。 */
    public static final PlayerAttribute HAMMER_WEAPON_DAMAGE_RATE = playerAttribute("hammer_weapon_damage_rate", 0.0, -10000.0, 10000.0);

    /** 未匹配武器形制伤害加成转换为最终伤害的比例。 */
    public static final PlayerAttribute WEAPON_DAMAGE_CONVERSION_RATE = playerAttribute("weapon_damage_conversion_rate", 0.0, 0.0, 1.0);

    /** 对同一目标的首次攻击造成额外伤害的比例。 */
    public static final PlayerAttribute FIRST_ATTACK_DAMAGE_RATIO = playerAttribute("first_attack_damage_ratio", 0.0, 0.0, 1.0);

    /** 击杀敌人后获得的武器附魔攻击力。 */
    public static final PlayerAttribute KILL_ENCHANT_DAMAGE = playerAttribute("kill_enchant_damage", 0.0, 0.0, 10000.0);

    /** 生命值达到该比例时激活高生命伤害加成。 */
    public static final PlayerAttribute HIGH_HEALTH_DAMAGE_THRESHOLD = playerAttribute("high_health_damage_threshold", 0.0, 0.0, 1.0);

    /** 高生命伤害加成激活时获得的攻击力倍率。 */
    public static final PlayerAttribute HIGH_HEALTH_DAMAGE_RATIO = playerAttribute("high_health_damage_ratio", 0.0, 0.0, 1.0);

    /** 玩家攻击产生暴击的概率。暴击额外造成 50% 伤害。 */
    public static final PlayerAttribute CRITICAL_CHANCE = playerAttribute("critical_chance", 0.0, -1.0, 1.0);

    /** 暴击时在基础 50% 之外追加的伤害比例。 */
    public static final PlayerAttribute CRITICAL_DAMAGE_RATIO = playerAttribute("critical_damage_ratio", 0.0, -0.5, 100.0);

    /** 获得经验时的倍率。 */
    public static final PlayerAttribute EXPERIENCE_GAIN_MULTIPLIER = playerAttribute("experience_gain_multiplier", 1.0, 0.0, 100.0);

    /** 每 1% 攻速额外转化的锤类伤害比例。 */
    public static final PlayerAttribute HAMMER_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT = playerAttribute("hammer_attack_speed_damage_ratio_per_percent", 0.0, 0.0, 1.0);

    /** 每 1% 暴击率额外转化的锤类固定伤害。 */
    public static final PlayerAttribute HAMMER_CRITICAL_CHANCE_FIXED_DAMAGE_PER_PERCENT = playerAttribute("hammer_critical_chance_fixed_damage_per_percent", 0.0, 0.0, 100.0);

    /** 每点额外攻击范围额外转化的锤类伤害比例。 */
    public static final PlayerAttribute HAMMER_RANGE_DAMAGE_RATIO_PER_POINT = playerAttribute("hammer_range_damage_ratio_per_point", 0.0, 0.0, 1.0);

    public static final PlayerAttribute BLADE_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT = playerAttribute("blade_attack_speed_damage_ratio_per_percent", 0.0, 0.0, 1.0);
    public static final PlayerAttribute AXE_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT = playerAttribute("axe_attack_speed_damage_ratio_per_percent", 0.0, 0.0, 1.0);
    public static final PlayerAttribute BLADE_LOST_CRITICAL_FIXED_DAMAGE_PER_PERCENT = playerAttribute("blade_lost_critical_fixed_damage_per_percent", 0.0, 0.0, 100.0);
    public static final PlayerAttribute AXE_LOST_CRITICAL_FIXED_DAMAGE_PER_PERCENT = playerAttribute("axe_lost_critical_fixed_damage_per_percent", 0.0, 0.0, 100.0);
    public static final PlayerAttribute BLADE_FIXED_DAMAGE_RATIO_PER_POINT = playerAttribute("blade_fixed_damage_ratio_per_point", 0.0, 0.0, 1.0);
    public static final PlayerAttribute AXE_FIXED_DAMAGE_RATIO_PER_POINT = playerAttribute("axe_fixed_damage_ratio_per_point", 0.0, 0.0, 1.0);

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
        event.add(EntityType.PLAYER, BLADE_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, BLADE_WEAPON_DAMAGE_RATE.holder());
        event.add(EntityType.PLAYER, SWORD_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, SWORD_WEAPON_DAMAGE_RATE.holder());
        event.add(EntityType.PLAYER, AXE_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, AXE_WEAPON_DAMAGE_RATE.holder());
        event.add(EntityType.PLAYER, HAMMER_WEAPON_DAMAGE.holder());
        event.add(EntityType.PLAYER, HAMMER_WEAPON_DAMAGE_RATE.holder());
        event.add(EntityType.PLAYER, WEAPON_DAMAGE_CONVERSION_RATE.holder());
        event.add(EntityType.PLAYER, FIRST_ATTACK_DAMAGE_RATIO.holder());
        event.add(EntityType.PLAYER, KILL_ENCHANT_DAMAGE.holder());
        event.add(EntityType.PLAYER, HIGH_HEALTH_DAMAGE_THRESHOLD.holder());
        event.add(EntityType.PLAYER, HIGH_HEALTH_DAMAGE_RATIO.holder());
        event.add(EntityType.PLAYER, CRITICAL_CHANCE.holder());
        event.add(EntityType.PLAYER, CRITICAL_DAMAGE_RATIO.holder());
        event.add(EntityType.PLAYER, EXPERIENCE_GAIN_MULTIPLIER.holder());
        event.add(EntityType.PLAYER, HAMMER_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT.holder());
        event.add(EntityType.PLAYER, HAMMER_CRITICAL_CHANCE_FIXED_DAMAGE_PER_PERCENT.holder());
        event.add(EntityType.PLAYER, HAMMER_RANGE_DAMAGE_RATIO_PER_POINT.holder());
        event.add(EntityType.PLAYER, BLADE_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT.holder());
        event.add(EntityType.PLAYER, AXE_ATTACK_SPEED_DAMAGE_RATIO_PER_PERCENT.holder());
        event.add(EntityType.PLAYER, BLADE_LOST_CRITICAL_FIXED_DAMAGE_PER_PERCENT.holder());
        event.add(EntityType.PLAYER, AXE_LOST_CRITICAL_FIXED_DAMAGE_PER_PERCENT.holder());
        event.add(EntityType.PLAYER, BLADE_FIXED_DAMAGE_RATIO_PER_POINT.holder());
        event.add(EntityType.PLAYER, AXE_FIXED_DAMAGE_RATIO_PER_POINT.holder());
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
