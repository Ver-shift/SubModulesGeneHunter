package org.galaxy.gene_hunter.api.init;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import net.minecraft.resources.ResourceLocation;
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.container.ChoiceContainer;

public class GeneHunterMenuInit {

    public static final ResourceLocation CHOICE_MENU = GeneHunter.asResource("choice_menu");

    public static void register() {
        PlayerUIMenuType.register(CHOICE_MENU, (player)-> ChoiceContainer::init);
    }



}
