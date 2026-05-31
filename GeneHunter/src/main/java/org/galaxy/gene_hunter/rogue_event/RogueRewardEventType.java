package org.galaxy.gene_hunter.rogue_event;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.api.system.rogue.RogueContext;
import org.galaxy.beyond.api.system.rogue.RogueEventType;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.container.ChoiceContainer;

public class RogueRewardEventType extends RogueEventType {

    public static final ResourceLocation ID = GeneHunter.asResource("rogue_reward");

    public RogueRewardEventType() {
        super(ID);
    }

    @Override
    public void cast(Context context) {
        var rogueContext = new RogueContext();
        for (var player : rogueContext.playersInRogue(context.level())) {
            ChoiceContainer.rogueRewardEvent(player);
            player.sendSystemMessage(Component.translatable("gene_hunter.event.rogue_reward"));
        }
    }

    @Override
    public Result next(Context context) {
        return Result.SUCCESS;
    }
}
