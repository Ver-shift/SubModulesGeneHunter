package org.galaxy.beyond.zone_cap;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
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

    @Override
    public void playerRightClickBlock(ServerPlayer player, Block block) {
        // NodeBlock owns click handling. The zone right-click hook is on the
        // same interaction path and would otherwise advance the flow twice.
    }
}
