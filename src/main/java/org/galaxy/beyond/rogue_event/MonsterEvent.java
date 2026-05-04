package org.galaxy.beyond.rogue_event;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

public class MonsterEvent extends RogueEventType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":monster");

    public MonsterEvent() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        //todo 简单的小怪召唤系统
    }

    @Override
    public Result next(Context context) {
        return Result.EMPTY;
    }
}
