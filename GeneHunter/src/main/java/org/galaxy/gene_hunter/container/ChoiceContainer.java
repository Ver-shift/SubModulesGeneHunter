package org.galaxy.gene_hunter;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.biotech.Biotech;

public class GeneChoiceContainer {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Biotech.MODID, "gene_choice");

    public static ModularUI init(Player player) {
        return createGeneInventoryUI(player);
    }
    public static ModularUI createGeneInventoryUI(Player player) {

        var root = new UIElement();
        



        return ModularUI.of(UI.of(root), player);
    }
}