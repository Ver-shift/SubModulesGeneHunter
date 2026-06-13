package org.galaxy.gene_hunter.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.galaxy.gene_hunter.api.init.GeneHunterAttachInit;
import org.galaxy.gene_hunter.api.system.GeneHunterData;
import org.galaxy.gene_hunter.api.system.choice.ChoiceManager;

public class GeneHunterAPI {


    public static ChoiceManager choiceManager() {
        return ChoiceManager.get();
    }

    public static ChoiceManager getChoiceManager(ServerPlayer player) {
        return choiceManager();
    }

    public static GeneHunterData getGeneHunterData(Player player) {
        return player.getData(GeneHunterAttachInit.GENE_HUNTER_DATA);
    }
}
