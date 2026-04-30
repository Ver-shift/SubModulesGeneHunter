package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.init.BeyondZoneRuleInit;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.ZoneType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * 节点区域
 */
public class NodeZone extends ZoneType {

    public static final ResourceLocation NODE_ZONE = Beyond.asResource("node_zone");

    public NodeZone() {
        super(NODE_ZONE);
    }



    @Override
    public void initialize(List<RuleData> listeners, ServerLevel level) {
        // 节点区域规则初始化
        addRule(listeners, BeyondZoneRuleInit.ALL_NODE_RULE);
    }
}
