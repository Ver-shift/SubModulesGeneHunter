package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.BeyondAPI;
import com.pz.beyond.api.system.progress.ProgressCatalog;
import com.pz.beyond.api.system.rule.RuleData;
import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 节点区域
 */
public class NodeZone extends AbstractZone<ProgressCatalog> {

    public static final ResourceLocation NODE_ZONE = Beyond.asResource("node_zone");

    public NodeZone() {
        super(NODE_ZONE);
    }

    @Override
    protected ProgressCatalog getAttachData(Level level) {
        return BeyondAPI.getProgressCatalog(level);
    }

    @Override
    public void initialize(List<RuleData> listeners, ServerLevel level) {
        // 节点区域规则初始化
    }
}
