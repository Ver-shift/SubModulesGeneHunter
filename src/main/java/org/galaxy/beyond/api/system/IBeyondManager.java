package org.galaxy.beyond.api.system;

import net.minecraft.server.level.ServerLevel;
import org.galaxy.beyond.api.system.node.core.INodeManager;
import org.galaxy.beyond.api.system.rogue.core.IRougeManager;
import org.galaxy.beyond.api.system.zone.core.IZoneManager;

public interface IBeyondManager {

    //system manager
    IZoneManager getZoneManager();
    INodeManager getNodeManager();
    IRougeManager getRougeManager();


    //event handle
    void levelTick(ServerLevel level);
}
