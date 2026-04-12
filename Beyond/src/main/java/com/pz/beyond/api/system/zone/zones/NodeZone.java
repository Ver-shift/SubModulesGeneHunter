package com.pz.beyond.api.system.zone.zones;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.resources.ResourceLocation;

/**
 * 节点区域，
 */
public class NodeZone extends AbstractZone {

    public static final ResourceLocation NODE_ZONE = Beyond.asResource("node_zone");

    public NodeZone() {
        super(NODE_ZONE);
    }
}
