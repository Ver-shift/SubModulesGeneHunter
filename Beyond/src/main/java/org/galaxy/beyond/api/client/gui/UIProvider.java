package org.galaxy.beyond.api.client.gui;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.vfyjxf.taffy.style.AlignContent;
import net.minecraft.world.entity.player.Player;
import org.galaxy.beyond.api.client.gui.scene.SceneUI;

public class UIProvider {


    public static ModularUI createSceneUI(Player player) {

        var baseRoot = new UIElement();

        baseRoot.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
            layout.paddingAll(3);
            layout.justifyContent(AlignContent.CENTER);
        });


        var sceneUI = new SceneUI(player);
        baseRoot.addChild(sceneUI);

        var ui = UI.of(baseRoot);
        return ModularUI.of(ui, player);
    }
}
