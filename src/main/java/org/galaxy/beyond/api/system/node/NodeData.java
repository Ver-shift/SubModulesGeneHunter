package org.galaxy.beyond.api.system.node;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeData implements IPersistedSerializable {

    @Persisted
    private NodeColor color = NodeColor.EMPTY;

    @Persisted
    private Identifier phaseId = NodePhase.LOCKED.getId();

    /** 节点覆盖的区块，存为 packed long，LDLib2 可直接序列化 List<Long> */
    @Persisted
    private List<Long> nodeChunks = new ArrayList<>();

    public static final MapCodec<NodeData> CODEC = PersistedParser.createMapCodec(NodeData::new);
    public static final StreamCodec<ByteBuf, NodeData> STREAM_CODEC = PersistedParser.createStreamCodec(NodeData::new);

    public NodeData() {}

    public NodeData(NodeColor color) {
        this.color = color;
    }

    // ---- ChunkPos 辅助方法 ----

    private static long pack(ChunkPos pos) {
        int x = pos.getMinBlockX() >> 4;
        int z = pos.getMinBlockZ() >> 4;
        return ((long)x & 0xFFFFFFFFL) | (((long)z & 0xFFFFFFFFL) << 32);
    }

    private static ChunkPos unpack(long packed) {
        return new ChunkPos((int)(packed & 0xFFFFFFFFL), (int)((packed >> 32) & 0xFFFFFFFFL));
    }

    public List<ChunkPos> getNodeChunkPosList() {
        return nodeChunks.stream().map(NodeData::unpack).toList();
    }

    public void addChunkPos(ChunkPos pos) {
        nodeChunks.add(pack(pos));
    }

    public boolean containsChunk(ChunkPos pos) {
        return nodeChunks.contains(pack(pos));
    }

    public NodePhase getPhase() {
        return NodePhase.byId(phaseId);
    }

    public void setPhase(NodePhase phase) {
        this.phaseId = phase.getId();
    }
}
