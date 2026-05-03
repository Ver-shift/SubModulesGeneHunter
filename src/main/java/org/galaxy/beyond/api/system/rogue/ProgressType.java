package org.galaxy.beyond.api.system.rogue;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

//单个关卡的运行时数据，
public class ProgressType {
    private Identifier id;

    //进度，从definition里面进行抽取
    private List<SceneType> scenes;

    /**
     * 当前关卡节点index，从0开始
     */
    private int scenesIndex;
    /**
     * 当前关卡能够遇见的遭遇以及内部的事件
     */
    private Map<EncounterType,EncounterData> encounters;


}
