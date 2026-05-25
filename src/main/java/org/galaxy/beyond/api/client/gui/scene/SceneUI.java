package org.galaxy.beyond.api.client.gui.scene;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.world.entity.player.Player;
import org.galaxy.beyond.api.system.BeyondAPI;

public class SceneUI extends UIElement {

    private final SceneLine sceneLine;

    public SceneUI(Player player) {
        this.layout(layout -> {
            layout.widthPercent(5);
            layout.heightPercent(40);
            layout.flexDirection(FlexDirection.COLUMN);
            layout.paddingAll(2);
        });
        this.style(style -> {
            style.background(Sprites.BORDER);
        });

        this.sceneLine = new SceneLine(BeyondAPI.getProgressType(player.level()));
        this.addChild(sceneLine);
    }
}
