package org.galaxy.gene_hunter.api.system.choice;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.server.level.ServerPlayer;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxy.gene_hunter.api.init.GeneHunterMenuInit;
import org.galaxy.gene_hunter.api.system.GeneHunterData;
import org.galaxy.gene_hunter.api.system.choice.core.IChoiceManager;
import org.galaxy.gene_hunter.loot.WeaponLootType;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxy.gene_hunter.container.ChoiceContainer;
import org.biotech.loot.XeneTraitLootType;

public class ChoiceManager implements IChoiceManager {

    private GeneHunterData data;
    private ChoiceHolderData choiceHolderData;
    private ServerPlayer player;

    public ChoiceManager(GeneHunterData data) {
        this.data = data;
        this.choiceHolderData = data.getChoiceHolderData();
        this.player = data.getPlayer();
    }


    @Override
    public boolean openChoiceMenu() {
        if (player instanceof ServerPlayer player) {
            return PlayerUIMenuType.openUI(player, GeneHunterMenuInit.CHOICE_MENU);
        }
        return false;
    }

    /**
     * RPC 方法：服务器端处理客户端请求打开选择菜单
     */
    @RPCPacket("gene_hunter:open_choice_menu")
    public static void openChoiceMenu(RPCSender sender) {
        if (sender.isRemote()) {
            var player = sender.asPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, GeneHunterMenuInit.CHOICE_MENU);
            }
        }
    }

    @Override
    public int getChoiceCount() {
        return choiceHolderData.getChoiceCount();
    }

    @Override
    public void setChoiceCount(int choice) {
        choiceHolderData.setChoiceCount(choice);
    }

    @Override
    public void doXeneRoll() {
        //清楚holder的数据

        var lootManager = GalaxyLibAPI.getLootTableManager(player);
        var list = choiceHolderData.getChoiceHolder();
        choiceHolderData.setCurrentLootType(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get());
        list.clear();

        for (int i = 0; i < getChoiceCount(); i++) {
            var result = lootManager.roolWithoutReplacement(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE);
            if (result.lootType() instanceof XeneTraitLootType xeneTraitLootType) {
                //将获取的物品放入choice_holder里面，等待选择
                list.add(xeneTraitLootType.stackLick(result));
            }
        }
    }

    @Override
    public void doWeaponRoll() {
        var lootManager = GalaxyLibAPI.getLootTableManager(player);
        var list = choiceHolderData.getChoiceHolder();
        choiceHolderData.setCurrentLootType(GeneHunterLootInit.WEAPON_LOOT_TYPE.get());

        list.clear();

        for (int i = 0; i < getChoiceCount(); i++) {
            var result = lootManager.roolWithoutReplacement(GeneHunterLootInit.WEAPON_LOOT_TYPE);
            if (result.lootType() instanceof WeaponLootType weaponLootType) {
                for (var entry : result.result()) {
                    var loot = weaponLootType.getLoot(entry.getId(), entry.getCount());
                    if (loot != null) {
                        list.add(loot);
                    }
                }
            }
        }
    }

    @Override
    public void refresh() {

        //就是这么shit，拓展性拉完了
        var currentLootType = choiceHolderData.getCurrentLootType();
        choiceHolderData.getChoiceHolder().clear();
        if (currentLootType == BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get()) {
            doXeneRoll();
        } else if (currentLootType == GeneHunterLootInit.WEAPON_LOOT_TYPE.get()) {
            doWeaponRoll();
        }


    }



}
