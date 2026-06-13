package org.galaxy.gene_hunter.rogue_event;

import org.galaxy.gene_hunter.GeneHunter;
import net.minecraft.resources.ResourceLocation;

public class MonsterEvent extends SpawnEvent {

    public static final ResourceLocation ID = GeneHunter.asResource("monster");

    public MonsterEvent() {
        super(ID);
    }
}
