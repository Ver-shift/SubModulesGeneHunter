package org.galaxy.beyond.api.system.node;

import lombok.Data;
import org.w3c.dom.Node;

/**
 * 会持久化保存的数据，每个节点创建的时候都有
 */
@Data
public class NodeData {

    private final NodeColor color;
    private NodeState state = NodeState.LOCKED;

    public NodeData(NodeColor color) {
        this.color = color;
    }
}
