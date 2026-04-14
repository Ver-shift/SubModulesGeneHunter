package com.pz.beyond.api.system.node;

import com.pz.beyond.Beyond;
import com.pz.beyond.api.system.node.core.INodeEventType;
import lombok.Data;
import net.minecraft.resources.ResourceLocation;

/**
 * 事件激活类型，
 */
@Data
public abstract class NodeEventType implements INodeEventType {


    private ResourceLocation identifier;

    public NodeEventType(ResourceLocation identifier) {
        this.identifier = identifier;
    }



}
