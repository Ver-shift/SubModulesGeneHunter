package org.galaxy.beyond.api.system.node;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.node.core.NodeState;

import java.util.ArrayList;
import java.util.List;

/**
 * 会持久化保存的数据，每个节点创建的时候都有
 */
@Data
public class NodeData implements IPersistedSerializable {

    @DescSynced
    @Persisted
    private NodeColor color = NodeColor.EMPTY;
    @DescSynced
    @Persisted
    private NodeState state = NodeState.LOCKED;
    /**
     * 单个节点会占据的所有区块总和
     */
    @DescSynced
    @Persisted(subPersisted = true)
    private List<ChunkPos> nodeChunks = new ArrayList<>();

    public NodeData() {}

    public NodeData(NodeColor color) {
        this.color = color;
    }
}
