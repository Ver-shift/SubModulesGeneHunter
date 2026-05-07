package org.galaxy.beyond.api.system.rogue.player;

import lombok.AllArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.galaxy.beyond.api.init.BeyondComponentInit;
import org.galaxy.beyond.api.init.BeyondItemInit;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.IPlayerRougeManager;
import org.galaxy.beyond.api.system.rogue.core.IRogueManager;
import org.galaxy.beyond.api.system.rogue.core.PlayerRogueState;
import org.galaxy.beyond.component.ValueComp;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@AllArgsConstructor
public class PlayerRougeManager implements IPlayerRougeManager {

    private final IRogueManager rogueManager;


    @Override
    public void tick(ServerPlayer player) {
        switch (getState(player)) {
            case LOBBY -> {}
            case PRE_ROGUE -> {handlePreRogue(player);}
            case ON_PROGRESS -> {}
            case PRE_NODE -> {handlePreNode(player);}
            case PRE_EVENT -> {handlePreEvent(player);}
            case ON_EVENT -> {}
            case SPECTATOR -> {handleSpectator(player);}
            case DEAD -> {handleStateDeath(player);}
            case REWARD -> {handleReward(player);}
            case PROGRESS_FINISH -> {}
        }
    }

    @Override
    public void intoRogue(ServerPlayer player) {
        BeyondAPI.getBeyondLevelData(player.level()).getRogueData().getInGamePlayers().add(player);
    }

    @Override
    public void leaveRogue(ServerPlayer player) {
        BeyondAPI.getBeyondLevelData(player.level()).getRogueData().getInGamePlayers().remove(player);
    }

    @Override
    public void playerLevelSafeZone(ServerPlayer player) {
        //给玩家发放奖励袋
        giveItem(player, BeyondItemInit.LOOT_BAG.get());
    }

    @Override
    public void playerIntoSafeZone(ServerPlayer player) {
        int choice = 1;
        switch (choice){
            //在preRogue状态进行返回。
            case 1: clearPlayerInventoryWithValueComp(player);
            //在结束肉鸽过后返回
            case 2: {}
            //在开始游戏过后任意时候中途返回
            case 3: {}
        }
    }

    @Override
    public void useLootBag(ServerPlayer player) {
        //可以经过一些校验
        setState(player,PlayerRogueState.PRE_ROGUE);
    }

    @Override
    public void clickNodeBlock(ServerPlayer player) {

        int choice = 1;
        switch (choice){
            //节点为lock状态或者节点为preNode状态。
            case 1: setState(player,PlayerRogueState.PRE_NODE);
            //玩家在onEvent状态准备
            case 2: setState(player,PlayerRogueState.PRE_EVENT);
        }
    }

    @Override
    public void playerDeath(LivingDeathEvent event) {
        //设置为旁观者模式，
        if (event.getEntity() instanceof ServerPlayer player && isInRogue(player)) {
            setState(player,PlayerRogueState.SPECTATOR);
            player.setHealth(player.getMaxHealth());
            event.setCanceled(true);
        }
    }

    @Override
    public void handlePreRogue(ServerPlayer player) {
        giveItem(player, Items.IRON_SWORD);
    }

    public void handlePreNode(ServerPlayer player) {
        //给玩家发布当前准备人数和消息
        //将肉鸽状态设置为PreRogue(如果是InProgress状态的话)，
        //将当前缓存节点数据也设置为PreRogue来做出标记。
    }

    public void handlePreEvent(ServerPlayer player) {
        //给玩家发布当前准备人数和消息
    }

    public void handleSpectator(ServerPlayer player) {
        //根据玩家生命数量做出处理
    }

    /**
     * 玩家改为死亡状态，如果所有玩家都死亡状态，切换到Reward状态
     * @param player
     */
    public void handleStateDeath(ServerPlayer player) {

    }
    public void handleReward(ServerPlayer player) {
        //根据玩家的表现发放奖励，进行结算操作。并且将状态改为ProgressFinish



        setState(player,PlayerRogueState.PROGRESS_FINISH);
    }

    @Override
    public void setState(ServerPlayer player, PlayerRogueState state) {
        //如果状态相同不会有后续逻辑
    }

    @Override
    public PlayerRogueState getState(ServerPlayer player) {
        return null;
    }

    @Override
    public void giveItem(ServerPlayer player, Item item){
        ItemStack stack = new ItemStack(item);
        //todo :统一1块钱，后面进行完善。
        stack.set(BeyondComponentInit.ITEM_VALUE,new ValueComp(1));
        if (!player.getInventory().add(stack)) {
            player.spawnAtLocation(player.level(), stack);
        }
    }

    @Override
    public boolean isInRogue(ServerPlayer player) {
        return BeyondAPI.getBeyondLevelData(player.level()).getRogueData().getInGamePlayers().contains(player);
    }

    @Override
    public int clearPlayerInventoryWithValueComp(ServerPlayer player) {
        int[] totalValue = {0};
        int[] itemCount = {0};

        // 玩家主背包 + 护甲 + 副手
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ValueComp.has(stack)) {
                totalValue[0] += ValueComp.get(stack);
                itemCount[0] += stack.getCount();
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        // Curios 饰品栏
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
            handler.getCurios().forEach((slotId, stacksHandler) -> {
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (ValueComp.has(stack)) {
                        totalValue[0] += ValueComp.get(stack);
                        itemCount[0] += stack.getCount();
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            })
        );

        // TODO: 精妙背包 SophisticatedBackpacks - 需添加依赖后实现背包内物品遍历

        if (itemCount[0] > 0) {
            player.sendSystemMessage(Component.translatable(
                "beyond.info.clear_inventory", itemCount[0], totalValue[0]));
        }
        return totalValue[0];
    }

}
