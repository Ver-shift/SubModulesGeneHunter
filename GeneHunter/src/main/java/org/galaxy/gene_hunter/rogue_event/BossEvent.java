package org.galaxy.gene_hunter.rogue_event;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.data.spawn.ZombieBossSpawnDefinition;

public class BossEvent extends GatewayEvent {

    public static final ResourceLocation ID = GeneHunter.asResource("boss");

    public BossEvent() {
        super(ID);
    }

    @Override
    protected ResourceLocation spawnDefinitionId(Context context) {
        return ZombieBossSpawnDefinition.ID;
    }
}
