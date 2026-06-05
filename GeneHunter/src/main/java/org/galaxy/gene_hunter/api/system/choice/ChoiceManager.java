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
import org.galaxy.gene_hunter.GeneHunter;
import org.galaxy.gene_hunter.api.init.GeneHunterMenuInit;
import org.galaxy.gene_hunter.api.init.GeneHunterLootInit;
import org.galaxy.gene_hunter.api.system.GeneHunterData;
import org.galaxy.gene_hunter.api.system.choice.core.IChoiceManager;
import org.galaxylib.api.GalaxyLibAPI;
import org.biotech.api.init.BiotechLootTypeInit;
import org.galaxylib.api.system.loot.LootManager;
import org.galaxylib.api.system.loot.core.ILootType;

import java.util.List;

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


    public void doRoll(ILootType<?> lootType) {
        doRoll(lootType, null);
    }

    @Override
    public void doRoll(ILootType<?> lootType, NodeColor nodeColor) {
        ItemStackHandler handler = choiceHolderData.getChoiceHolderHandler();
        choiceHolderData.clear();
        int maxSlots = Math.min(getChoiceCount(), handler.getSlots());
        NodeColor color = nodeColor == null ? NodeColor.GREEN : nodeColor;

        LootManager.Request request = createChoiceRequest(lootType, color);
        var result = GalaxyLibAPI.getLootManager().rollChoices(request, LootManager.Context.of(player, request.getRandomId()), maxSlots);
        choiceHolderData.setCurrentLootType(lootType);
        choiceHolderData.setCurrentNodeColor(color);

        for (int i = 0; i < result.options().size(); i++) {
            handler.setStackInSlot(i, result.options().get(i).stack());
        }
    }

    private LootManager.Request createChoiceRequest(ILootType<?> lootType, NodeColor nodeColor) {
        LootManager.Request.RequestBuilder builder = LootManager.Request.builder()
                .lootType(lootType)
                .replacement(false);

        if (lootType == GeneHunterLootInit.WEAPON_LOOT_TYPE.get()) {
            return builder
                    .tables(
                            List.of(
                                    GeneHunter.asResource("one_hand_weapon_base"),
                                    GeneHunter.asResource("two_hand_weapon_base"),
                                    GeneHunter.asResource("polearm_weapon_base")
                            )
                    )
                    .rolls(1)
                    .build();
        }

        if (lootType == BiotechLootTypeInit.XENE_TRAIT_LOOT_TYPE.get()) {
            return builder
                    .tables(List.of(GeneHunter.asResource("xene_trait_base")))
                    .weightModifier(new XeneChoiceWeightModifier(nodeColor))
                    .rolls(lootType.getPoolCount(player))
                    .build();
        }

        return builder.rolls(1).build();
    }

    @Override
    public void startRoll(ILootType<?> lootType) {
        startRoll(lootType, null);
    }

    @Override
    public void startRoll(ILootType<?> lootType, NodeColor nodeColor) {
        doRoll(lootType, nodeColor);
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

        ILootType<?> lootType = choiceHolderData.getCurrentLootType();
        if (lootType == null) {
            return;
        }
        choiceHolderData.clear();
        doRoll(lootType, choiceHolderData.getCurrentNodeColor());
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
                            ItemStack claimedStack = createClaimStack(stack);
                            ILootType<?> lootType = choiceManager.choiceHolderData.getCurrentLootType();
                            if (lootType != null) {
                                lootType.claim(serverPlayer, claimedStack, LootManager.Context.of(serverPlayer));
                            } else {
                                // 如果没有战利品类型，直接给玩家
                                if (!serverPlayer.getInventory().add(claimedStack)) {
                                    serverPlayer.spawnAtLocation(claimedStack);
                                }
                            }
                            manager.endRoll(claimedStack);

                        }
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
