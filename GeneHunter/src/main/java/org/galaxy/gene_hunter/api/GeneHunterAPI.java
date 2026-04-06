package org.galaxy.gene_hunter.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;
import org.galaxy.gene_hunter.api.init.GeneHunterCapInit;
import org.galaxy.gene_hunter.api.system.GeneHunterData;
import org.galaxy.gene_hunter.api.system.choice.core.IChoiceManager;

public class GeneHunterAPI {


    public static IChoiceManager getChoiceManager(ServerPlayer player) {
        return player.getCapability(GeneHunterCapInit.CHOICE_MANAGER);
    }

    public static GeneHunterData getGeneHunterData(Player player) {
        return player.getData(GeneHunterAttachInit.GENE_HUNTER_DATA);
    }
}
