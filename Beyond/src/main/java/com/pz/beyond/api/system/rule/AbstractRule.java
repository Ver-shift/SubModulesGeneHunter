package com.pz.beyond.api.system.rule;

import net.minecraft.resources.ResourceLocation;

/**
 * 单例模式，
 */
public abstract class AbstractRule implements IZoneRule{

    private final ResourceLocation identifier;
    /**
     * rule 的价值，用来测试收益
     */
    private int rule_value;

    private RuleType rule_type = RuleType.Natural;

    public AbstractRule(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public enum RuleType{

        /**
         * 对玩家有好处的，非常的少
         */
        Good,

        /**
         * 诅咒效果类型
         */
        Bad,
        /**
         * 自然持续生效的
         */
        Natural
    }



}
