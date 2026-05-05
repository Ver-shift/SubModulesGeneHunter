package org.galaxy.beyond.api.system.rogue.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import org.galaxy.beyond.api.event.custom.PlayerRogueStateChangeEvent;
import org.galaxy.beyond.api.init.BeyondItemInit;
import org.galaxy.beyond.api.system.BeyondAPI;
import org.galaxy.beyond.api.system.rogue.core.IPlayerRougeManager;
import org.galaxy.beyond.api.system.zone.ZoneType;

public class PlayerRougeManager implements IPlayerRougeManager {

    @Override
    public void tick(ServerPlayer player) {

    }

    @Override
    public void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to) {
        if (from == ZoneType.Safe_Zone){
            playerLevelSafeZone(player);
        }
        if (to == ZoneType.Safe_Zone){
            playerIntoSafeZone(player);
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
    public void intoProgress(ServerPlayer player) {

    }

    @Override
    public void setState(ServerPlayer player, PlayerRogueState newState) {
        PlayerRogueStateChangeEvent event = new PlayerRogueStateChangeEvent(player, getState(player), newState);
        PlayerRogueStateChangeEvent postEvent = NeoForge.EVENT_BUS.post(event);
        BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().setState(event.getNewState());
    }

    @Override
    public PlayerRogueState getState(ServerPlayer player) {
        return BeyondAPI.getBeyondPlayerData(player).getPlayerRogueData().getState();
    }



    @Override
    public boolean isInGame(ServerPlayer player) {
        return BeyondAPI.getBeyondLevelData(player.level()).getRogueData().getInGamePlayers().contains(player);
    }






    //玩家区域管理器
    private void playerLevelSafeZone(ServerPlayer player){
        //发放奖励袋(防止玩家不断往返区域高频触发)
        giveItem(player, BeyondItemInit.LOOT_BAG.get());
    }
    @Override
    public void playerUseLootBag(ServerPlayer player) {
        //给予玩家初始物资。
        giveItem(player, Items.IRON_SWORD);
        //变换玩家状态
        setState(player,PlayerRogueState.READY_ROGUE);
    }

    /**
     * 通过传送或者任意方式进行的操作。
     * @param player
     */
    private void playerIntoSafeZone(ServerPlayer player){

    }

    private void giveItem(ServerPlayer player, Item item){
        var stack = new ItemStack(item);
            if (!player.getInventory().add(stack)) {
                player.spawnAtLocation(player.level(),stack);
            }
    }

}
