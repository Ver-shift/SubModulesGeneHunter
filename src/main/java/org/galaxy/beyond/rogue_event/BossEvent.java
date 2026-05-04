package org.galaxy.beyond.rogue_event;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

public class BossEvent extends RogueEventType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":boss");

    public BossEvent() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        //召唤凋零吧
    }

    @Override
    public Result next(Context context) {
        return Result.EMPTY;
    }
}
