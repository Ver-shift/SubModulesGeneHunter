package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.galaxy.beyond.api.system.rogue.core.RogueState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class RogueData implements IPersistedSerializable {

    @Persisted
    private ResourceKey<Level> rogueLevel = Level.OVERWORLD;
    //游戏状态
    @DescSynced
    @Persisted
    private RogueState rogueState = RogueState.LOBBY;
    //肉鸽节点数据
    @DescSynced
    @Persisted(subPersisted = true)
    private RogueNodeData rogueNodeData;
    //当前副本（一次冒险只有一个）
    @DescSynced
    @Persisted(subPersisted = true)
    private ProgressType progressType;
    //本局种子，开局时从进度种子克隆而来，保证整局体验不变
    @DescSynced
    @Persisted
    private long gameSeed;
    //当前进度索引
    @DescSynced
    @Persisted
    private int progressIndex;
    //区块 → 遭遇类型分配
    @DescSynced
    @Persisted
    private Map<ChunkPos, EncounterType> encounterAssignments = new ConcurrentHashMap<>();

}
