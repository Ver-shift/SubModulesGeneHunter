package org.galaxy.beyond.zone_cap;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.zone.CapType;
import org.galaxy.beyond.api.system.zone.ZoneCapType;

public class AllNodeZoneCap extends ZoneCapType {

    public static final Identifier ID = Identifier.parse(Beyond.MODID + ":all_node_zone");

    public AllNodeZoneCap() {
        super(ID);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public CapType getCapType() {
        return CapType.NORMAL;
    }
}
