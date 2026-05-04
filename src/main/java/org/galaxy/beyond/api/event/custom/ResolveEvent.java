package org.galaxy.beyond.api.event.custom;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.galaxy.beyond.api.system.rogue.EventTask;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.beyond.api.system.rogue.definition.ProgressDefinition;

import java.util.List;

/**
 * 将数据包数据转化为运行时数据。
 * 可以在转化的时候插入新数据。实现更高随机性。
 * @param <T>
 * @param <V>
 */
public abstract class ResolveEvent<T,V> extends LevelEvent {
    @Getter
    private final T from;
    @Getter @Setter
    private V to;

    public ResolveEvent(LevelAccessor level,T from,V to) {
        super(level);
        this.from = from;
        this.to = to;
    }

    public static class ResolveEventTaskEvent extends ResolveEvent<ProgressDefinition.EncounterEntry,EventTask>{
        public ResolveEventTaskEvent(LevelAccessor level, ProgressDefinition.EncounterEntry from, EventTask to) {
            super(level, from, to);
        }

    }

    public static class ResolveSceneEvent extends ResolveEvent<List<ProgressDefinition.SceneEntry>,List<SceneType>>{

        public ResolveSceneEvent(LevelAccessor level, List<ProgressDefinition.SceneEntry> from, List<SceneType> to) {
            super(level, from, to);
        }


    }


}
