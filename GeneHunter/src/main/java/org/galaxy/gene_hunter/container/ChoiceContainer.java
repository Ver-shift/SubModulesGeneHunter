package org.galaxy.gene_hunter.container;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.biotech.Biotech;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.ui.element.Choice;

import static org.galaxy.gene_hunter.api.GeneHunterAPI.getChoiceManager;

public class ChoiceContainer {


    public static ModularUI init(Player player) {
        return createGeneInventoryUI(player);
    }
    public static ModularUI createGeneInventoryUI(Player player) {

        var root = new UIElement();
        if (player instanceof ServerPlayer serverPlayer) {
            var manager = getChoiceManager(serverPlayer);

            manager.getChoiceCount();
            for (int i = 0; i < manager.getChoiceCount(); i++) {
                var choice = new Choice();
                root.addChild(choice);

            }

        }





        return ModularUI.of(UI.of(root), player);
    }
}