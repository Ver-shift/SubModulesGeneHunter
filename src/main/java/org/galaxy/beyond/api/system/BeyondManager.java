package org.galaxy.beyond.api.system;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.node.NodeManager;
import org.galaxy.beyond.api.system.node.core.INodeManager;
import org.galaxy.beyond.api.system.rogue.RogueManager;
import org.galaxy.beyond.api.system.rogue.core.IRougeManager;
import org.galaxy.beyond.api.system.zone.ZoneManager;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

public class BeyondManager implements IBeyondManager {

    private final ZoneManager zoneManager = new ZoneManager();
    private final NodeManager nodeManager = new NodeManager();
    private final RogueManager rogueManager = new RogueManager();


    @Override
    public void levelTick(ServerLevel level) {

    }


    @Override
    public IZoneManager getZoneManager() {
        return zoneManager;
    }

    @Override
    public INodeManager getNodeManager() {
        return nodeManager;
    }

    @Override
    public IRougeManager getRougeManager() {
        return rogueManager;
    }


}
