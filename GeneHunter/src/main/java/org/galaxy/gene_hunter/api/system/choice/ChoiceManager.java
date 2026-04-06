package org.galaxy.gene_hunter.api.system.choice;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
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
        ItemStackHandler handler = choiceHolderData.getChoiceHolderHandler();
        choiceHolderData.clear();
        var lootManager = GalaxyLibAPI.getLootTableManager(player);
        choiceHolderData.setCurrentLootType(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get());

        int maxSlots = Math.min(getChoiceCount(), handler.getSlots());
        for (int i = 0; i < maxSlots; i++) {
            var result = lootManager.roolWithoutReplacement(BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE);
            if (result.lootType() instanceof XeneTraitLootType xeneTraitLootType) {
                ItemStack stack = xeneTraitLootType.stackLick(result);
                handler.setStackInSlot(i, stack == null ? ItemStack.EMPTY : stack);
            }
        }
    }

    @Override
    public void doWeaponRoll() {
        ItemStackHandler handler = choiceHolderData.getChoiceHolderHandler();
        choiceHolderData.clear();
        var lootManager = GalaxyLibAPI.getLootTableManager(player);
        choiceHolderData.setCurrentLootType(GeneHunterLootInit.WEAPON_LOOT_TYPE.get());

        int writeIndex = 0;
        int maxSlots = Math.min(getChoiceCount(), handler.getSlots());
        for (int i = 0; i < maxSlots && writeIndex < handler.getSlots(); i++) {
            var result = lootManager.roolWithoutReplacement(GeneHunterLootInit.WEAPON_LOOT_TYPE);
            if (result.lootType() instanceof WeaponLootType weaponLootType) {
                for (var entry : result.result()) {
                    var loot = weaponLootType.getLoot(entry.getId(), entry.getCount());
                    if (loot != null && writeIndex < handler.getSlots()) {
                        handler.setStackInSlot(writeIndex, loot);
                        writeIndex++;
                    }
                }
            }
        }
    }

    @Override
    public void refresh() {

        //就是这么shit，拓展性拉完了
        var currentLootType = choiceHolderData.getCurrentLootType();
        choiceHolderData.clear();
        if (currentLootType == BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get()) {
            doXeneRoll();
        } else if (currentLootType == GeneHunterLootInit.WEAPON_LOOT_TYPE.get()) {
            doWeaponRoll();
        }


    }

    /**
     * RPC 方法：客户端请求刷新选择
     */
    @RPCPacket("gene_hunter:refresh_choice")
    public static void rpcRefresh(RPCSender sender) {
        if (sender.isRemote()) {
            var player = sender.asPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                var manager = GeneHunterAPI.getChoiceManager(serverPlayer);
                if (manager instanceof ChoiceManager choiceManager) {
                    choiceManager.refresh();
                }
            }
        }
    }

}
