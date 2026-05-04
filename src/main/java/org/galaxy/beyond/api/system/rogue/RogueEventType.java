package org.galaxy.beyond.api.system.rogue;

import lombok.NonNull;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

/**
 * 事件类型
 */
public abstract class RogueEventType {
    private final Identifier id;

    public RogueEventType(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }

    /**
     * 触发节点事件
     * @param context
     */
    public abstract void cast(Context context);

    /**
     * 尝试启动下一个节点事件，默认返回 EMPTY。
     * @param context
     */
    @NonNull
    public abstract Result next(Context context);



    public record Context(EncounterType type, ServerLevel level) {

    }

    public enum Result{
        SUCCESS,
        FAILURE,
        EMPTY;

        public boolean isSuccess() {
            return this == SUCCESS;
        }


    }
}
