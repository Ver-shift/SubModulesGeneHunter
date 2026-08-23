package org.galaxy.gene_hunter.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.galaxy.gene_hunter.api.init.GeneHunterAttributeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterTags;

/** 根据当前主手武器选择攻击传播规则的属性来源。 */
public final class AttackPropagationFactory {
    public enum Type {
        EXPLOSION,
        CHAIN_LIGHTNING,
        SLASH
    }

    private AttackPropagationFactory() {
    }

    /**
     * 获取当前武器的传播触发概率。
     * 分类属性大于零时优先使用；未分类或分类属性为零时回退到通用属性。
     */
    public static double triggerChance(ServerPlayer player, Type type) {
        GeneHunterAttributeInit.PlayerAttribute specific = weaponType(player.getMainHandItem()).attribute(type);
        double specificChance = specific == null ? 0.0D : value(player, specific);
        return specificChance > 0.0D ? specificChance : value(player, universalAttribute(type));
    }

    private static WeaponType weaponType(ItemStack stack) {
        for (WeaponType type : WeaponType.values()) {
            if (stack.is(type.tag)) {
                return type;
            }
        }
        return WeaponType.NONE;
    }

    private static GeneHunterAttributeInit.PlayerAttribute universalAttribute(Type type) {
        return switch (type) {
            case EXPLOSION -> GeneHunterAttributeInit.ATTACK_EXPLOSION_TRIGGER_CHANCE;
            case CHAIN_LIGHTNING -> GeneHunterAttributeInit.ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE;
            case SLASH -> GeneHunterAttributeInit.ATTACK_SLASH_TRIGGER_CHANCE;
        };
    }

    private static double value(ServerPlayer player, GeneHunterAttributeInit.PlayerAttribute attribute) {
        var instance = player.getAttribute(attribute.holder());
        return instance == null ? 0.0D : instance.getValue();
    }

    private enum WeaponType {
        SWORD(GeneHunterTags.SWORD_WEAPON,
                GeneHunterAttributeInit.SWORD_ATTACK_EXPLOSION_TRIGGER_CHANCE,
                GeneHunterAttributeInit.SWORD_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE,
                GeneHunterAttributeInit.SWORD_ATTACK_SLASH_TRIGGER_CHANCE),
        BLADE(GeneHunterTags.BLADE_WEAPON,
                GeneHunterAttributeInit.BLADE_ATTACK_EXPLOSION_TRIGGER_CHANCE,
                GeneHunterAttributeInit.BLADE_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE,
                GeneHunterAttributeInit.BLADE_ATTACK_SLASH_TRIGGER_CHANCE),
        AXE(GeneHunterTags.AXE_WEAPON,
                GeneHunterAttributeInit.AXE_ATTACK_EXPLOSION_TRIGGER_CHANCE,
                GeneHunterAttributeInit.AXE_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE,
                GeneHunterAttributeInit.AXE_ATTACK_SLASH_TRIGGER_CHANCE),
        HAMMER(GeneHunterTags.HAMMER_WEAPON,
                GeneHunterAttributeInit.HAMMER_ATTACK_EXPLOSION_TRIGGER_CHANCE,
                GeneHunterAttributeInit.HAMMER_ATTACK_CHAIN_LIGHTNING_TRIGGER_CHANCE,
                GeneHunterAttributeInit.HAMMER_ATTACK_SLASH_TRIGGER_CHANCE),
        NONE(null, null, null, null);

        private final TagKey<Item> tag;
        private final GeneHunterAttributeInit.PlayerAttribute explosion;
        private final GeneHunterAttributeInit.PlayerAttribute chainLightning;
        private final GeneHunterAttributeInit.PlayerAttribute slash;

        WeaponType(TagKey<Item> tag, GeneHunterAttributeInit.PlayerAttribute explosion,
                   GeneHunterAttributeInit.PlayerAttribute chainLightning, GeneHunterAttributeInit.PlayerAttribute slash) {
            this.tag = tag;
            this.explosion = explosion;
            this.chainLightning = chainLightning;
            this.slash = slash;
        }

        private GeneHunterAttributeInit.PlayerAttribute attribute(Type type) {
            return switch (type) {
                case EXPLOSION -> explosion;
                case CHAIN_LIGHTNING -> chainLightning;
                case SLASH -> slash;
            };
        }
    }
}
