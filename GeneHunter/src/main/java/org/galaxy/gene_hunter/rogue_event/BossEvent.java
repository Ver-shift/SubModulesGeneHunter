package org.galaxy.gene_hunter.rogue_event;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.gene_hunter.GeneHunter;

public class BossEvent extends GatewayEvent {

    public static final ResourceLocation ID = GeneHunter.asResource("boss");
    private static final ResourceLocation GATEWAY_ID = GeneHunter.asResource("zombie_boss");

    public BossEvent() {
        super(ID);
    }

    @Override
    protected ResourceLocation gatewayId() {
        return GATEWAY_ID;
    }
}
