package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.node.NodeData;

@Data
public class RogueNodeData implements IPersistedSerializable {

    @Persisted(subPersisted = true)
    private NodeData nodeData = new NodeData();
    @Persisted(subPersisted = true)
    private EncounterData encounterData = new EncounterData();
    @Persisted
    private int currentEventIndex;
    @Persisted
    private ChunkPos nodeChunk;

    public static final MapCodec<RogueNodeData> CODEC = PersistedParser.createMapCodec(RogueNodeData::new);
    public static final StreamCodec<ByteBuf, RogueNodeData> STREAM_CODEC = PersistedParser.createStreamCodec(RogueNodeData::new);
}
