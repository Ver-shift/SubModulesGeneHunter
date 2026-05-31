package org.galaxy.gene_hunter.rogue_event;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.gene_hunter.GeneHunter;

public class MonsterEvent extends GatewayEvent {

    public static final ResourceLocation ID = GeneHunter.asResource("monster");
    private static final ResourceLocation GATEWAY_ID = GeneHunter.asResource("zombie_example");

    public MonsterEvent() {
        super(ID);
    }

    @Override
    protected ResourceLocation gatewayId() {
        return GATEWAY_ID;
    }
}
