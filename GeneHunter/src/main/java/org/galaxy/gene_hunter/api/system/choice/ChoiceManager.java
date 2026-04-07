package org.galaxy.gene_hunter.api.system.choice;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.api.init.GeneHunterMenuInit;
import org.galaxy.gene_hunter.api.system.GeneHunterData;
import org.galaxy.gene_hunter.api.system.choice.core.IChoiceManager;
import org.galaxy.gene_hunter.loot.WeaponLootType;
import org.galaxylib.api.GalaxyLibAPI;
import org.biotech.loot.XeneTraitLootType;
import org.galaxylib.api.system.loot.core.ILootTableManager;
import org.galaxylib.api.system.loot.core.ILootType;
import org.galaxylib.api.system.loot.data.LootPoolData;

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



    public void doRoll(ILootType<?> lootType){
        ItemStackHandler handler = choiceHolderData.getChoiceHolderHandler();
        choiceHolderData.clear();
        var lootManger = GalaxyLibAPI.getLootTableManager(player);
        int maxSlots = Math.min(getChoiceCount(), handler.getSlots());
        // 只抽取一次，获取多个结果
        // 从同一个结果中分配物品到各个槽位
        for (int i = 0; i < maxSlots; i++) {

            if (lootType instanceof XeneTraitLootType xeneTraitLootType) {
                rollWithTrait(handler, i, xeneTraitLootType, lootManger);
            }

            if (lootType instanceof WeaponLootType weaponLootType) {
                var result = lootManger.roolWithoutReplacement(lootType);

                choiceHolderData.setCurrentLootResult(result);
                LootPoolData.Entry entry = result.result().get(i);
                var loot = weaponLootType.getLoot(entry.getId(), entry.getCount());
                if (loot != null) {
                    handler.setStackInSlot(i, loot);
                }
            }
        }
    }

    /**
     * 词条这里采用特殊方式，来完成多词条的抽取
     * @param holderSlots
     * @param lootType
     * @param manager
     */
    private void rollWithTrait(ItemStackHandler holderSlots,int index, ILootType<?> lootType, ILootTableManager manager){
        var result = manager.roolWithoutReplacement(lootType);
        choiceHolderData.setCurrentLootResult(result);
        ItemStack lootItemStack = lootType.stackLike(result);
        holderSlots.setStackInSlot(index, lootItemStack);
    }

    @Override
    public void startRoll(ILootType<?> lootType) {
        doRoll(lootType);
        openChoiceMenu();
        choiceHolderData.setCanRefresh(true);
    }

    @Override
    public void endRoll(ItemStack claimedStack) {
        // 清空所有槽位，表示选择完成
        choiceHolderData.clear();
        // 播放音效
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
        // 发送消息通知玩家
        if (!claimedStack.isEmpty()) {
            Component itemName = claimedStack.getHoverName();
            player.sendSystemMessage(Component.translatable("message.gene_hunter.item_claimed", itemName));
        }
        // 关闭屏幕
        player.closeContainer();
        choiceHolderData.setCanRefresh(false);
    }

    @Override
    public void refresh() {
        if (!choiceHolderData.isCanRefresh()) {
            return;
        }

        var currentLootResult = choiceHolderData.getCurrentLootResult();
        if (currentLootResult == null || currentLootResult.lootType() == null) {
            return;
        }
        choiceHolderData.clear();
        doRoll(currentLootResult.lootType());
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

    @RPCPacket("gene_hunter:claim_slot")
    public static void claimSlot(RPCSender sender, int slotIndex) {
        if (sender.isRemote()) {
            var player = sender.asPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                var manager = GeneHunterAPI.getChoiceManager(serverPlayer);
                if (manager instanceof ChoiceManager choiceManager) {
                    var handler = choiceManager.choiceHolderData.getChoiceHolderHandler();
                    if (slotIndex >= 0 && slotIndex < handler.getSlots()) {
                        ItemStack stack = handler.getStackInSlot(slotIndex);
                        if (!stack.isEmpty()) {
                            // 获取当前战利品类型，使用其 claimItemStackToPlayer 方法
                            var lootResult = choiceManager.choiceHolderData.getCurrentLootResult();
                            if (lootResult != null && lootResult.lootType() != null) {
                                lootResult.lootType().claimItemStackToPlayer(serverPlayer, stack);
                            } else {
                                // 如果没有战利品类型，直接给玩家
                                if (!serverPlayer.getInventory().add(stack)) {
                                    serverPlayer.spawnAtLocation(stack);
                                }
                            }
                            manager.endRoll(stack);

                        }
                    }
                }
            }
        }
    }

}
