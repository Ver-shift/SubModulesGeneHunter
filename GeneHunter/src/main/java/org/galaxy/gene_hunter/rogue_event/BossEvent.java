package org.galaxy.gene_hunter.rogue_event;

import net.minecraft.resources.ResourceLocation;
import lombok.NonNull;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.beyond.api.system.rogue.SceneType;
import org.galaxy.gene_hunter.GeneHunter;

public class BossEvent extends SpawnEvent {

    public static final ResourceLocation ID = GeneHunter.asResource("boss");

    public BossEvent() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        if (context.type().getSceneType() != SceneType.CLIMAX) {
            return;
        }
        super.cast(context);
    }

    @Override
    @NonNull
    public RogueEventType.Result next(Context context) {
        if (context.type().getSceneType() != SceneType.CLIMAX) {
            return Result.SUCCESS;
        }
        return super.next(context);
    }
}
