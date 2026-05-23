package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.NodePhase;

import java.util.List;

@Data
public class RogueNodeData implements IPersistedSerializable {

    @Persisted(subPersisted = true)
    private NodeData nodeData = new NodeData();
    @Persisted(subPersisted = true)
    private EncounterData encounterData = new EncounterData();
    @Persisted
    private int currentEventIndex;
    @Persisted
    private ChunkPos  nodeChunk;

    public static final MapCodec<RogueNodeData> CODEC = PersistedParser.createMapCodec(RogueNodeData::new);
    public static final StreamCodec<ByteBuf, RogueNodeData> STREAM_CODEC = PersistedParser.createStreamCodec(RogueNodeData::new);

    // ---- 委托 ----

    public NodePhase getNodePhase() { return nodeData.getPhase(); }
    public void setNodePhase(NodePhase phase) { nodeData.setPhase(phase); }
    public List<ChunkPos> getNodeChunks() { return nodeData.getNodeChunkPosList(); }
    public boolean containsChunk(ChunkPos pos) { return nodeData.containsChunk(pos); }
    public boolean hasEncounter() { return encounterData.getEvents() != null && encounterData.getEvents().hasEvents(); }

    // ---- 行为方法 ----

    /** LOCKED → PRE_NODE：清上次数据，准备新一轮遭遇 */
    public void prepareForEncounter() {
        setNodePhase(NodePhase.PRE_NODE);
        encounterData = new EncounterData();
        currentEventIndex = 0;
    }

    /** PRE_NODE → ON_EVENT：绑定遭遇数据，事件链开始 */
    public void startEncounter(EncounterData encData) {
        this.encounterData = encData;
        this.currentEventIndex = 0;
        setNodePhase(NodePhase.ON_EVENT);
    }

    /** 步进到下一个事件，返回 true 表示这是最后一个事件 */
    public boolean advanceToNextEvent() {
        currentEventIndex++;
        boolean isLast = currentEventIndex >= eventCount() - 1;
        setNodePhase(isLast ? NodePhase.ON_EVENT : NodePhase.PRE_EVENT);
        return isLast;
    }

    public int eventCount() {
        return encounterData.getEvents() != null ? encounterData.getEvents().eventCount() : 0;
    }

    /** ON_EVENT / PRE_EVENT → UNLOCKED：节点通关，颜色置蓝 */
    public void markUnlocked() {
        setNodePhase(NodePhase.UNLOCKED);
        nodeData.setColor(NodeColor.BLUE);
    }
}
