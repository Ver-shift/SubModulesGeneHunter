package org.galaxy.beyond.api.system.rogue.definition;

import lombok.Data;
import org.galaxy.beyond.api.system.rogue.EncounterType;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;

import java.util.List;
import java.util.Map;

/**
 * 关卡定义
 */
@Data
public class ProgressDefinition {

    private List<SceneEntry> scenes;

    //每次会从encounter Type 之内抽取。
    private List<EncounterEntry> eventTasks;
    






    @Data
    public class SceneEntry{
        private List<SceneType> sceneTypes;
        private int weight;
    }

    @Data
    public class EncounterEntry{
        private EncounterType encounterType;
        private List<EventTaskEntry> eventTasks;
    }
    /**
     * 一个个任务组组成的列表，
     */
    @Data
    public class EventTaskEntry {
        private EventTask eventTasks;
        private int weight;
    }

}
