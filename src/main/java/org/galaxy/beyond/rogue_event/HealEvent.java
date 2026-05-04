package org.galaxy.beyond.rogue_event;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

public class HealEvent extends RogueEventType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":heal");

    public HealEvent() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
    }

    @Override
    public Result next(Context context) {
        return Result.EMPTY;
    }
}
