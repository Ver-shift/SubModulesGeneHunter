package com.pz.beyond.api.system.rule;

import com.pz.beyond.api.init.BeyondZoneRuleInit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 单份rule 数据，能够创建多个不同的实例，给不同的地方使用
 */
@Data
public class RuleData {

    public static final String RULE_ID = "id";
    public static final String RULE_LEVEL = "level";

    /**
     * 规则等级，增加程度。
     */
    private int ruleLevel;
    private final AbstractRule rule;


    public RuleData(AbstractRule rule) {
        this(rule, 1);
    }

    public RuleData(AbstractRule rule, int level) {
        this.rule = rule;
        this.ruleLevel = level;
    }

    public RuleData(ResourceLocation identifier, int level) {
        this(BeyondZoneRuleInit.getRuleById(identifier), level);
    }

    public AbstractRule getRule(ResourceLocation identifier) {
        return BeyondZoneRuleInit.getRuleById(identifier);
    }

    public ResourceLocation getRuleId() {
        return rule.getIdentifier();
    }

    //TODO : 事件支持
    public void addLevel(int level) {
        setRuleLevel(getRuleLevel() + level);
    }

    public void setLevel(int level) {
        this.ruleLevel = level;
    }

    public static final Codec<RuleData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf(RULE_ID).forGetter(RuleData::getRuleId),
            Codec.INT.fieldOf(RULE_LEVEL).forGetter(RuleData::getRuleLevel)
    ).apply(builder, RuleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RuleData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            RuleData::getRuleId,
            ByteBufCodecs.VAR_INT,
            RuleData::getRuleLevel,
            RuleData::new
    );
}