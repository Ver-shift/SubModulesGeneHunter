package com.pz.beyond.api.event.custom;

import com.pz.beyond.api.system.zone.AbstractZone;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 仅在服务端触发，表示生物通过多种方式，通过安全区，无论是传送还是步行或者死亡。这个事件无法取消，也无法改变结果
 */
public class PlayerChangeZoneEvent extends PlayerEvent{
    public PlayerChangeZoneEvent(ServerPlayer player, AbstractZone<?> oldZone, AbstractZone<?> newZone) {
        super(player);
        this.oldZone = oldZone;
        this.newZone = newZone;
    }

    private final AbstractZone<?> oldZone;
    private final AbstractZone<?> newZone;

    public AbstractZone<?> getOldZone() {
        return oldZone;
    }

    public AbstractZone<?> getNewZone() {
        return newZone;
    }
}
