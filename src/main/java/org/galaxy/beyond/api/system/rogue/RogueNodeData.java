package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.node.NodeData;

@Data
public class RogueNodeData implements IPersistedSerializable {

    @DescSynced
    @Persisted(subPersisted = true)
    private NodeData nodeData;
    @DescSynced
    @Persisted(subPersisted = true)
    private EncounterData encounterData;
    @DescSynced
    @Persisted
    private int currentEventIndex;
    @Persisted
    private ChunkPos nodeChunk;

}
