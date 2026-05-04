package org.galaxy.beyond.api.system.rogue;

import lombok.Data;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//单个关卡的运行时数据，
@Data
public class ProgressType {
    private final Identifier id;

    //进度，从definition里面进行抽取
    private List<SceneType> scenes = new ArrayList<>();
    /**
     * 当前关卡节点index，从0开始
     */
    private int scenesIndex;
    /**
     * 当前关卡能够遇见的遭遇以及内部的事件
     */
    private List<EncounterData> encounters;

    public ProgressType(Identifier id) {
        this.id = id;
    }
}
