package org.galaxy.beyond.api.system.rogue.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.galaxy.beyond.api.init.BeyondComponentInit;
import org.galaxy.beyond.component.ValueComp;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * 纯静态工具方法 —— 物品给予 + 背包结算。
 */
public final class RoguePlayerManager {

    private RoguePlayerManager() {}

    /**
     * 发放带 ValueComp 的物品，背包满则丢脚下。
     */
    public static void giveItem(ServerPlayer player, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(BeyondComponentInit.ITEM_VALUE, new ValueComp(1));
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(player.level(), stack);
        }
    }

    /**
     * 清除背包 + Curios 中所有带 ValueComp 的物品。
     * @return 回收总价值
     */
    public static int clearInventory(ServerPlayer player) {
        int[] totalValue = {0};
        int[] itemCount = {0};

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ValueComp.has(stack)) {
                totalValue[0] += ValueComp.get(stack);
                itemCount[0] += stack.getCount();
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        var curios = CuriosApi.getCuriosInventory(player);
        if (curios.isPresent()) {
            curios.get().getCurios().forEach((slotId, stacksHandler) -> {
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (ValueComp.has(stack)) {
                        totalValue[0] += ValueComp.get(stack);
                        itemCount[0] += stack.getCount();
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        }

        if (itemCount[0] > 0) {
            player.sendSystemMessage(Component.translatable(
                    "beyond.info.clear_inventory", itemCount[0], totalValue[0]));
        }
        return totalValue[0];
    }
}
