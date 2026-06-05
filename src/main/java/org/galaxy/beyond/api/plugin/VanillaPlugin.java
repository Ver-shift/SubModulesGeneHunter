package org.galaxy.beyond.api.plugin;

import net.minecraft.resources.Identifier;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.system.rogue.cap.NodeCap;
import org.galaxy.beyond.api.system.rogue.cap.NodeZoneEnterCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerInGameCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerProgressFinishCap;
import org.galaxy.beyond.api.system.rogue.cap.ProgressStartCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueInitCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueProgressFinishCap;

@AutoInit
public class VanillaPlugin implements IRoguePlugin {

    @Override
    public Identifier getId() {
        return Beyond.asResource("vanilla");
    }

    @Override
    public void initRogueCaps(RogueCapInit registration) {
        registration.initCap(ProgressStartCap.ID);
        registration.initCap(PlayerInGameCap.ID);
        registration.initCap(NodeCap.ID);
        registration.initCap(NodeZoneEnterCap.ID);
        registration.initCap(RogueInitCap.ID);
        registration.initCap(RogueProgressFinishCap.ID);
        registration.initCap(PlayerProgressFinishCap.ID);
    }
}
