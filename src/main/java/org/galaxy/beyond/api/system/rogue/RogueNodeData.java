package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Data;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.node.NodeData;

@Data
public class RogueNodeData implements IPersistedSerializable {

    @Persisted(subPersisted = true)
    private NodeData nodeData;
    @Persisted(subPersisted = true)
    private EncounterData encounterData;
    @Persisted
    private int currentEventIndex;
    @Persisted
    private ChunkPos nodeChunk;

}
