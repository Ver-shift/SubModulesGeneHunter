package org.galaxy.beyond.api.system.node;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeData implements IPersistedSerializable {

    @Persisted
    private long nodeKey;

    @Persisted
    private NodeColor color = NodeColor.EMPTY;

    @Persisted
    private ResourceLocation phaseId = NodePhase.LOCKED.getId();

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
        ensureNodeKey();
    }

    public boolean addChunkPosIfAbsent(ChunkPos pos) {
        long packed = pack(pos);
        if (nodeChunks.contains(packed)) return false;
        nodeChunks.add(packed);
        ensureNodeKey();
        return true;
    }

    public boolean addPackedChunkIfAbsent(long packed) {
        if (nodeChunks.contains(packed)) return false;
        nodeChunks.add(packed);
        ensureNodeKey();
        return true;
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

    public long ensureNodeKey() {
        if (nodeKey == 0L && !nodeChunks.isEmpty()) {
            nodeKey = nodeChunks.getFirst();
        }
        return nodeKey;
    }

    public NodeData copy() {
        NodeData copy = new NodeData();
        copy.nodeKey = ensureNodeKey();
        copy.color = color;
        copy.phaseId = phaseId;
        copy.nodeChunks = new ArrayList<>(nodeChunks);
        return copy;
    }

    public static void writeFull(FriendlyByteBuf buf, NodeData data) {
        data.ensureNodeKey();
        buf.writeLong(data.nodeKey);
        NodeColor.STREAM_CODEC.encode(buf, data.color);
        buf.writeUtf(data.phaseId.toString());
        buf.writeVarInt(data.nodeChunks.size());
        for (long chunk : data.nodeChunks) {
            buf.writeLong(chunk);
        }
    }

    public static NodeData readFull(FriendlyByteBuf buf) {
        NodeData data = new NodeData();
        data.nodeKey = buf.readLong();
        data.color = NodeColor.STREAM_CODEC.decode(buf);
        data.phaseId = ResourceLocation.parse(buf.readUtf());
        int size = buf.readVarInt();
        data.nodeChunks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            data.nodeChunks.add(buf.readLong());
        }
        data.ensureNodeKey();
        return data;
    }
}
