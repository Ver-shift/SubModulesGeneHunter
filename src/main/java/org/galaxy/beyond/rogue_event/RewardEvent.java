package org.galaxy.beyond.rogue_event;

import lombok.NonNull;
import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.event.custom.RogueEvent;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

/**
 * 获得奖励事件
 */
public class RewardEvent extends RogueEventType {

    public static final Identifier ID = Beyond.asResource("reward");

    public RewardEvent() {
        super(ID);
    }

    @Override
    public void cast(Context context) {

    }

    @Override
    public @NonNull Result next(Context context) {
        return null;
    }
}
