package org.galaxy.beyond.api.system.node;

import lombok.Data;
import net.minecraft.world.level.ChunkPos;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 会持久化保存的数据，每个节点创建的时候都有
 */
@Data
public class NodeData {

    private NodeColor color;
    private NodeState state = NodeState.LOCKED;

    /**
     * 单个节点会占据的所有区块总和
     */
    private List<ChunkPos> nodeChunks = new ArrayList<>();

    public NodeData(NodeColor color) {
        this.color = color;
    }

    

}
