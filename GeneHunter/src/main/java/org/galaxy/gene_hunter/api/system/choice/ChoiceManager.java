package org.galaxy.gene_hunter.api.system.choice;

import net.minecraft.server.level.ServerPlayer;
import org.biotech.api.init.BiotechAttributeInit;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.api.system.choice.core.IChoiceManager;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxylib.api.init.GalaxyLibLootTypeInit;
import org.biotech.container.GeneChoiceContainer;
import org.biotech.loot.XeneTraitLootType;

public class ChoiceManager implements IChoiceManager {

    private ServerPlayer player;

    public ChoiceManager(ServerPlayer player) {
        this.player = player;
    }


    @Override
    public boolean openChoiceMenu() {
//        if (player instanceof ServerPlayer player) {
//            return PlayerUIMenuType.openUI(player, GeneChoiceContainer.ID);
//        }
//        return false;
    }

    @Override
    public int getChoiceCount() {
        var value = player.getAttribute(BiotechAttributeInit.GENE_CHOICE_COUNT).getValue();
        return (int)value;
    }

    @Override
    public void setChoiceCount(int choice) {
        	var attribute = player.getAttribute(BiotechAttributeInit.GENE_CHOICE_COUNT);
        	if (attribute != null) {
        		attribute.setBaseValue(choice);
        	}
    }

    @Override
    public void doRoll() {
        //清楚holder的数据

        var lootManager = GalaxyLibAPI.getLootTableManager(player);


        for (int i = 0; i < getChoiceCount(); i++) {
            var result = lootManager.roolWithoutReplacement(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE);
            if (result.lootType() instanceof XeneTraitLootType xeneTraitLootType) {
                //将获取的物品放入choice_holder里面，等待选择



            }
        }

    }


}
