package org.galaxy.beyond.api.system.rogue;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import lombok.Data;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

//单个关卡的运行时数据，
@Data
public class ProgressType implements IPersistedSerializable {
    @DescSynced
    @Persisted
    private Identifier id;

    //进度，从definition里面进行抽取
    @DescSynced
    @Persisted(subPersisted = true)
    private List<SceneType> scenes = new ArrayList<>();
    /**
     * 当前关卡节点index，从0开始
     */
    @DescSynced
    @Persisted
    private int scenesIndex;
    /**
     * 当前关卡能够遇见的遭遇以及内部的事件
     */
    @DescSynced
    @Persisted(subPersisted = true)
    private List<EncounterData> encounters;

    public ProgressType() {}

    public ProgressType(Identifier id) {
        this.id = id;
    }
}
