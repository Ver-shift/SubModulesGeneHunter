package org.galaxy.beyond.api.init;

import net.minecraft.resources.ResourceLocation;
import org.galaxy.beyond.Beyond;
import org.galaxy.beyond.api.plugin.AutoInit;
import org.galaxy.beyond.api.plugin.IRoguePlugin;
import org.galaxy.beyond.api.plugin.RogueCapInit;
import org.galaxy.beyond.api.system.rogue.cap.NodeCap;
import org.galaxy.beyond.api.system.rogue.cap.NodeZoneEnterCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerInGameCap;
import org.galaxy.beyond.api.system.rogue.cap.PlayerProgressFinishCap;
import org.galaxy.beyond.api.system.rogue.cap.ProgressStartCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueInitCap;
import org.galaxy.beyond.api.system.rogue.cap.RogueProgressFinishCap;

/**
 * Beyond 内置插件示例。
 * <p>
 * 其他模组可以照这个类写自己的插件：
 * <pre>
 * {@code
 * @AutoInit
 * public class MyBeyondPlugin implements IRoguePlugin {
 *     public ResourceLocation getId() {
 *         return MyMod.asResource("beyond_plugin");
 *     }
 *
 *     public void registerRogueCaps(RogueCapRegistration registration) {
 *         registration.addCap(MyCap.ID, MyCap::new);
 *     }
 * }
 * }
 * </pre>
 */
@AutoInit
public class BeyondPlugin implements IRoguePlugin {

    @Override
    public ResourceLocation getId() {
        return Beyond.asResource("beyond_plugin");
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
