package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.ReadOnlyManaged;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.init.BeyondPhaseInit;
import org.galaxy.beyond.api.system.node.NodeData;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;

import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RogueData implements IPersistedSerializable {

    @Persisted
    private ResourceKey<Level> rogueLevel = Level.OVERWORLD;

    @Persisted(subPersisted = true)
    private RoguePhase phase = BeyondPhaseInit.ROGUE_LOBBY.get();

    @Persisted(subPersisted = true)
    private RogueNodeData rogueNodeData;

    @Persisted(subPersisted = true)
    private ProgressType progressType;

    @Persisted
    private long gameSeed;

    @Persisted
    private int progressIndex;

    @Persisted
    @ReadOnlyManaged(serializeMethod = "encounterMapSerialize", deserializeMethod = "encounterMapDeserialize")
    private final Map<ChunkPos, EncounterType> encounterAssignments = new ConcurrentHashMap<>();

    @Persisted
    private List<ChunkPos> completedNodeChunks = new CopyOnWriteArrayList<>();

    /** 节点区域 → NodeData（发现时创建，带颜色，跨局持久） */
    @Persisted
    @ReadOnlyManaged(serializeMethod = "nodeDataMapSerialize", deserializeMethod = "nodeDataMapDeserialize")
    private final Map<ChunkPos, NodeData> nodeDataMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unused")
    private CompoundTag encounterMapSerialize(Map<ChunkPos, EncounterType> m) {
        CompoundTag tag = new CompoundTag();
        ListTag keys = new ListTag();
        ListTag values = new ListTag();
        for (var e : m.entrySet()) {
            keys.add(StringTag.valueOf(e.getKey().x() + "," + e.getKey().z()));
            values.add(StringTag.valueOf(e.getValue().name()));
        }
        tag.put("keys", keys);
        tag.put("values", values);
        return tag;
    }

    @SuppressWarnings("unused")
    private Map<ChunkPos, EncounterType> encounterMapDeserialize(CompoundTag tag) {
        Map<ChunkPos, EncounterType> m = new ConcurrentHashMap<>();
        ListTag keys = tag.getListOrEmpty("keys");
        ListTag values = tag.getListOrEmpty("values");
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
            String[] parts = keys.get(i).asString().orElse("0,0").split(",");
            m.put(new ChunkPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
                    EncounterType.valueOf(values.get(i).asString().orElse("Green_Event")));
        }
        return m;
    }

    @SuppressWarnings("unused")
    private CompoundTag nodeDataMapSerialize(Map<ChunkPos, NodeData> m) {
        CompoundTag tag = new CompoundTag();
        ListTag keys = new ListTag();
        ListTag values = new ListTag();
        for (var e : m.entrySet()) {
            keys.add(StringTag.valueOf(e.getKey().x() + "," + e.getKey().z()));
            values.add(StringTag.valueOf(e.getValue().getColor().name()));
        }
        tag.put("keys", keys);
        tag.put("values", values);
        return tag;
    }

    @SuppressWarnings("unused")
    private Map<ChunkPos, NodeData> nodeDataMapDeserialize(CompoundTag tag) {
        Map<ChunkPos, NodeData> m = new ConcurrentHashMap<>();
        ListTag keys = tag.getListOrEmpty("keys");
        ListTag values = tag.getListOrEmpty("values");
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
            String[] parts = keys.get(i).asString().orElse("0,0").split(",");
            ChunkPos cp = new ChunkPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            NodeData nd = new NodeData(org.galaxy.beyond.api.system.node.NodeColor.valueOf(
                    values.get(i).asString().orElse("EMPTY")));
            m.put(cp, nd);
        }
        return m;
    }

}
