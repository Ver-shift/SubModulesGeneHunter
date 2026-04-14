package com.pz.beyond.api.system.node.core;

import com.pz.beyond.api.system.node.EncounterType;
import com.pz.beyond.api.system.progress.ProgressType;

public interface INodeEventManger {

    /**
     * 进行一个抽取，根据当前 ProgressType 和 EncounterType
     */
    void rollNodeEvent();

    void addNodeEvent(ProgressType progressType, EncounterType encounterType);
}
