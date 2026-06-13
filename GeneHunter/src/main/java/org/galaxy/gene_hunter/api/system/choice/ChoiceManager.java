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
import org.galaxy.beyond.component.ValueComp;
import org.galaxy.beyond.api.system.node.NodeColor;
import org.galaxy.gene_hunter.api.GeneHunterAPI;
import org.galaxy.gene_hunter.api.init.GeneHunterMenuInit;
import org.galaxylib.api.GalaxyLibAPI;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.List;

public final class ChoiceManager {

    private static final ChoiceManager INSTANCE = new ChoiceManager();

    private final ChoiceRollFactory rollFactory = new ChoiceRollFactory();
    private final ChoiceExperienceCost experienceCost = new ChoiceExperienceCost();

    private ChoiceManager() {
    }

    public static ChoiceManager get() {
        return INSTANCE;
    }

    private ChoiceHolderData data(ServerPlayer player) {
        return GeneHunterAPI.getGeneHunterData(player).getChoiceHolderData();
    }


    public boolean openChoiceMenu(ServerPlayer player) {
        return PlayerUIMenuType.openUI(player, GeneHunterMenuInit.CHOICE_MENU);
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

    public int getChoiceCount(ServerPlayer player) {
        return data(player).getChoiceCount();
    }

    public void setChoiceCount(ServerPlayer player, int choice) {
        data(player).setChoiceCount(choice);
    }


    public void doRoll(ServerPlayer player, ILootType<?> lootType) {
        doRoll(player, lootType, null);
    }

    public void doRoll(ServerPlayer player, ILootType<?> lootType, NodeColor nodeColor) {
        doRoll(player, lootType, nodeColor, 0);
    }

    private void doRoll(ServerPlayer player, ILootType<?> lootType, NodeColor nodeColor, int fixedRolls) {
        ChoiceHolderData choiceHolderData = data(player);
        ItemStackHandler handler = choiceHolderData.getChoiceHolderHandler();
        choiceHolderData.clear();
        int maxSlots = Math.min(getChoiceCount(player), handler.getSlots());
        NodeColor color = nodeColor == null ? NodeColor.GREEN : nodeColor;
        ChoiceStage stage = new ChoiceStage(lootType, color, fixedRolls);

        LootManager.Request request = rollFactory.create(player, stage);
        var result = GalaxyLibAPI.getLootManager().rollChoices(request, LootManager.Context.of(player, request.getRandomId()), maxSlots);
        choiceHolderData.setCurrentLootType(lootType);
        choiceHolderData.setCurrentNodeColor(color);
        choiceHolderData.setCurrentFixedRolls(fixedRolls);
        choiceHolderData.setRefreshCost(experienceCost.nextCost(request, choiceHolderData.getRefreshTimes()));

        for (int i = 0; i < result.options().size(); i++) {
            handler.setStackInSlot(i, result.options().get(i).stack());
        }
    }

    public void startRoll(ServerPlayer player, ILootType<?> lootType) {
        startRoll(player, lootType, null);
    }

    public void startRoll(ServerPlayer player, ILootType<?> lootType, NodeColor nodeColor) {
        startStages(player, List.of(ChoiceStage.of(lootType, nodeColor)));
    }

    public void startStages(ServerPlayer player, List<ChoiceStage> stages) {
        if (stages == null || stages.isEmpty()) {
            return;
        }
        ChoiceHolderData choiceHolderData = data(player);
        choiceHolderData.startStages(stages);
        startStage(player, choiceHolderData.currentStage());
        openChoiceMenu(player);
    }

    private void startStage(ServerPlayer player, ChoiceStage stage) {
        if (stage == null) {
            endAllStages(player);
            return;
        }
        ChoiceHolderData choiceHolderData = data(player);
        choiceHolderData.setRefreshTimes(0);
        choiceHolderData.setCanRefresh(true);
        doRoll(player, stage.lootType(), stage.nodeColor(), stage.fixedRolls());
    }

    public void endRoll(ServerPlayer player, ItemStack claimedStack) {
        ChoiceHolderData choiceHolderData = data(player);
        // 清空所有槽位，表示选择完成
        choiceHolderData.clear();
        // 播放音效
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
        // 发送消息通知玩家
        if (!claimedStack.isEmpty()) {
            Component itemName = claimedStack.getHoverName();
            player.sendSystemMessage(Component.translatable("message.gene_hunter.item_claimed", itemName));
        }
        if (choiceHolderData.nextStage()) {
            startStage(player, choiceHolderData.currentStage());
            return;
        }
        endAllStages(player);
    }

    private void endAllStages(ServerPlayer player) {
        player.closeContainer();
        ChoiceHolderData choiceHolderData = data(player);
        choiceHolderData.setCanRefresh(false);
        choiceHolderData.clearStages();
    }

    public void refresh(ServerPlayer player) {
        ChoiceHolderData choiceHolderData = data(player);
        if (!choiceHolderData.isCanRefresh()) {
            return;
        }

        ILootType<?> lootType = choiceHolderData.getCurrentLootType();
        if (lootType == null) {
            return;
        }
        int cost = choiceHolderData.getRefreshCost();
        if (!experienceCost.consume(player, cost)) {
            player.displayClientMessage(Component.translatable("message.gene_hunter.refresh.not_enough_xp", cost), true);
            return;
        }
        choiceHolderData.setRefreshTimes(choiceHolderData.getRefreshTimes() + 1);
        doRoll(player, lootType, choiceHolderData.getCurrentNodeColor(), choiceHolderData.getCurrentFixedRolls());
    }

    /**
     * RPC 方法：客户端请求刷新选择
     */
    @RPCPacket("gene_hunter:refresh_choice")
    public static void rpcRefresh(RPCSender sender) {
        if (sender.isRemote()) {
            var player = sender.asPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                GeneHunterAPI.choiceManager().refresh(serverPlayer);
            }
        }
    }

    @RPCPacket("gene_hunter:claim_slot")
    public static void claimSlot(RPCSender sender, int slotIndex) {
        if (sender.isRemote()) {
            var player = sender.asPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                var choiceManager = GeneHunterAPI.choiceManager();
                var choiceHolderData = choiceManager.data(serverPlayer);
                var handler = choiceHolderData.getChoiceHolderHandler();
                if (slotIndex >= 0 && slotIndex < handler.getSlots()) {
                    ItemStack stack = handler.getStackInSlot(slotIndex);
                    if (!stack.isEmpty()) {
                        ItemStack claimedStack = createClaimStack(stack);
                        ILootType<?> lootType = choiceHolderData.getCurrentLootType();
                        if (lootType != null) {
                            lootType.claim(serverPlayer, claimedStack, LootManager.Context.of(serverPlayer));
                        } else {
                            // 如果没有战利品类型，直接给玩家
                            if (!serverPlayer.getInventory().add(claimedStack)) {
                                serverPlayer.spawnAtLocation(claimedStack);
                            }
                        }
                        choiceManager.endRoll(serverPlayer, claimedStack);

                    }
                }
            }
        }
    }

    private static ItemStack createClaimStack(ItemStack stack) {
        ItemStack claimedStack = stack.copy();
        ValueComp.set(claimedStack, 1);
        return claimedStack;
    }

}
