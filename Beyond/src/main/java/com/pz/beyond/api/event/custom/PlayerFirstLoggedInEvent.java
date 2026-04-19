package com.pz.beyond.api.event.custom;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerFirstLoggedInEvent extends PlayerEvent {

    public PlayerFirstLoggedInEvent(Player player) {
        super(player);
    }
}
