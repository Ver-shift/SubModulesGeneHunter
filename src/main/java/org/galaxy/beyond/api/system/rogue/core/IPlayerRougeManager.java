package org.galaxy.beyond.api.system.rogue.core;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.galaxy.beyond.api.system.zone.ZoneType;

public interface IPlayerRougeManager {

    /**
     * 通过tick自动判断玩家的state
     */
    void tick(ServerPlayer player);

    /**
     * 尝试加入肉鸽系统
     */
    void intoRogue(ServerPlayer player);

    /**
     * 离开肉鸽系统，离开维度时固定调用
     */
    void leaveRogue(ServerPlayer player);

    //状态切换==================================================

    void playerLevelSafeZone(ServerPlayer player);

    void playerIntoSafeZone(ServerPlayer player);

    void playerChangeZone(ServerPlayer player, ZoneType from, ZoneType to);

    void useLootBag(ServerPlayer player);
3
    void clickNodeBlock(ServerPlayer player);

    void playerDeath(LivingDeathEvent event);


    //状态切换后触发的逻辑======================================
    void handlePreRogue(ServerPlayer player);


    //辅助逻辑==================================================
    void setState(ServerPlayer player,PlayerRogueState state);
    PlayerRogueState getState(ServerPlayer player);

    void giveItem(ServerPlayer player, Item item);
    boolean isInRogue(ServerPlayer player);
    /**
     *
     * @param player
     * @return 清理的物品总价值
     */
    int clearPlayerInventoryWithValueComp(ServerPlayer player);
}
