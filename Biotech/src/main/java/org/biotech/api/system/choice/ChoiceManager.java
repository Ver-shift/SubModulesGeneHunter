package org.biotech.api.system.choice;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.biotech.api.BiotechAPI;
import org.biotech.api.init.AttributeInit;
import org.biotech.api.init.LootTypeInit;
import org.biotech.api.system.choice.core.IChoiceManager;
import org.biotech.api.system.loot.data.LootPoolData;
import org.biotech.api.system.trait.core.ITrait;
import org.biotech.container.GeneChoiceContainer;
import org.biotech.loot.XeneTraitLootType;

public class ChoiceManager implements IChoiceManager {

    private Player player;

    public ChoiceManager(Player player) {
        this.player = player;
    }


    @Override
    public boolean openChoiceMenu() {
        if (player instanceof ServerPlayer player) {
            return PlayerUIMenuType.openUI(player, GeneChoiceContainer.ID);
        }
        return false;
    }

    @Override
    public int getChoiceCount() {
        var value = player.getAttribute(AttributeInit.GENE_CHOICE_COUNT).getValue();
        return (int)value;
    }

    @Override
    public void setChoiceCount(int choice) {
        	var attribute = player.getAttribute(AttributeInit.GENE_CHOICE_COUNT);
        	if (attribute != null) {
        		attribute.setBaseValue(choice);
        	}
    }

    @Override
    public void doRoll() {
        //清楚holder的数据

        var lootManager = BiotechAPI.getLootTableManager(player);


        for (int i = 0; i < getChoiceCount(); i++) {
            var result = lootManager.roolWithoutReplacement(LootTypeInit.XENE_TRAIT_LOOT_TYPE);
            if (result.lootType() instanceof XeneTraitLootType xeneTraitLootType) {
                //将获取的物品放入choice_holder里面，等待选择



            }
        }

    }


}
