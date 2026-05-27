package org.galaxy.gene_hunter.rogue_event;

import lombok.NonNull;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.rogue.RogueEventType;

public class MonsterEvent extends RogueEventType {
    public MonsterEvent(ResourceLocation id) {
        super(id);
    }

    @Override
    public void cast(Context context) {

    }

    @Override
    public @NonNull Result next(Context context) {
        return null;
    }
}
