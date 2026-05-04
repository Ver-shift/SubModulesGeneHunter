package org.galaxy.beyond.rogue_event;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

public class ShopEvent extends RogueEventType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":shop");

    public ShopEvent() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        //唤起一个村名交易商店
    }

    @Override
    public Result next(Context context) {
        return Result.EMPTY;
    }
}
