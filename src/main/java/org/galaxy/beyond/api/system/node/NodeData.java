package org.galaxy.beyond.api.system.node;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Data;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeData implements IPersistedSerializable {

    @Persisted
    private NodeColor color = NodeColor.EMPTY;

    @Persisted(subPersisted = true)
    private NodePhase phase = BeyondPhaseInit.NODE_LOCKED.get();

    @Persisted(subPersisted = true)
    private List<ChunkPos> nodeChunks = new ArrayList<>();

    public NodeData() {}

    public NodeData(NodeColor color) {
        this.color = color;
    }
}
