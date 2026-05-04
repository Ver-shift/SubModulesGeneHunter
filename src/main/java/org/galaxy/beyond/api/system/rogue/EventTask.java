package org.galaxy.beyond.api.system.rogue;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务列表，会依次按照顺序执行。
 */
@Data
public class EventTask {

    private List<RogueEventType> events = new ArrayList<>();
    private int eventIndex;

    public EventTask(List<RogueEventType> events) {
        this.events = events;
        this.eventIndex = 0;
    }

}
