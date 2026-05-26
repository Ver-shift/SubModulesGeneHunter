package org.galaxy.beyond.api.client.gui.scene;

import com.lowdragmc.lowdraglib2.gui.hud.ModularHudLayer;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.AlignContent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.galaxy.beyond.api.client.gui.UIProvider;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.RoguePhase;
import org.galaxy.beyond.api.system.zone.ZoneType;
import org.jetbrains.annotations.Nullable;

public class SceneUILayer implements ModularHudLayer {

    private ModularUI ui;

    @Override
    public @Nullable ModularUI getModularUI() {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        if (player == null || level == null) return null;

        var lzd = BeyondAPI.getLevelZoneData(level);
        var zone = lzd.getZoneType(player.chunkPosition());
        var rogueData = BeyondAPI.getRogueData(level);
        boolean inGame = rogueData.getPhase() != RoguePhase.LOBBY;
        boolean inZone = zone != null && zone != ZoneType.Safe_Zone;
        if (!inGame || !inZone) return null;

        if (ui == null) {
            ui = buildUI(player);
        }
        return ui;
    }



    private static ModularUI buildUI(Player player) {
        var root = new UIElement();
        root.layout(layout -> {

            layout.width(Minecraft.getInstance().getWindow().getScreenWidth());
            layout.height(Minecraft.getInstance().getWindow().getScreenHeight());

            layout.paddingAll(3);
            layout.justifyContent(AlignContent.CENTER);
        });
        root.addChild(new SceneUI(player));
        return ModularUI.of(UI.of(root), player);
    }
}
