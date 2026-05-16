package org.galaxy.beyond.api.system.rogue;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.api.system.rogue.core.ProgressEventType;

import java.util.ArrayList;
import java.util.List;

/**
 * 行动路线 —— 决定整局游戏的节点类型、怪物种类和 Boss。
 */
public class ActionRoute {

    private final Identifier id;
    private final String displayName;

    /** 每个进度位置的配置（按索引） */
    private final List<ProgressStep> steps = new ArrayList<>();

    public ActionRoute(Identifier id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public Identifier getId() { return id; }
    public String getDisplayName() { return displayName; }
    public List<ProgressStep> getSteps() { return steps; }
    public int getTotalProgress() { return steps.size(); }

    public ProgressStep getStep(int index) {
        return index >= 0 && index < steps.size() ? steps.get(index) : null;
    }

    public ActionRoute addStep(ProgressEventType eventType) {
        steps.add(new ProgressStep(steps.size(), eventType));
        return this;
    }

    /** 行动路线中的单个进度步骤 */
    public static class ProgressStep {
        private final int index;
        private final ProgressEventType eventType;

        public ProgressStep(int index, ProgressEventType eventType) {
            this.index = index;
            this.eventType = eventType;
        }

        public int getIndex() { return index; }
        public ProgressEventType getEventType() { return eventType; }
    }
}
