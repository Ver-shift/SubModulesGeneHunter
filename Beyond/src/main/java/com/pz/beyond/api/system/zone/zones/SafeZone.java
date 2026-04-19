package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.init.BeyondZoneRuleInit;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.ZoneType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * 安全区域
 * <p>
 * 负责安全区的初始化和玩家传送
 */
public class SafeZone extends ZoneType {

    public static final ResourceLocation SAFE_ZONE = Beyond.asResource("safe_zone");

    public SafeZone() {
        super(SAFE_ZONE);
    }

    /**
     * 初始化安全区规则
     * 添加默认的规则监听器
     * 
     * @param listeners 规则监听器列表
     * @param level 服务端维度
     */
    @Override
    public void initialize(List<RuleData> listeners, ServerLevel level) {
        addRule(listeners,BeyondZoneRuleInit.ALL_SAFE_RULE);
    }



}
