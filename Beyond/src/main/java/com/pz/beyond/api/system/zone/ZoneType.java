package com.pz.beyond.api.system.zone;

import com.mojang.serialization.Codec;
import com.pz.beyond.api.init.BeyondZoneInit;
import com.pz.beyond.api.init.BeyondZoneRuleInit;
import com.pz.beyond.api.system.rule.AbstractRule;
import com.pz.beyond.api.system.rule.RuleData;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;


public abstract class ZoneType {

    /**
     * 标识符
     */
    @Getter
    protected final ResourceLocation identifier;

    public ZoneType(ResourceLocation identifier) {
        this.identifier = identifier;
    }




    /**
     * 初始化区域数据
     * 在 ZoneData 首次创建时调用，用于添加默认的规则监听器
     * 
     * 子类必须实现此方法，通过 listeners 参数添加规则
     * 
     * @param listeners 规则监听器列表，向其中添加规则
     * @param level 服务端维度（可用于获取维度相关信息）
     */
    public abstract void initialize(List<RuleData> listeners, ServerLevel level);

    /**
     * 便捷方法：添加规则到列表
     */
    protected void addRule(List<RuleData> listeners, RuleData rule) {
        if (rule != null && listeners != null) {
            listeners.add(rule);
        }
    }
    protected void addRule(List<RuleData> listeners, AbstractRule rule) {
        addRule(listeners, new RuleData(rule));
    }
    protected void addRule(List<RuleData> listeners, ResourceLocation ruleId) {
        addRule(listeners, new RuleData(BeyondZoneRuleInit.getRuleById(ruleId)));
    }
    protected void addRule(List<RuleData> listeners, Supplier<AbstractRule> rule) {
        addRule(listeners, new RuleData(rule.get()));
    }
    protected void addRule(List<RuleData> listeners,Supplier<AbstractRule> rule,int level){
        addRule(listeners, new RuleData(rule.get(), level));
    }

    /**
     * 便捷方法：批量添加规则
     */
    protected void addRules(List<RuleData> listeners, RuleData... rules) {
        if (listeners != null && rules != null) {
            for (RuleData rule : rules) {
                addRule(listeners, rule);
            }
        }
    }

    public static final Codec<ZoneType> CODEC =
            ResourceLocation.CODEC.xmap(
                    BeyondZoneInit::getZoneById,
                    ZoneType::getIdentifier
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ZoneType> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ZoneType::getIdentifier,
                    BeyondZoneInit::getZoneById
            );

}
