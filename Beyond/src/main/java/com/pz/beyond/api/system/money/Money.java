package com.pz.beyond.api.system.money;

import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 玩家钱币数据，玩家获得经验，就能获取钱币。钱币可以用来消费
 */
@Getter
public class Money {
//    Player

    private ServerPlayer player;
    private int money;

    public Money(ServerPlayer player) {
        this.player = player;
        this.money = player.totalExperience;
    }

    
}
